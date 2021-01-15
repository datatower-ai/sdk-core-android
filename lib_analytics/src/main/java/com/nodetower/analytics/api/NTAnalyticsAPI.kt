package com.nodetower.analytics.api

import android.content.Context
import com.nodetower.analytics.config.AnalyticsConfigOptions
import com.nodetower.base.utils.LogUtils
import com.nodetower.base.utils.NetworkType
import org.json.JSONObject
import java.util.concurrent.TimeUnit

open class NTAnalyticsAPI : AbstractAnalyticsApi {

    //private
    internal constructor() : super()
    internal constructor(
        context: Context?,
        serverURL: String,
        debugMode: DebugMode
    ) : super(context, serverURL, debugMode)


    override fun enableLog(enable: Boolean) {
        TODO("Not yet implemented")
    }

    override var maxCacheSize: Long
        get() = TODO("Not yet implemented")
        set(value) {}
    override val isDebugMode: Boolean
        get() = TODO("Not yet implemented")
    override val isNetworkRequestEnable: Boolean
        get() = TODO("Not yet implemented")

    override fun enableNetworkRequest(isRequest: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setFlushNetworkPolicy(networkType: Int) {
        TODO("Not yet implemented")
    }

    override var flushInterval: Int
        get() = TODO("Not yet implemented")
        set(value) {}
    override var flushBulkSize: Int
        get() = TODO("Not yet implemented")
        set(value) {}
    override var sessionIntervalTime: Int
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun getAccountId(): String? {
        TODO("Not yet implemented")
    }

    override fun getAppId(): String? {
        TODO("Not yet implemented")
    }

    override fun trackAppInstall(properties: JSONObject?) {
        TODO("Not yet implemented")
    }

    override fun trackAppInstall() {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun removeTimer(eventName: String?) {
        TODO("Not yet implemented")
    }

    override fun trackTimerBegin(eventName: String?) {
        TODO("Not yet implemented")
    }

    override fun trackTimerBegin(eventName: String?, timeUnit: TimeUnit?) {
        TODO("Not yet implemented")
    }

    override fun trackTimerEnd(eventName: String?, properties: JSONObject?) {
        TODO("Not yet implemented")
    }

    override fun trackTimerEnd(eventName: String?) {
        TODO("Not yet implemented")
    }

    override fun clearTrackTimer() {
        TODO("Not yet implemented")
    }

    override fun clearReferrerWhenAppEnd() {
        TODO("Not yet implemented")
    }

    override val mainProcessName: String?
        get() = TODO("Not yet implemented")

    override fun flush() {
        TODO("Not yet implemented")
    }

    override fun flushSync() {
        TODO("Not yet implemented")
    }

    override val superProperties: JSONObject?
        get() = TODO("Not yet implemented")

    override fun registerSuperProperties(superProperties: JSONObject?) {
        TODO("Not yet implemented")
    }

    override fun unregisterSuperProperty(superPropertyName: String?) {
        TODO("Not yet implemented")
    }

    override fun clearSuperProperties() {
        TODO("Not yet implemented")
    }

    override fun trackTimerStart(eventName: String?): String? {
        TODO("Not yet implemented")
    }

    override fun trackTimerPause(eventName: String?) {
        TODO("Not yet implemented")
    }

    override fun trackTimerResume(eventName: String?) {
        TODO("Not yet implemented")
    }

    override fun setCookie(cookie: String?, encode: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getCookie(decode: Boolean): String? {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun stopTrackThread() {
        TODO("Not yet implemented")
    }

    override fun startTrackThread() {
        TODO("Not yet implemented")
    }

    override fun enableDataCollect() {
        TODO("Not yet implemented")
    }

    override fun getScreenOrientation(): String? {
        TODO("Not yet implemented")
    }


    fun getServerUrl() = mServerUrl

    fun getFlushNetworkPolicy() =
        if (mConfigOptions != null) mConfigOptions!!.mNetworkTypePolicy else NetworkType.TYPE_NONE

    fun isMultiProcessFlushData() =
        if (mConfigOptions != null) mConfigOptions!!.isSubProcessFlushData else false


    companion object {
        // 可视化埋点功能最低 API 版本
        const val VTRACK_SUPPORTED_MIN_API = 16
//
//        /**
//         * 获取 SDK 的版本号
//         * @return SDK 的版本号
//         */
//        // SDK 版本，此属性插件会进行访问，谨慎修改
//        val sDKVersion = BuildConfig.SDK_VERSION
//            get() = Companion.field
//
//        // 此属性插件会进行访问，谨慎删除。当前 SDK 版本所需插件最低版本号，设为空，意为没有任何限制
//        const val MIN_PLUGIN_VERSION = BuildConfig.MIN_PLUGIN_VERSION

        /**
         * 插件版本号，插件会用到此属性，请谨慎修改
         */
        var ANDROID_PLUGIN_VERSION = ""

        /**
         * 获取 SensorsDataAPI 单例
         *
         * @param context App的Context
         * @return SensorsDataAPI 单例
         */
        fun sharedInstance(context: Context?): NTAnalyticsAPI {
            if (null == context) {
                return NTAnalyticsEmptyImplementation()
            }
            synchronized(sInstanceMap) {
                val appContext = context.applicationContext
                val instance = sInstanceMap[appContext]
                if (null == instance) {
                    LogUtils.i(
                        TAG,
                        "The static method sharedInstance(context, serverURL, debugMode) should be called before calling sharedInstance()"
                    )
                    return NTAnalyticsEmptyImplementation()
                }
                return instance
            }
        }

        /**
         * 初始化 SDK
         *
         * @param context App 的 Context
         * @param saConfigOptions SDK 的配置项
         */
        fun startWithConfigOptions(context: Context?, configOptions: AnalyticsConfigOptions?) {
            if (context == null || configOptions == null) {
                throw NullPointerException("Context、AnalyticsConfigOptions 不可以为 null")
            }
            mConfigOptions = configOptions
            val sensorsDataAPI = configOptions.mServerUrl?.let {
                getInstance(context, it, DebugMode.DEBUG_OFF)
            }
            sensorsDataAPI?.let {
                if (!it.mSDKConfigInit) {
                    it.applySAConfigOptions()
                }
            }

        }

        private fun getInstance(
            context: Context?,
            serverURL: String,
            debugMode: DebugMode
        ): NTAnalyticsAPI {
            if (null == context) {
                return NTAnalyticsEmptyImplementation()
            }
            synchronized(sInstanceMap) {
                val appContext = context.applicationContext
                var instance = sInstanceMap[appContext]
                if (null == instance) {
                    instance = NTAnalyticsAPI(appContext, serverURL, debugMode)
                    sInstanceMap[appContext] = instance
                }
                return instance
            }
        }

    }
}