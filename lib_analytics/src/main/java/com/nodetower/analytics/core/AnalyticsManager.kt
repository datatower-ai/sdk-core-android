package com.nodetower.analytics.core

import android.content.Context
import android.os.*
import android.text.TextUtils
import com.nodetower.analytics.Constant
import com.nodetower.analytics.api.RoiqueryAnalyticsAPI
import com.nodetower.analytics.data.DateAdapter
import com.nodetower.analytics.data.DataParams
import com.nodetower.base.network.HttpCallback
import com.nodetower.base.network.HttpMethod
import com.nodetower.base.network.RequestHelper
import com.nodetower.base.utils.LogUtils
import com.nodetower.base.utils.NetworkUtils.isNetworkAvailable
import com.nodetower.base.utils.NetworkUtils.isShouldFlush
import com.nodetower.base.utils.NetworkUtils.networkType
import org.json.JSONObject
import kotlin.collections.HashMap


/**
 * 管理内部事件采集、上报
 */
class AnalyticsManager private constructor(
    var mContext: Context,
    var mAnalyticsDataAPI: RoiqueryAnalyticsAPI
) {
    private val mWorker: Worker = Worker()
    private val mDateAdapter: DateAdapter? = DateAdapter.getInstance()


    fun enqueueEventMessage(name: String, eventJson: JSONObject) {
        try {
            if (mDateAdapter == null) return
            synchronized(mDateAdapter) {
                //插入数据库
                val ret = mDateAdapter.addJSON(eventJson)
                val msg =
                    if (ret < 0) " Failed to insert the event " else " the event: $name  has been inserted to db，count = $ret  "
                LogUtils.json(TAG + msg, eventJson.toString())
                //发送上报的message
                Message.obtain().apply {
                    //上报标志
                    this.what = FLUSH_QUEUE
                    // 立即发送：有特殊事件需要立即上报、库存已满（无法插入）、超过允许本地缓存日志的最大条目数
                    if (isNeedFlushImmediately(name)
                        || ret == DataParams.DB_OUT_OF_MEMORY_ERROR
                        || ret > mAnalyticsDataAPI.flushBulkSize
                    ) {
                        mWorker.runMessage(this)
                    } else {
                        //不立即上报，有时间间隔
                        mWorker.runMessageOnce(this, mAnalyticsDataAPI.flushInterval.toLong())
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
     * 是否需要立即上报
     */
    private fun isNeedFlushImmediately(eventName: String): Boolean {
        return eventName == Constant.PRESET_EVENT_APP_OPEN
                || eventName == Constant.PRESET_EVENT_APP_FIRST_OPEN
                || eventName == Constant.PRESET_EVENT_APP_ATTRIBUTE
                || eventName == Constant.PRESET_EVENT_APP_CLOSE
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
            if (!mAnalyticsDataAPI.isNetworkRequestEnable) {
                LogUtils.i(TAG, "NetworkRequest disable，disable upload！")
                return false
            }
            if (TextUtils.isEmpty(mAnalyticsDataAPI.getServerUrl())) {
                LogUtils.i(TAG, "Server url is null or empty，disable upload")
                return false
            }
            //无网络
            if (!isNetworkAvailable(mContext)) {
                LogUtils.d(TAG,"NetworkAvailable，disable upload")
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
                if (mDateAdapter?.isSubProcessFlushing == true) {
                    return false
                }
                DateAdapter.getInstance()!!.commitSubProcessFlushState(true)
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
    private fun uploadData() {
        //不上报数据
        if (!enableUploadData()) return
        //这里每次只发送一条,后续可以考虑一次上报多条数据
        if (mDateAdapter == null) return
        var eventsData: Array<String>?
        synchronized(mDateAdapter){
           eventsData = mDateAdapter.generateDataString(
               DataParams.TABLE_EVENTS,
               Constant.EVENT_REPORT_SIZE
           )
        }

        if (eventsData == null) {
            mDateAdapter.commitSubProcessFlushState(false)
            LogUtils.d(TAG,"db count = 0，disable upload")
            return
        }
        LogUtils.json(TAG , "event uploading")
        //列表最后一条数据的id，删除时根据此id <= 进行删除
        val lastId = eventsData!![0]
        //事件主体，json格式
        val event = eventsData!![1]

        //http 请求
        RequestHelper.Builder(
            HttpMethod.POST,
            mAnalyticsDataAPI.getServerUrl() + Constant.EVENT_REPORT_URL
        )
            .jsonData(event)
            .retryCount(3)
            .callback(object : HttpCallback.StringCallback() {
                override fun onFailure(code: Int, errorMessage: String?) {
                    LogUtils.d(TAG, errorMessage)
                }

                override fun onResponse(response: String?) {
                    LogUtils.json("$TAG upload event result  ", response)
                    if (!response.isNullOrBlank() && JSONObject(response).get("code") == 0) {
                        //上报成功后删除本地数据
                        var leftCount = mDateAdapter.cleanupEvents(lastId)
                        LogUtils.d(TAG,"db left count = $leftCount")
                        //避免事件积压
                        flush(mAnalyticsDataAPI.flushInterval.toLong())
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
        private const val TAG = "AnalyticsManager"
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
    }

}