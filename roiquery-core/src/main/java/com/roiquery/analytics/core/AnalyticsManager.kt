package com.roiquery.analytics.core

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import com.roiquery.analytics.Constant
import com.roiquery.analytics.Constant.EVENT_INFO_SYN
import com.roiquery.analytics.Constant.PRE_EVENT_INFO_SYN
import com.roiquery.analytics.ROIQueryCoroutineScope
import com.roiquery.analytics.data.DataParams
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.network.HttpCallback
import com.roiquery.analytics.network.HttpMethod
import com.roiquery.analytics.network.RequestHelper
import com.roiquery.analytics.utils.LogUtils
import com.roiquery.analytics.utils.NetworkUtils.isNetworkAvailable
import com.roiquery.analytics.utils.TimeCalibration
import com.roiquery.quality.ROIQueryErrorParams
import com.roiquery.quality.ROIQueryQualityHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.HashMap


/**
 * 管理内部事件采集、上报
 */
class AnalyticsManager private constructor(
    var mContext: Context,
//    var mAnalyticsDataAPI: AnalyticsImp
) : ROIQueryCoroutineScope() {
    private val mWorker: Worker = Worker()
    private val mDateAdapter: EventDateAdapter? = EventDateAdapter.getInstance()


    fun enqueueEventMessage(name: String, eventJson: JSONObject,eventSyn:String) {
        try {
            if (mDateAdapter == null) return
            synchronized(mDateAdapter) {
                scope.launch {
                    //插入数据库
                    val insertedCount = mDateAdapter.addJSON(eventJson, eventSyn)

                    qualityReport(insertedCount)
                    val msg =
                        if (insertedCount < 0) " Failed to insert the event " else " the event: $name  has been inserted to db，count = $insertedCount  "
//                LogUtils.json(TAG + msg, eventJson.toString())
                    //发送上报的message
                    Message.obtain().apply {
                        //上报标志
                        this.what = FLUSH_QUEUE
                        mWorker.runMessageOnce(this, 1000L)
                    }
                }

            }
        } catch (e: Exception) {
            LogUtils.i(TAG, "enqueueEventMessage error:$e")
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.UNKNOWN_TYPE,
                "event name: $name ," + e.stackTraceToString()
            )
        }
    }

    private fun qualityReport(insertedCount: Int) {
        if (insertedCount < 0) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.DATA_INSERT_ERROR,
                insertedCount.toString()
            )
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
     * 删除所有数据库内的事件
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
                LogUtils.i(TAG, "A task is currently uploading，or upload is disable")
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
        var eventsData:String?
        synchronized(mDateAdapter) {
            eventsData = mDateAdapter.generateDataString(
                Constant.EVENT_REPORT_SIZE
            )
        }

        if (eventsData == null||JSONArray(eventsData).length()==0) {
            LogUtils.d(TAG, "db count = 0，disable upload")
            mDateAdapter.enableUpload = true
            return
        }

        //如果未进行时间同步，发空参数进行时间同步
        if (TimeCalibration.TIME_NOT_VERIFY_VALUE == TimeCalibration.instance.getVerifyTimeAsync()) {
            LogUtils.d(TAG, "time do not calibrate yet")
            mDateAdapter.enableUpload = true
            TimeCalibration.instance.getReferenceTime()
            return
        }
        scope.launch(Dispatchers.IO) {
            //事件主体，json格式
            eventsData?.let {
                EventInfoCheckHelper.instance.correctEventTime(it) { info ->
                    launch(Dispatchers.Main) {
                        if (info.isNotEmpty()) {
                            mDateAdapter.enableUpload = false
                            //http 请求
                            uploadDataToNet(info,  mDateAdapter)
                        } else {
                            mDateAdapter.enableUpload = true
                        }
                    }
                }
            }
        }
    }

    private fun uploadDataToNet(
        event: String,
        mDateAdapter: EventDateAdapter
    ) {
        RequestHelper.Builder(
            HttpMethod.POST_ASYNC,
            Constant.EVENT_REPORT_URL
        )
            .jsonData(event)
            .retryCount(Constant.EVENT_REPORT_TRY_COUNT)
            .callback(object :
                HttpCallback.JsonCallback() {
                override fun onFailure(code: Int, errorMessage: String?) {
                    LogUtils.d(TAG, errorMessage)
                    ROIQueryQualityHelper.instance.reportQualityMessage(
                        ROIQueryErrorParams.REPORT_ERROR_ON_FAIL,
                        errorMessage
                    )
                }

                override fun onResponse(response: JSONObject?) {
                    LogUtils.json("$TAG upload event data ", event)
                    LogUtils.json("$TAG upload event result ", response)
                    if (response?.getInt(ResponseDataKey.KEY_CODE) == 0) {

                        deleteEventAfterReport(event, mDateAdapter)

                        //避免事件积压，成功后再次上报
                        flush(2000L)
                    } else {
                        val msg =
                            "error code: ${response?.getString(ResponseDataKey.KEY_CODE)}, msg: ${
                                response?.getString(ResponseDataKey.KEY_MSG)
                            }"
                        LogUtils.d(TAG, msg)
                        ROIQueryQualityHelper.instance.reportQualityMessage(
                            ROIQueryErrorParams.REPORT_ERROR_ON_RESPONSE,
                            msg
                        )
                    }
                }

                override fun onAfter() {
                    mDateAdapter.enableUpload = true
                }
            }).execute()
    }

    private fun deleteEventAfterReport(
        event: String,
        mDateAdapter: EventDateAdapter
    ) {
        //上报成功后删除本地数据
        try {
            val jsonArray = JSONArray(event)
            val length = jsonArray.length()
            for (i in 0 until length) {
                jsonArray.optJSONObject(i)?.let {
                    if (it.optString(EVENT_INFO_SYN).isNotEmpty()){
                        mDateAdapter.cleanupEvents(it.optString(EVENT_INFO_SYN))
                    }else{
                        mDateAdapter.cleanupEvents(it.optString(PRE_EVENT_INFO_SYN))
                    }
                }
            }
        } catch (e: Exception) {
            // 保证数据不会重复上传
            mDateAdapter.deleteAllEvents()
        } finally {
        }
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
                    ROIQueryQualityHelper.instance.reportQualityMessage(
                        ROIQueryErrorParams.UNKNOWN_TYPE,
                        e.stackTraceToString()
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