package com.nodetower.analytics.core

import android.content.Context
import android.os.*
import android.text.TextUtils
import com.nodetower.analytics.api.NTAnalyticsAPI
import com.nodetower.analytics.data.DbAdapter
import com.nodetower.analytics.data.DbParams
import com.nodetower.analytics.utils.ConnectErrorException
import com.nodetower.analytics.utils.DebugModeException
import com.nodetower.analytics.utils.InvalidDataException
import com.nodetower.analytics.utils.ResponseErrorException
import com.nodetower.base.utils.LogUtils
import com.nodetower.base.utils.NetworkUtils.isNetworkAvailable
import com.nodetower.base.utils.NetworkUtils.isShouldFlush
import com.nodetower.base.utils.NetworkUtils.networkType
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.collections.HashMap


/**
 * 管理内部事件采集、上报
 */
class AnalyticsManager private constructor(
    var mContext: Context,
    var mAnalyticsDataAPI: NTAnalyticsAPI
) {
    private val mWorker: Worker = Worker()
    private val mDbAdapter: DbAdapter? = DbAdapter.getInstance()


    fun enqueueEventMessage(name: String, eventJson: JSONObject) {
        try {
            synchronized(mDbAdapter!!) {
                //插入数据库
                val ret = mDbAdapter.addJSON(eventJson)
                if (ret < 0) {
                    val error = "Failed to enqueue the event: $eventJson"
                    if (mAnalyticsDataAPI.isDebugMode) {
                        throw DebugModeException(error)
                    } else {
                        LogUtils.i(TAG, error)
                    }
                }
                val m: Message = Message.obtain()
                m.what = FLUSH_QUEUE
                if (mAnalyticsDataAPI.isDebugMode || ret == DbParams.DB_OUT_OF_MEMORY_ERROR) {
                    mWorker.runMessage(m)
                } else {
                    // track_signup 立即发送
                    if (name == "track_signup" || ret > mAnalyticsDataAPI.flushBulkSize) {
                        mWorker.runMessage(m)
                    } else {
                        val interval: Int = mAnalyticsDataAPI.flushInterval
                        mWorker.runMessageOnce(m, interval.toLong())
                    }
                }
            }
        } catch (e: Exception) {
            LogUtils.i(TAG, "enqueueEventMessage error:$e")
        }
    }


    fun flush(timeDelayMills: Long = 0L) {
        Message.obtain().apply {
            what = FLUSH_QUEUE
            if (timeDelayMills == 0L) mWorker.runMessage(this)
                else mWorker.runMessageOnce(this, timeDelayMills)
        }
    }

    fun deleteAll() = mWorker.runMessage(Message.obtain().apply { what = DELETE_ALL })

    private fun sendData() {
        try {
            if (!mAnalyticsDataAPI.isNetworkRequestEnable) {
                LogUtils.i(TAG, "NetworkRequest 已关闭，不发送数据！")
                return
            }
            if (TextUtils.isEmpty(mAnalyticsDataAPI.getServerUrl())) {
                LogUtils.i(TAG, "Server url is null or empty.")
                return
            }

            //无网络
            if (!isNetworkAvailable(mContext)) {
                return
            }

            //不符合同步数据的网络策略
            val networkType = networkType(mContext)
            if (!isShouldFlush(networkType, mAnalyticsDataAPI.getFlushNetworkPolicy())) {
                LogUtils.i(TAG, String.format("您当前网络为 %s，无法发送数据，请确认您的网络发送策略！", networkType))
                return
            }

            // 如果开启多进程上报
            if (mAnalyticsDataAPI.isMultiProcessFlushData()) {
                // 已经有进程在上报
                if (mDbAdapter!!.isSubProcessFlushing) {
                    return
                }
                DbAdapter.getInstance()!!.commitSubProcessFlushState(true)
            } else if (!mAnalyticsDataAPI.mIsMainProcess) { //不是主进程
                return
            }
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
            return
        }
        var count = 100
        while (count > 0) {
            var deleteEvents = true
            var eventsData: Array<String>?
            synchronized(mDbAdapter!!) {
                eventsData = if (mAnalyticsDataAPI.isDebugMode) {
                    /* debug 模式下服务器只允许接收 1 条数据 */
                    mDbAdapter.generateDataString(DbParams.TABLE_EVENTS, 1)
                } else {
                    mDbAdapter.generateDataString(DbParams.TABLE_EVENTS, 50)
                }
            }
            if (eventsData == null) {
                DbAdapter.getInstance()!!.commitSubProcessFlushState(false)
                return
            }
            val lastId = eventsData!![0]
            val rawMessage = eventsData!![1]
            val gzip = eventsData!![2]
            var errorMessage: String? = null
            try {
                var data = rawMessage
                if (DbParams.GZIP_DATA_EVENT == gzip) {
//                    data = encodeData(rawMessage)
                }
                if (!TextUtils.isEmpty(data)) {
//                    sendHttpRequest(mAnalyticsDataAPI.getServerUrl(), data, gzip, rawMessage, false)
                }
            } catch (e: ConnectErrorException) {
                deleteEvents = false
                errorMessage = "Connection error: " + e.message
            } catch (e: InvalidDataException) {
                errorMessage = "Invalid data: " + e.message
            } catch (e: ResponseErrorException) {
//                deleteEvents = isDeleteEventsByCode(e.httpCode)
                errorMessage = "ResponseErrorException: " + e.message
            } catch (e: Exception) {
                deleteEvents = false
                errorMessage = "Exception: " + e.message
            } finally {
                val isDebugMode: Boolean = mAnalyticsDataAPI.isDebugMode

                if (deleteEvents || isDebugMode) {
                    count = mDbAdapter.cleanupEvents(lastId)
                    LogUtils.i(
                        TAG,
                        java.lang.String.format(Locale.getDefault(), "Events flushed. [left = %d]", count)
                    )
                } else {
                    count = 0
                }
            }
        }
        if (mAnalyticsDataAPI.isMultiProcessFlushData()) {
            mDbAdapter!!.commitSubProcessFlushState(false)
        }
    }
//
//    @Throws(ConnectErrorException::class, ResponseErrorException::class)
//    private fun sendHttpRequest(
//        path: String?,
//        data: String,
//        gzip: String,
//        rawMessage: String,
//        isRedirects: Boolean
//    ) {
//        var connection: HttpURLConnection? = null
//        var `in`: InputStream? = null
//        var out: OutputStream? = null
//        var bout: BufferedOutputStream? = null
//        try {
//            val url = URL(path)
//            connection = url.openConnection() as HttpURLConnection
//            if (connection == null) {
//                SALog.i(
//                    TAG,
//                    java.lang.String.format(
//                        "can not connect %s, it shouldn't happen",
//                        url.toString()
//                    ),
//                    null
//                )
//                return
//            }
//            val configOptions: SAConfigOptions = SensorsDataAPI.getConfigOptions()
//            if (configOptions != null && configOptions.mSSLSocketFactory != null && connection is HttpsURLConnection) {
//                (connection as HttpsURLConnection).setSSLSocketFactory(configOptions.mSSLSocketFactory)
//            }
//            connection.setInstanceFollowRedirects(false)
//            if (mAnalyticsDataAPI.getDebugMode() === SensorsDataAPI.DebugMode.DEBUG_ONLY) {
//                connection.addRequestProperty("Dry-Run", "true")
//            }
//            connection.setRequestProperty("Cookie", mAnalyticsDataAPI.getCookie(false))
//            val builder: Uri.Builder = Builder()
//            //先校验crc
//            if (!TextUtils.isEmpty(data)) {
//                builder.appendQueryParameter("crc", data.hashCode().toString())
//            }
//            builder.appendQueryParameter("gzip", gzip)
//            builder.appendQueryParameter("data_list", data)
//            val query: String = builder.build().getEncodedQuery()
//            if (TextUtils.isEmpty(query)) {
//                return
//            }
//            connection.setFixedLengthStreamingMode(query.getBytes(CHARSET_UTF8).length)
//            connection.setDoOutput(true)
//            connection.setRequestMethod("POST")
//            out = connection.getOutputStream()
//            bout = BufferedOutputStream(out)
//            bout.write(query.getBytes(CHARSET_UTF8))
//            bout.flush()
//            val responseCode: Int = connection.getResponseCode()
//            SALog.i(TAG, "responseCode: $responseCode")
//            if (!isRedirects && needRedirects(responseCode)) {
//                val location = getLocation(connection, path)
//                if (!TextUtils.isEmpty(location)) {
//                    closeStream(bout, out, null, connection)
//                    sendHttpRequest(location, data, gzip, rawMessage, true)
//                    return
//                }
//            }
//            `in` = try {
//                connection.getInputStream()
//            } catch (e: FileNotFoundException) {
//                connection.getErrorStream()
//            }
//            val responseBody = slurp(`in`)
//            `in`.close()
//            `in` = null
//            val response = String(responseBody, CHARSET_UTF8)
//            if (SALog.isLogEnabled()) {
//                val jsonMessage: String = JSONUtils.formatJson(rawMessage)
//                // 状态码 200 - 300 间都认为正确
//                if (responseCode >= HttpURLConnection.HTTP_OK &&
//                    responseCode < HttpURLConnection.HTTP_MULT_CHOICE
//                ) {
//                    SALog.i(TAG, "valid message: \n$jsonMessage")
//                } else {
//                    SALog.i(TAG, "invalid message: \n$jsonMessage")
//                    SALog.i(
//                        TAG,
//                        java.lang.String.format(Locale.CHINA, "ret_code: %d", responseCode)
//                    )
//                    SALog.i(TAG, java.lang.String.format(Locale.CHINA, "ret_content: %s", response))
//                }
//            }
//            if (responseCode < HttpURLConnection.HTTP_OK || responseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
//                // 校验错误
//                throw ResponseErrorException(
//                    String.format(
//                        "flush failure with response '%s', the response code is '%d'",
//                        response, responseCode
//                    ), responseCode
//                )
//            }
//        } catch (e: IOException) {
//            throw ConnectErrorException(e)
//        } finally {
//            closeStream(bout, out, `in`, connection)
//        }
//    }
//
//    /**
//     * 在服务器正常返回状态码的情况下，目前只有 (>= 500 && < 600) || 404 || 403 才不删数据
//     *
//     * @param httpCode 状态码
//     * @return true: 删除数据，false: 不删数据
//     */
//    private fun isDeleteEventsByCode(httpCode: Int): Boolean {
//        var shouldDelete = true
//        if (httpCode == HttpURLConnection.HTTP_NOT_FOUND || httpCode == HttpURLConnection.HTTP_FORBIDDEN ||
//            httpCode >= HttpURLConnection.HTTP_INTERNAL_ERROR && httpCode < 600
//        ) {
//            shouldDelete = false
//        }
//        return shouldDelete
//    }
//
//    private fun closeStream(
//        bout: BufferedOutputStream?,
//        out: OutputStream?,
//        `in`: InputStream?,
//        connection: HttpURLConnection?
//    ) {
//        if (null != bout) {
//            try {
//                bout.close()
//            } catch (e: Exception) {
//                SALog.i(TAG, e.message)
//            }
//        }
//        if (null != out) {
//            try {
//                out.close()
//            } catch (e: Exception) {
//                SALog.i(TAG, e.message)
//            }
//        }
//        if (null != `in`) {
//            try {
//                `in`.close()
//            } catch (e: Exception) {
//                SALog.i(TAG, e.message)
//            }
//        }
//        if (null != connection) {
//            try {
//                connection.disconnect()
//            } catch (e: Exception) {
//                SALog.i(TAG, e.message)
//            }
//        }
//    }
//
//    @Throws(InvalidDataException::class)
//    private fun encodeData(rawMessage: String): String {
//        var gos: GZIPOutputStream? = null
//        return try {
//            val os = ByteArrayOutputStream(rawMessage.getBytes(CHARSET_UTF8).length)
//            gos = GZIPOutputStream(os)
//            gos.write(rawMessage.getBytes(CHARSET_UTF8))
//            gos.close()
//            val compressed: ByteArray = os.toByteArray()
//            os.close()
//            String(Base64Coder.encode(compressed))
//        } catch (exception: IOException) {
//            // 格式错误，直接将数据删除
//            throw InvalidDataException(exception)
//        } finally {
//            if (gos != null) {
//                try {
//                    gos.close()
//                } catch (e: IOException) {
//                    // ignore
//                }
//            }
//        }
//    }

    // Worker will manage the (at most single) IO thread associated with
    // this AnalyticsMessages instance.
    // XXX: Worker class is unnecessary, should be just a subclass of HandlerThread
    private inner class Worker  constructor() {
        private val mHandlerLock = Any()
        private val mHandler: Handler?
        fun runMessage(msg: Message) {
            synchronized(mHandlerLock) {
                // We died under suspicious circumstances. Don't try to send any more events.
                mHandler?.sendMessage(msg)
                    ?: LogUtils.i(
                        TAG,
                        "Dead worker dropping a message: " + msg.what
                    )
            }
        }

        fun runMessageOnce(msg: Message, delay: Long) {
            synchronized(mHandlerLock) {
                // We died under suspicious circumstances. Don't try to send any more events.
                if (mHandler == null) {
                    LogUtils.i(
                        TAG,
                        "Dead worker dropping a message: " + msg.what
                    )
                } else {
                    if (!mHandler.hasMessages(msg.what)) {
                        mHandler.sendMessageDelayed(msg, delay)
                    }
                }
            }
        }

        private inner class AnalyticsMessageHandler  constructor(looper: Looper) :
            Handler(looper) {

            override fun handleMessage(msg: Message) {
                try {
                    when (msg.what) {
                        FLUSH_QUEUE -> {
                            sendData()
                        }
                        DELETE_ALL -> {
                            try {
                                mDbAdapter!!.deleteAllEvents()
                            } catch (e: Exception) {
                                LogUtils.printStackTrace(e)
                            }
                        }
                        else -> {
                            LogUtils.i(
                                TAG,
                                "Unexpected message received by SensorsData worker: $msg"
                            )
                        }
                    }
                } catch (e: RuntimeException) {
                    LogUtils.i(TAG, "Worker threw an unhandled exception", e)
                }
            }
        }

        init {
            val thread = HandlerThread(
                "com.nodetower.analytics.AnalyticsMessages.Worker",
                Thread.MIN_PRIORITY
            )
            thread.start()
            mHandler = AnalyticsMessageHandler(thread.looper)
        }
    }

    companion object {
        private const val TAG = "AnalyticsMessages"
        private const val FLUSH_QUEUE = 3
        private const val DELETE_ALL = 4
        private val S_INSTANCES: MutableMap<Context, AnalyticsManager> = HashMap()

        /**
         * 获取 AnalyticsMessages 对象
         *
         * @param messageContext Context
         */
        fun getInstance(
            messageContext: Context,
            analyticsAPI:  NTAnalyticsAPI
        ): AnalyticsManager? {
            synchronized(S_INSTANCES) {
                val appContext: Context = messageContext.applicationContext
                val ret: AnalyticsManager?
                if (!S_INSTANCES.containsKey(appContext)) {
                    ret = AnalyticsManager(appContext, analyticsAPI)
                    S_INSTANCES[appContext] = ret
                } else {
                    ret = S_INSTANCES[appContext]
                }
                return ret
            }
        }

        @Throws(IOException::class)
        private fun slurp(inputStream: InputStream): ByteArray {
            val buffer = ByteArrayOutputStream()
            var nRead: Int
            val data = ByteArray(8192)
            while (inputStream.read(data, 0, data.size).also { nRead = it } != -1) {
                buffer.write(data, 0, nRead)
            }
            buffer.flush()
            return buffer.toByteArray()
        }
    }

}