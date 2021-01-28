package com.nodetower.analytics.api

import android.content.Context
import com.nodetower.analytics.config.AnalyticsConfigOptions
import com.nodetower.analytics.data.DbAdapter
import com.nodetower.base.utils.LogUtils
import org.json.JSONObject

open class RoiqueryAnalyticsAPI : AbstractAnalyticsApi {

    //private
    internal constructor() : super()
    internal constructor(
        context: Context?,
        serverURL: String,
    ) : super(context, serverURL)


    override fun enableLog(enable: Boolean) {
        LogUtils.setEnableLog(enable)
    }

    override var maxCacheSize: Long
        get() = mConfigOptions.mMaxCacheSize

        set(value) {
            mConfigOptions.setMaxCacheSize(value)
        }

    override val isNetworkRequestEnable: Boolean
        get() = mEnableNetworkRequest

    override fun enableNetworkRequest(isRequest: Boolean) {
        mEnableNetworkRequest = isNetworkRequestEnable
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

    override var sessionIntervalTime: Int
        get() = mSessionTime
        set(value) {
            mSessionTime = value
        }

    override fun getAccountId(): String? {
        if (mContext != null) {
            return DbAdapter.getInstance(mContext,mContext.packageName)?.loginId
        }
        return ""
    }

    override fun getAppId(): String? {
        return mConfigOptions.mAppId
    }

    override fun trackAppInstall(properties: JSONObject?) {

    }

    override fun trackAppInstall() {

    }

    override fun track(eventName: String?, properties: JSONObject?) {
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

    override fun track(eventName: String?) {

    }

    override fun clearReferrerWhenAppEnd() {

    }

    override val mainProcessName: String?
        get() =  ""

    override fun flush() {
        mAnalyticsManager?.flush()
    }

    override fun flushSync() {

    }

    override val superProperties: JSONObject?
        get() = JSONObject()

    override fun setCookie(cookie: String?, encode: Boolean) {
    }

    override fun getCookie(decode: Boolean): String? {
        return ""
    }

    override fun deleteAll() {

    }

    override fun stopTrackThread() {

    }

    override fun startTrackThread() {

    }

    override fun enableDataCollect() {

    }

    override fun getScreenOrientation(): String? {
        return ""
    }


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