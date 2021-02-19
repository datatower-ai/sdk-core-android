package com.nodetower.analytics.api

import android.content.Context
import com.nodetower.analytics.Constant
import com.nodetower.analytics.config.AnalyticsConfigOptions
import com.nodetower.analytics.core.TrackTaskManagerThread
import com.nodetower.analytics.data.DataParams
import com.nodetower.analytics.data.EventDateAdapter
import com.nodetower.base.utils.LogUtils
import org.json.JSONObject

open class RoiqueryAnalyticsAPI : AbstractAnalyticsApi {

    //private
    internal constructor() : super()
    internal constructor(
        context: Context?,
        serverURL: String,
    ) : super(context, serverURL)


    override var maxCacheSize: Long
        get() = mConfigOptions.mMaxCacheSize
        set(value) {
            mConfigOptions.setMaxCacheSize(value)
        }

    override val isNetworkRequestEnable: Boolean
        get() = mEnableNetworkRequest

    override fun enableNetworkRequest(isRequest: Boolean) {
        mEnableNetworkRequest = isRequest
    }

    override fun setFlushNetworkPolicy(networkType: Int) {
        mConfigOptions.setNetworkTypePolicy(networkType)
    }

    override var flushInterval: Int
        get() = mConfigOptions.mFlushInterval
        set(value) {
            mConfigOptions.setFlushInterval(value)
        }

    override var flushBulkSize: Int
        get() = mConfigOptions.mFlushBulkSize
        set(value) {
            mConfigOptions.setFlushBulkSize(value)
        }

    override var accountId: String?
        get() = mContext?.let { EventDateAdapter.getInstance(it, mContext.packageName)?.accountId }
        set(value) {
            if (value != null) {
                mContext?.let { EventDateAdapter.getInstance(it, mContext.packageName)?.commitAccountId(
                    value
                ) }
                updateEventInfo("#acid", value)
            }
        }

    override fun getAppId(): String? {
        return mConfigOptions.mAppId
    }

    override fun track(eventName: String?, properties: JSONObject?) {
        if (!isEnableDataCollect()) {
            LogUtils.i(TAG, "event track disable")
            return
        }
        mTrackTaskManager?.let {
            it.addTrackEventTask(Runnable {
                try {
                    if (eventName != null) {
                        trackEvent(eventName, properties)
                    }
                } catch (e: Exception) {
                    LogUtils.printStackTrace(e)
                }
            })
        }
    }

    override fun trackAppClose(properties: JSONObject?) {
        track(Constant.PRESET_EVENT_APP_CLOSE, properties)
    }

    override fun trackPageOpen(properties: JSONObject?) {
        track(Constant.PRESET_EVENT_PAGE_OPEN, properties)
    }

    override fun trackPageClose(properties: JSONObject?) {
        track(Constant.PRESET_EVENT_PAGE_CLOSE, properties)
    }

    override fun trackAdClick(properties: JSONObject?) {
        track(Constant.PRESET_EVENT_AD_CLICK, properties)

    }

    override fun trackAdShow(properties: JSONObject?) {
        track(Constant.PRESET_EVENT_AD_SHOW, properties)

    }


    override val mainProcessName: String?
        get() = mainProcessName

    override fun flush() {
        mAnalyticsManager?.flush()
    }


    override val superProperties: JSONObject?
        get() = JSONObject()


    override fun deleteAll() {
        mAnalyticsManager?.deleteAll()
    }

    override fun stopTrackThread() {
        if (mTrackTaskManagerThread != null && !mTrackTaskManagerThread!!.isStopped) {
            mTrackTaskManagerThread!!.stop()
            LogUtils.i(TAG, "Data collection thread has been stopped")
        }
    }

    override fun startTrackThread() {
        if (mTrackTaskManagerThread == null || mTrackTaskManagerThread!!.isStopped) {
            mTrackTaskManagerThread = TrackTaskManagerThread()
            Thread(mTrackTaskManagerThread).start()
            LogUtils.i(TAG, "Data collection thread has been started")
        }
    }

    override fun enableDataCollect(enable:Boolean) {
        if (!mConfigOptions.isDataCollectEnable) {
            DataParams.getInstance()?.let {
                mContext!!.contentResolver.notifyChange(
                    it.dataCollectUri,
                    null
                )
            }
        }
        mConfigOptions.isDataCollectEnable = enable
        mTrackTaskManager!!.setDataCollectEnable(enable)
    }

    fun isEnableDataCollect() = mConfigOptions.isDataCollectEnable

    fun getServerUrl() = mServerUrl

    fun getFlushNetworkPolicy() =
        mConfigOptions.mNetworkTypePolicy

    fun isMultiProcessFlushData() =
        mConfigOptions.isSubProcessFlushData


    companion object {
        /**
         * 初始化 SDK
         *
         * @param context App 的 Context
         * @param saConfigOptions SDK 的配置项
         */
        @JvmStatic
        fun init(context: Context?, configOptions: AnalyticsConfigOptions?) {
            if (context == null || configOptions == null) {
                throw NullPointerException("Context、AnalyticsConfigOptions 不可以为 null")
            }
            mConfigOptions = configOptions
            val api = configOptions.mServerUrl?.let {
                getInstance(context, it)
            }
            api?.let {
                if (!it.mSDKConfigInit) {
                    it.applySAConfigOptions()
                }
            }
        }

        private fun getInstance(
            context: Context?,
            serverURL: String,
        ): RoiqueryAnalyticsAPI {
            if (null == context) {
                return RoiqueryAnalyticsEmptyImplementation()
            }
            synchronized(S_INSTANCE_MAP) {
                val appContext = context.applicationContext
                var instance = S_INSTANCE_MAP[appContext]
                if (null == instance) {
                    instance = RoiqueryAnalyticsAPI(appContext, serverURL)
                    S_INSTANCE_MAP[appContext] = instance
                }
                return instance
            }
        }

        /**
         * 获取 单例
         *
         * @param context App的Context
         * @return DataAPI 单例
         */
        @JvmStatic
        fun getInstance(context: Context?): RoiqueryAnalyticsAPI {
            if (null == context) {
                return RoiqueryAnalyticsEmptyImplementation()
            }
            synchronized(S_INSTANCE_MAP) {
                val appContext = context.applicationContext
                val instance = S_INSTANCE_MAP[appContext]
                if (null == instance) {
                    LogUtils.i(
                        TAG,
                        "The static method sharedInstance(context, serverURL, debugMode) should be called before calling sharedInstance()"
                    )
                    return RoiqueryAnalyticsEmptyImplementation()
                }
                return instance
            }
        }

    }
}