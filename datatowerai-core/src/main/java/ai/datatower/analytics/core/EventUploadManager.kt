package ai.datatower.analytics.core

import ai.datatower.analytics.Constant
import ai.datatower.analytics.Constant.EVENT_INFO_APP_ID
import ai.datatower.analytics.Constant.EVENT_INFO_SYN
import ai.datatower.analytics.Constant.PRE_EVENT_INFO_SYN
import ai.datatower.analytics.config.AnalyticsConfig
import ai.datatower.analytics.data.EventDataAdapter
import ai.datatower.analytics.network.HttpCallback
import ai.datatower.analytics.network.HttpService
import ai.datatower.analytics.network.RemoteService
import ai.datatower.analytics.network.RemoteVerificationException
import ai.datatower.analytics.network.ServiceUnavailableException
import ai.datatower.analytics.taskqueue.DataUploadQueue
import ai.datatower.analytics.taskqueue.MainQueue
import ai.datatower.analytics.taskqueue.MonitorQueue
import ai.datatower.analytics.taskqueue.launchSequential
import ai.datatower.analytics.utils.LogUtils
import ai.datatower.analytics.utils.NetworkUtils.isNetworkAvailable
import ai.datatower.analytics.utils.TimeCalibration
import ai.datatower.quality.DTErrorParams
import ai.datatower.quality.DTQualityHelper
import ai.datatower.quality.PerfAction
import ai.datatower.quality.PerfLogger
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.MalformedInputException


/**
 * 管理内部事件上报
 */
class EventUploadManager private constructor(
) {
    private val mWorker: Worker = Worker()
    private val mDateAdapter: EventDataAdapter? = EventDataAdapter.getInstance()
    private val mPoster: RemoteService = HttpService()
    private val mErrorInsertDataMap: MutableMap<String, JSONObject> = mutableMapOf()
    private var uploadErrorCount: Int = 0
        set(value) {
            field = value
            if (value > 3) {
                // 连续三次都没有成功
                MonitorQueue.get()?.reportUploadError(DTErrorParams.CODE_UPLOAD_ERROR_MULTi_TIMES)
            }
        }

    fun enqueueEventMessage(
        name: String,
        eventJson: JSONObject,
        eventSyn: String,
        insertHandler: ((code: Int, msg: String) -> Unit)? = null
    ) {
        val dataAdapter = mDateAdapter ?: return
        try {
            //插入数据库
            dataAdapter.addJSON(eventJson, eventSyn).onSameQueueThen {
                MainQueue.get().postTask {
                    checkInsertResult(it, name, eventJson, eventSyn, insertHandler)
                    //发送上报的message
                    Message.obtain().apply {
                        //上报标志
                        this.what = FLUSH_QUEUE
                        mWorker.runMessageOnce(this, FLUSH_DELAY)
                    }
                }
            }
        } catch (e: Exception) {
            LogUtils.i(TAG, "enqueueEventMessage error:$e")
            DTQualityHelper.instance.reportQualityMessage(
                DTErrorParams.CODE_INIT_DB_ERROR,
                "event name: $name ," + e.stackTraceToString(),
                DTErrorParams.INSERT_DB_NORMAL_ERROR
            )
            insertHandler?.invoke(
                DTErrorParams.CODE_INIT_DB_ERROR,
                DTErrorParams.INSERT_DB_NORMAL_ERROR
            )
        }
    }

    fun enqueueErrorInsertEventMessage() {
        try {
            if (mErrorInsertDataMap.isNotEmpty()) {
                val eventSyn = mErrorInsertDataMap.keys.first()
                val eventJson = mErrorInsertDataMap[eventSyn]
                val evenName = eventJson?.getString(Constant.EVENT_INFO_NAME)

                if (eventJson != null && evenName != null) {
                    mErrorInsertDataMap.remove(eventSyn)
                    enqueueEventMessage(evenName, eventJson, eventSyn)
                }
            }
        } catch (e: Exception) {
            LogUtils.i(TAG, "enqueueErrorInsertEventMessage error:$e")
        }
    }

    private fun checkInsertResult(
        insertCode: Int,
        eventName: String,
        eventJson: JSONObject,
        eventSyn: String,
        insertHandler: ((code: Int, msg: String) -> Unit)? = null
    ) {
        val msg =
            if (insertCode < 0) " Failed to insert the event " else " the event: $eventName  has been inserted to db，code = $insertCode  "
        insertHandler?.invoke(insertCode, msg)
        if (insertCode < 0) {
            if (!mErrorInsertDataMap.containsKey(eventSyn) && mErrorInsertDataMap.size < 20) {
                mErrorInsertDataMap[eventSyn] = eventJson
            }
            qualityReport(msg)
        }
        LogUtils.json(TAG, msg)
    }


    private fun qualityReport(msg: String) {
        DTQualityHelper.instance.reportQualityMessage(
            DTErrorParams.CODE_INSERT_DB_NORMAL_ERROR,
            msg, DTErrorParams.INSERT_DB_NORMAL_ERROR
        )
    }

    /**
     * 主动上报
     */
    fun flush(timeDelayMills: Long = 0L) {
        if (AnalyticsConfig.instance.isSdkDisable()) {
            return
        }
        Message.obtain().apply {
            what = FLUSH_QUEUE
            if (timeDelayMills == 0L) mWorker.runMessage(this)
            else mWorker.runMessageOnce(this, timeDelayMills)
        }
    }


    /**
     * 删除所有数据库内的事件
     */
    fun deleteAll() = mWorker.runMessage(Message.obtain().apply { what = DELETE_ALL })

    /**
     * 检查是否可以数据上报
     */
    private fun enableUploadData(): Boolean {
        try {
            if (AnalyticsConfig.instance.isSdkDisable()) {
                return false
            }

            // 接入方设置了手动触发上报，但尚未触发。
            if (!AnalyticsConfig.instance.mManualUploadSwitch.get()) {
                return false
            }

            //无网络
            if (!isNetworkAvailable(AnalyticsConfig.instance.mContext)) {
                LogUtils.d(TAG, "NetworkAvailable，disable upload")
                return false
            }
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
            DTQualityHelper.instance.reportQualityMessage(
                DTErrorParams.CODE_CHECK_ENABLE_UPLOAD_EXCEPTION,
                e.message,
                DTErrorParams.CHECK_ENABLE_UPLOAD_EXCEPTION,
                DTErrorParams.TYPE_WARNING
            )
            return false
        }
        return true
    }


    /**
     * 数据上报到服务器
     */
    private suspend fun uploadData(eventData: String? = null) {

        do {
            //不上报数据
            if (!enableUploadData()) return

            if (mDateAdapter == null) {
                break
            }

            PerfLogger.doPerfLog(PerfAction.TRACKBEGIN, System.currentTimeMillis())

            var eventsData = eventData ?: ""
            try {
                if (eventsData.isEmpty()) {
                    //读取数据库数据
                    PerfLogger.doPerfLog(PerfAction.READEVENTDATAFROMDBBEGIN, System.currentTimeMillis())
                    eventsData = runBlocking {
                        withTimeoutOrNull(5000) {
                            mDateAdapter.readEventsDataFromDb(Constant.EVENT_REPORT_SIZE).await()
                        }
                    }?.getOrThrow() ?: return
                    PerfLogger.doPerfLog(PerfAction.READEVENTDATAFROMDBEND, System.currentTimeMillis())
                }

                val jsonArray = JSONArray(eventsData)
                if (jsonArray.length() == 0) {
                    LogUtils.d(TAG, "db count = 0，disable upload")
                    PerfLogger.doPerfLog(PerfAction.TRACKEND, System.currentTimeMillis())
                    break
                }

                if (eventData.isNullOrEmpty()) {
                    // Splitting by app_id
                    val appId = jsonArray.getJSONObject(0)
                        .getJSONObject(Constant.EVENT_BODY)
                        .optString(EVENT_INFO_APP_ID)
                    val diffMap = mutableMapOf<String, JSONArray>()
                    val removed = mutableListOf<Int>();
                    for (i in 1 until jsonArray.length()) {
                        val crt = jsonArray.getJSONObject(i);
                        val crtAppId = crt.getJSONObject(Constant.EVENT_BODY).optString(EVENT_INFO_APP_ID)
                        if (crtAppId != appId) {
                            diffMap[crtAppId]?.put(crt) ?: run {
                                diffMap[crtAppId] = JSONArray().apply {
                                    put(crt)
                                }
                            }
                            removed.add(i)
                        }
                    }

                    // To ensure final eventsData only contains identical app_id.
                    for (idx in removed.reversed()) {
                        jsonArray.remove(idx);
                    }
                    eventsData = jsonArray.toString()

                    // Upload other app_id events
                    if (diffMap.isNotEmpty()) {
                        diffMap.forEach { (k, v) ->
                            LogUtils.d("Events with app_id '$k' are split (count: ${v.length()})")
                            Message.obtain().apply {
                                what = FLUSH_QUEUE_SPECIFY
                                obj = v.toString()
                                mWorker.runMessage(this)
                            }
                        }
                    }
                }

                //如果未进行时间同步，发空参数进行时间同步
                if (TimeCalibration.TIME_NOT_VERIFY_VALUE == TimeCalibration.instance.getVerifyTimeAsync()) {
                    LogUtils.d(TAG, "Failed to upload! reason: time do not calibrate yet")
                    TimeCalibration.instance.getReferenceTime()
                    PerfLogger.doPerfLog(PerfAction.TRACKEND, System.currentTimeMillis())
                    break
                }
            } catch (e: Exception) {
                uploadErrorCount++
                checkFailTimes()

                DTQualityHelper.instance.reportQualityMessage(
                    DTErrorParams.CODE_UPLOAD_ERROR_READ_DATA,
                    e.message,
                    DTErrorParams.HANDLE_UPLOAD_MESSAGE_ERROR,
                    DTErrorParams.TYPE_WARNING
                )
            }

            //事件主体，json格式
            var deleteMethod: ((JSONObject) -> Boolean)? = null
            var uploadInfo: String? = null
            eventsData.let {
                EventInfoCheckHelper.instance.correctEventInfo(it) { info ->
                    try {
                        uploadInfo = info
                        if (info.isNotEmpty() && info != "[]") {
                            //http 请求
                            deleteMethod = uploadDataToNet(info)
                        }
                    } catch (e: Exception) {
                        uploadErrorCount++
                        checkFailTimes()

                        DTQualityHelper.instance.reportQualityMessage(
                            DTErrorParams.CODE_HANDLE_UPLOAD_MESSAGE_ERROR,
                            e.message,
                            DTErrorParams.HANDLE_UPLOAD_MESSAGE_ERROR,
                            DTErrorParams.TYPE_WARNING
                        )
                    }
                }
            }

            deleteMethod?.let { deleteSelector ->
                PerfLogger.doPerfLog(PerfAction.DELETEDBBEGIN, System.currentTimeMillis())

                uploadInfo?.let {
                    //上报成功后，删除数据库数据
                    deleteEventAfterReport(uploadInfo!!, mDateAdapter, deleteSelector)
                    //避免事件积压，成功后再次上报
                    flush(FLUSH_DELAY)
                    //如果远程控制之前获取失败，这里再次获取
                    AnalyticsConfig.instance.getRemoteConfig()
                }

                PerfLogger.doPerfLog(PerfAction.DELETEDBEND, System.currentTimeMillis())
            }

            uploadErrorCount = 0
            PerfLogger.doPerfLog(PerfAction.TRACKEND, System.currentTimeMillis())

        } while (false)
    }

    private fun uploadDataToNet(
        event: String
    ): ((JSONObject) -> Boolean)? {
        var deleteSeletor: ((JSONObject) -> Boolean)? = null
        var errorMessage: String? = null
        try {

            PerfLogger.doPerfLog(PerfAction.UPLOADDATABEGIN, System.currentTimeMillis())

            val response: String = mPoster.performRequest(
                getEventUploadUrl(), event,
                false, null, null
            )
            val responseJson = JSONObject(response)
            val code = responseJson.getInt(HttpCallback.ResponseDataKey.KEY_CODE)
            if (code == 0) {
                deleteSeletor = { true }
            } else {
                // status == 200 && code != 0
                handleDifferentResponseCode(
                    code,
                    responseJson.optString(HttpCallback.ResponseDataKey.KEY_MSG, "")
                ).also { result ->
                    result.errorMessage?.let { errorMessage = it }
                    result.deleteSelector?.let { deleteSeletor = it }
                }
            }
            Log.w("xxx", event)
            LogUtils.json("$TAG upload event data ", event)
            LogUtils.json("$TAG upload event result ", responseJson)
        } catch (e: RemoteVerificationException) {
            // status != 200 && has response body
            handleRemoteVerificationException(e).also { result ->
                result.errorMessage?.let { errorMessage = it }
                result.deleteSelector?.let { deleteSeletor = it }
            }
        } catch (e: ServiceUnavailableException) {
            // status != 200 && no response body
            errorMessage =
                ("(SUE) Cannot post message to [" + getEventUploadUrl()) + "] due to " + e.message
        } catch (e: MalformedInputException) {
            errorMessage = ("Cannot interpret " + getEventUploadUrl()) + " as a URL."
        } catch (e: IOException) {
            errorMessage =
                ("(IO) Cannot post message to [" + getEventUploadUrl()) + "] due to " + e.message
        } catch (e: JSONException) {
            errorMessage = "Cannot post message due to JSONException"
        } catch (t: Throwable) {
            errorMessage = "Cannot post message due to unexpected exception: ${t.message}"
        } finally {

            PerfLogger.doPerfLog(PerfAction.UPLOADDATAEND, System.currentTimeMillis())

            if (!TextUtils.isEmpty(errorMessage)) {
                LogUtils.d(errorMessage)
                DTQualityHelper.instance.reportQualityMessage(
                    DTErrorParams.CODE_REPORT_ERROR_ON_RESPONSE,
                    errorMessage, level = DTErrorParams.TYPE_WARNING
                )
            }
        }
        return deleteSeletor
    }

    private suspend fun deleteEventAfterReport(
        event: String,
        mDateAdapter: EventDataAdapter,
        shouldDelete: (JSONObject) -> Boolean = { true }
    ) {
        //上报成功后删除本地数据
        try {
            val jsonArray = JSONArray(event)
            val length = jsonArray.length()
            val allEvents: MutableList<String> = mutableListOf()

            for (i in 0 until length) {
                jsonArray.optJSONObject(i)?.let {
                    if (shouldDelete(it)) {
                        if (it.optString(EVENT_INFO_SYN).isNotEmpty()) {
                            allEvents.add(it.optString(EVENT_INFO_SYN))
                        } else {
                            allEvents.add(it.optString(PRE_EVENT_INFO_SYN))
                        }
                    }
                }
            }

            LogUtils.d("DT HTTP", "${allEvents.size} events to delete...")

            if (allEvents.isNotEmpty()) {
                withTimeoutOrNull(5000) {
                    mDateAdapter.cleanupBatchEvents(allEvents).await()
                }
            }
        } catch (e: Exception) {
            uploadErrorCount++
            checkFailTimes()

            DTQualityHelper.instance.reportQualityMessage(
                DTErrorParams.CODE_DELETE_UPLOADED_EXCEPTION,
                e.message, DTErrorParams.DELETE_DB_EXCEPTION
            )
        }
    }

    private fun handleRemoteVerificationException(e: RemoteVerificationException): HandleCodeResult {
        var result = HandleCodeResult(null, null)

        try {
            val responseJson = JSONObject(e.response)
            val code = responseJson.getInt(HttpCallback.ResponseDataKey.KEY_CODE)
            val message = responseJson.getString(HttpCallback.ResponseDataKey.KEY_MSG)
            result = handleDifferentResponseCode(code, message)
        } catch (_: Throwable) {
            result = result.copy(
                errorMessage = ("(RVE) Cannot post message to [" + getEventUploadUrl()) + "] due to " + e.message
            )
        }

        return result
    }

    private fun handleDifferentResponseCode(code: Int, message: String): HandleCodeResult {
        var errorMessage: String? = null
        var deleteSelector: ((JSONObject) -> Boolean)? = null
        when (code) {
            2 -> {
                val appId = message.split(" ").last()
                LogUtils.e("DT Http", "Verification failed, due to #app_id ($appId) is invalid! Associated events will be removed!")
                errorMessage = message
                deleteSelector = { it.optString(EVENT_INFO_APP_ID, appId) == appId }
            }
            else -> throw Exception()
        }
        return HandleCodeResult(errorMessage, deleteSelector)
    }

    private fun checkFailTimes() {
        if (uploadErrorCount == 3) {
//            连续三次出错，主动上报一次
            DTQualityHelper.instance.reportQualityMessage(
                DTErrorParams.CODE_UPLOAD_ERROR_OVER_MAX,
                null
            )
        }
    }

    fun getEventUploadUrl(): String {
        val url = AnalyticsConfig.instance.reportUrl()
        if (url.isEmpty()) {
            return Constant.SERVER_URL_EXTERNAL + Constant.EVENT_REPORT_PATH
        }
        return url + Constant.EVENT_REPORT_PATH
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

        private inner class AnalyticsMessageHandler(looper: Looper) :
            Handler(looper) {
            override fun handleMessage(msg: Message) {
                try {
                    when (msg.what) {
                        FLUSH_QUEUE -> {
                            if (DataUploadQueue.get().taskCount() <= 1) {
                                DataUploadQueue.get().launchSequential { uploadData() }
                            }
                        }

                        DELETE_ALL -> {
                            try {
                                mDateAdapter?.deleteAllEvents()
                            } catch (e: Exception) {
                                LogUtils.printStackTrace(e)
                            }
                        }

                        FLUSH_QUEUE_SPECIFY -> {
                            val eventData = msg.obj?.toString()
                            DataUploadQueue.get().launchSequential { uploadData(eventData) }
                        }

                        else -> {
                            LogUtils.i(
                                TAG,
                                "Unexpected message received by SensorsData worker: $msg"
                            )
                        }
                    }
                } catch (e: RuntimeException) {
                    DTQualityHelper.instance.reportQualityMessage(
                        DTErrorParams.CODE_TRACK_ERROR,
                        e.stackTraceToString(), DTErrorParams.TRACK_MANAGER_ERROR
                    )
                    LogUtils.i(
                        TAG,
                        "Worker threw an unhandled exception",
                        e
                    )
                }
            }
        }

        init {
            val thread = HandlerThread(
                "DT.AnalyticsMessagesWorker",
                Thread.MIN_PRIORITY
            )
            thread.start()
            mHandler = AnalyticsMessageHandler(thread.looper)
        }
    }

    companion object {
        private const val TAG = Constant.LOG_TAG
        private const val FLUSH_QUEUE = 3
        private const val FLUSH_DELAY = 1000L
        private const val DELETE_ALL = 4
        private const val FLUSH_QUEUE_SPECIFY = 5

        private var instancessss: EventUploadManager? = null

        @Synchronized
        fun getInstance(): EventUploadManager? {
            if (null == instancessss) {
                instancessss = EventUploadManager()
            }
            return this.instancessss
        }
    }

}

data class HandleCodeResult(
    val errorMessage: String? = null,
    val deleteSelector: ((JSONObject) -> Boolean)? = null
)
