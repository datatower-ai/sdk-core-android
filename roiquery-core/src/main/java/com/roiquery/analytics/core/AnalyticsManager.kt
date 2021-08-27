package com.roiquery.analytics.core

import android.content.Context
import android.os.*
import android.text.TextUtils
import com.roiquery.analytics.Constant
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.api.AnalyticsImp
import com.roiquery.analytics.data.DataParams
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.network.HttpCallback
import com.roiquery.analytics.network.HttpMethod
import com.roiquery.analytics.network.RequestHelper
import com.roiquery.analytics.utils.AppInfoUtils
import com.roiquery.analytics.utils.LogUtils
import com.roiquery.analytics.utils.NetworkUtils.isNetworkAvailable

import org.json.JSONObject
import kotlin.collections.HashMap


/**
 * 管理内部事件采集、上报
 */
class AnalyticsManager private constructor(
    var mContext: Context,
//    var mAnalyticsDataAPI: AnalyticsImp
) {
    private val mWorker: Worker = Worker()
    private val mDateAdapter: EventDateAdapter? = EventDateAdapter.getInstance()
    private var mLastEventName: String? = null
    private var mLastEventTime: Long? = null
    private var mLastEventJson: JSONObject? = null


    fun enqueueEventMessage(name: String, eventJson: JSONObject) {
        try {
            if (mDateAdapter == null) return
            synchronized(mDateAdapter) {
                //是否数据重复
                if (isEventRepetitive(eventJson)) return
                //插入数据库
                val insertedCount = mDateAdapter.addJSON(eventJson)
                checkAppAttributeInsertState(insertedCount,name)
                val msg =
                    if (insertedCount < 0) " Failed to insert the event " else " the event: $name  has been inserted to db，count = $insertedCount  "
                LogUtils.json(TAG + msg, eventJson.toString())
                //发送上报的message
                Message.obtain().apply {
                    //上报标志
                    this.what = FLUSH_QUEUE
                    // 立即发送：有特殊事件需要立即上报、库存已满（无法插入）、超过允许本地缓存日志的最大条目数
                    if (isNeedFlushImmediately(name)
                        || insertedCount == DataParams.DB_OUT_OF_MEMORY_ERROR
                        || insertedCount > 100
                    ) {
                        mWorker.runMessage(this)
                    } else {
                        //不立即上报，有时间间隔
                        mWorker.runMessageOnce(this, 2000L)
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
     * app_attribute 事件采集情况
     */
    private fun checkAppAttributeInsertState(insertCount: Int, eventName: String) {
        if (insertCount > 0 && Constant.PRESET_EVENT_TAG + eventName ==  Constant.PRESET_EVENT_APP_ATTRIBUTE) {
            mDateAdapter?.isAttributed = true
        }
    }

    /**
     * 重复数据校验
     */
    private fun isEventRepetitive(
        eventJson: JSONObject
    ): Boolean {
        var isRepetitive = false
        val eventName = eventJson.getString(Constant.EVENT_INFO_NAME)
        val eventTime = eventJson.getString(Constant.EVENT_INFO_TIME).toLong()
        if (mLastEventName == null) {
            isRepetitive = false
        } else if (mLastEventName == eventName && eventTime - mLastEventTime!! < 1000) {
            if (mLastEventJson != null) {
                val currentKeys = mutableListOf<String>().apply {
                    eventJson.getJSONObject(Constant.EVENT_INFO_PROPERTIES).keys().forEach {
                        if (!it.startsWith("#")) {
                            this.add(it)
                        }
                    }
                }
                val lastKeys = mutableListOf<String>().apply {
                    mLastEventJson!!.getJSONObject(Constant.EVENT_INFO_PROPERTIES).keys().forEach {
                        if (!it.startsWith("#")) {
                            this.add(it)
                        }
                    }
                }
                //same key
                if (currentKeys.size == lastKeys.size && currentKeys.containsAll(lastKeys)) {
                    isRepetitive = true
                    for (key in currentKeys) {
                        //if the values do not equals, that means different event
                        if (!mLastEventJson!!.getJSONObject(Constant.EVENT_INFO_PROPERTIES)
                                .getString(key)
                                .equals(
                                    eventJson.getJSONObject(Constant.EVENT_INFO_PROPERTIES)
                                        .getString(key)
                                )
                        ) {
                            isRepetitive = false
                            break
                        }
                    }
                }
            }
        }
        mLastEventName = eventName
        mLastEventTime = eventTime
        mLastEventJson = eventJson
        return isRepetitive
    }

    /**
     * 是否需要立即上报
     */
    private fun isNeedFlushImmediately(eventName: String): Boolean {
        val tagName = Constant.PRESET_EVENT_TAG + eventName
        return tagName == Constant.PRESET_EVENT_APP_OPEN
                || tagName == Constant.PRESET_EVENT_APP_FIRST_OPEN
                || tagName == Constant.PRESET_EVENT_APP_ATTRIBUTE
                || tagName == Constant.PRESET_EVENT_APP_CLOSE
    }

    /**
     * 删除所有数据库内的事件，慎用
     */
    fun deleteAll() = mWorker.runMessage(Message.obtain().apply { what = DELETE_ALL })

    /**
     * 检查是否可以数据上报
     */
    private fun enableUploadData(): Boolean {
        try {
            //无网络
            if (!isNetworkAvailable(mContext)) {
                LogUtils.d(TAG, "NetworkAvailable，disable upload")
                return false
            }
            if (mDateAdapter?.enableUpload == false) {
                LogUtils.i(TAG, "A process is currently uploading，or upload is disable")
                return false
            } else {
                mDateAdapter?.enableUpload = false
            }
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
            mDateAdapter?.enableUpload = true
            return false
        }
        return true
    }

    /**
     * 数据上报到服务器
     */
    private fun uploadData() {
        //不上报数据
        if (!enableUploadData()) return

        if (mDateAdapter == null) {
            mDateAdapter?.enableUpload = true
            return
        }
        //读取数据库数据
        var eventsData: Array<String>?
        synchronized(mDateAdapter) {
            eventsData = mDateAdapter.generateDataString(
                Constant.EVENT_REPORT_SIZE
            )
        }

        if (eventsData == null) {
            LogUtils.d(TAG, "db count = 0，disable upload")
            mDateAdapter.enableUpload = true
            return
        }
        LogUtils.json(
            TAG,
            "event uploading,process:${AppInfoUtils.getCurrentProcessName(mContext.applicationContext)}"
        )
        //列表最后一条数据的id，删除时根据此id <= 进行删除
        val lastId = eventsData!![0]
        //事件主体，json格式
        val event = eventsData!![1]

        //http 请求
        RequestHelper.Builder(
            HttpMethod.POST,
            Constant.EVENT_REPORT_URL
        )
            .jsonData(event)
            .retryCount(Constant.EVENT_REPORT_TRY_COUNT)
            .callback(object :
                HttpCallback.StringCallback() {
                override fun onFailure(code: Int, errorMessage: String?) {
                    LogUtils.d(TAG, errorMessage)
//                    mAnalyticsDataAPI.trackQualityEvent(
//                        "uploadData onFailure && events = $event, && error = $errorMessage"
//                    )
                }

                override fun onResponse(response: String?) {
                    LogUtils.d("$TAG upload event url  ", Constant.EVENT_REPORT_URL)
                    LogUtils.json("$TAG upload event result  ", response)
                    if (!response.isNullOrBlank() && JSONObject(response).getInt(ResponseDataKey.KEY_CODE) == 0) {
                        //上报成功后删除本地数据
                        val leftCount = mDateAdapter.cleanupEvents(lastId)
                        LogUtils.d(TAG, "db left count = $leftCount")
                        //避免事件积压，成功后再次上报
                        flush(2000L)
                    }
                }

                override fun onAfter() {
                    mDateAdapter.enableUpload = true
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
                            uploadData()
                        }
                        DELETE_ALL -> {
                            try {
                                mDateAdapter?.deleteAllEvents()
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
                "com.roiquery.analytics.AnalyticsMessages.Worker",
                Thread.MIN_PRIORITY
            )
            thread.start()
            mHandler = AnalyticsMessageHandler(thread.looper)
        }
    }

    companion object {
        private const val TAG = Constant.LOG_TAG
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
//            analyticsAPI: AnalyticsImp
        ): AnalyticsManager? {
            synchronized(S_INSTANCES) {
                val appContext: Context = messageContext.applicationContext
                val ret: AnalyticsManager?
                if (!S_INSTANCES.containsKey(appContext)) {
                    ret = AnalyticsManager(appContext)
                    S_INSTANCES[appContext] = ret
                } else {
                    ret = S_INSTANCES[appContext]
                }
                return ret
            }
        }
    }

}