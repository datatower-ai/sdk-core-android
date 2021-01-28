package com.nodetower.analytics.core

import android.content.Context
import android.os.*
import android.text.TextUtils
import com.nodetower.analytics.api.RoiqueryAnalyticsAPI
import com.nodetower.analytics.data.DbAdapter
import com.nodetower.analytics.data.DbParams
import com.nodetower.analytics.utils.ConnectErrorException
import com.nodetower.analytics.utils.DebugModeException
import com.nodetower.analytics.utils.InvalidDataException
import com.nodetower.analytics.utils.ResponseErrorException
import com.nodetower.base.network.HttpCallback
import com.nodetower.base.network.HttpMethod
import com.nodetower.base.network.RequestHelper
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
    var mAnalyticsDataAPI: RoiqueryAnalyticsAPI
) {
    private val mWorker: Worker = Worker()
    private val mDbAdapter: DbAdapter? = DbAdapter.getInstance()


    fun enqueueEventMessage(name: String, eventJson: JSONObject) {
        try {
            synchronized(mDbAdapter!!) {
                //插入数据库
                val ret = mDbAdapter.addJSON(eventJson)
                val msg = if (ret < 0) "Failed to enqueue the event: $eventJson" else "insert event to db : $eventJson"
                LogUtils.d(TAG,msg)
                //发送上报的message
                Message.obtain().apply {
                    //上报标志
                    this.what = FLUSH_QUEUE
                    //库存已满
                    if (ret == DbParams.DB_OUT_OF_MEMORY_ERROR) {
                        mWorker.runMessage(this)
                    } else {
                        // 超过本地缓存 立即发送
                        if ( ret > mAnalyticsDataAPI.flushBulkSize) {
                            mWorker.runMessage(this)
                        } else {
                            //不立即上报，有时间间隔
                            mWorker.runMessageOnce(this, mAnalyticsDataAPI.flushInterval.toLong())
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LogUtils.i(TAG, "enqueueEventMessage error:$e")
        }
    }

    /**
     * 主动上报
     */
    fun flush(timeDelayMills: Long = 0L) {
        Message.obtain().apply {
            what = FLUSH_QUEUE
            if (timeDelayMills == 0L) mWorker.runMessage(this)
            else mWorker.runMessageOnce(this, timeDelayMills)
        }
    }

    /**
     * 删除所有数据库内的事件，慎用
     */
    fun deleteAll() = mWorker.runMessage(Message.obtain().apply { what = DELETE_ALL })

    /**
     * 检查是否可以数据上报
     */
    private fun checkSendData(): Boolean {
        try {
            if (!mAnalyticsDataAPI.isNetworkRequestEnable) {
                LogUtils.i(TAG, "NetworkRequest 已关闭，不发送数据！")
                return false
            }
            if (TextUtils.isEmpty(mAnalyticsDataAPI.getServerUrl())) {
                LogUtils.i(TAG, "Server url is null or empty.")
                return false
            }
            //无网络
            if (!isNetworkAvailable(mContext)) {
                return false
            }
            //不符合同步数据的网络策略
            val networkType = networkType(mContext)
            if (!isShouldFlush(networkType, mAnalyticsDataAPI.getFlushNetworkPolicy())) {
                LogUtils.i(TAG, String.format("您当前网络为 %s，无法发送数据，请确认您的网络发送策略！", networkType))
                return false
            }
            // 如果开启多进程上报
            if (mAnalyticsDataAPI.isMultiProcessFlushData()) {
                // 已经有进程在上报
                if (mDbAdapter!!.isSubProcessFlushing) {
                    return false
                }
                DbAdapter.getInstance()!!.commitSubProcessFlushState(true)
            } else if (!mAnalyticsDataAPI.mIsMainProcess) { //不是主进程
                return false
            }
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
            return false
        }
        return true
    }

    /**
     * 数据上报到服务器
     */
    private fun sendData() {
        //不上报数据
        if (!checkSendData()) return
        //这里每次只发送一条
        var eventsData: Array<String>? =
            mDbAdapter?.generateDataString(DbParams.TABLE_EVENTS, 1)

        if (eventsData == null) {
            DbAdapter.getInstance()!!.commitSubProcessFlushState(false)
            return
        }
        //列表最后一条数据的id，删除时根据此id <= 进行删除
        val lastId = eventsData[0]
        //事件的json的
        val event = eventsData[1]

        //http 请求
        RequestHelper.Builder(HttpMethod.POST, mAnalyticsDataAPI.getServerUrl() + "/report")
            .jsonData(event)
            .callback(object : HttpCallback.StringCallback() {
                override fun onFailure(code: Int, errorMessage: String?) {
                    LogUtils.d(TAG, errorMessage)
                }

                override fun onResponse(response: String?) {
                    LogUtils.d(TAG, response)
                    if (!response.isNullOrBlank()) {
                        if (JSONObject(response).get("code") == 0) {
                            //上报成功删除数据
                            mDbAdapter?.cleanupEvents(lastId)
                        }
                    }
                }
                override fun onAfter() {

                }
            }).execute()
    }

    private inner class Worker {
        private val mHandlerLock = Any()
        private val mHandler: Handler?
        fun runMessage(msg: Message) {
            synchronized(mHandlerLock) {
                mHandler?.sendMessage(msg)
                    ?: LogUtils.i(
                        TAG,
                        "Dead worker dropping a message: " + msg.what
                    )
            }
        }

        fun runMessageOnce(msg: Message, delay: Long) {
            synchronized(mHandlerLock) {
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

        private inner class AnalyticsMessageHandler constructor(looper: Looper) :
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
            analyticsAPI: RoiqueryAnalyticsAPI
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