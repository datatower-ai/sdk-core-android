package ai.datatower.analytics.config

import ai.datatower.analytics.BuildConfig
import ai.datatower.analytics.Constant
import ai.datatower.analytics.api.AnalyticsImp
import ai.datatower.analytics.data.persistence.SharedPreferencesLoader
import ai.datatower.analytics.data.persistence.StorageDisableFlag
import ai.datatower.analytics.data.persistence.StorageReportUrl
import ai.datatower.analytics.utils.LogUtils
import ai.datatower.analytics.utils.PresetEvent
import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import java.util.concurrent.Future


class AnalyticsConfig
/**
 * 私有构造函数
 */
private constructor() : AbstractAnalyticsConfig() {

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            AnalyticsConfig()
        }
    }

    private val configPreferenceName = "datatower.android.config"
    private val sPrefsLoader: SharedPreferencesLoader = SharedPreferencesLoader()

    private var sdkDisableStorage: StorageDisableFlag? = null
    private var reportUrlStorage: StorageReportUrl? = null

    private var sdkDisable: Boolean = false
    private var reportUrl: String? = null

    @Volatile
    private var hasGetRemoteConfig = false

    @Volatile
    private var isFetching = false

    fun getRemoteConfig() {
        // NOTE: Feature disabled for now, may be enabled in the future.
        /*
        Thread {
            if (hasGetRemoteConfig) {
                return@Thread
            }

            if (isFetching) {
                return@Thread
            }

            PerfLogger.doPerfLog(PerfAction.GETCONFIGBEGIN, System.currentTimeMillis())

            isFetching = true

            initRemoteConfig()

            try {
                val response = RequestHelper.Builder(HttpMethod.GET_SYNC, configUrl)
                    .params(mutableMapOf<String, String>().apply {
                        put("app_id", mAppId ?: "")
                        put("sdk_version", getSDKVersion())
                        put("sdk_type", getSDKType())
                        put("os", Constant.SDK_TYPE_ANDROID)
                    })
                    .retryCount(Constant.EVENT_REPORT_TRY_COUNT)
                    .executeSync()

                if (!hasGetRemoteConfig && response != null && response.code == 200 && !TextUtils.isEmpty(response.result)) {
                    val responseJson = JSONObject(response.result)
                    if (responseJson.has("is_off")) {
                        val isOff = responseJson.getBoolean("is_off")
                        sdkDisableStorage?.put(isOff)
                        sdkDisable = isOff
                        LogUtils.d(Constant.LOG_TAG,"sdk disable: $isOff")
                    }
                    if (responseJson.has("report_url")) {
                        val url = responseJson.getString("report_url")
                        reportUrlStorage?.put(url)
                        reportUrl = if (!TextUtils.isEmpty(url)) url else mServerUrl
                    }
                    hasGetRemoteConfig = true
                }
            } catch (ignored: Exception) {
            }
            isFetching = false
            PerfLogger.doPerfLog(PerfAction.GETCONFIGEND, System.currentTimeMillis())
        }.start()
         */
    }

    private fun initRemoteConfig(){
        val storedSharedPrefs: Future<SharedPreferences> =
            sPrefsLoader.loadPreferences(
                mContext,
                configPreferenceName
            )
        //默认为true
        sdkDisableStorage = StorageDisableFlag(storedSharedPrefs)
        //默认为空
        reportUrlStorage = StorageReportUrl(storedSharedPrefs)

        sdkDisable = sdkDisableStorage?.get() ?: false
        val pReportUrl = reportUrlStorage?.get()
        reportUrl = if (pReportUrl != null && pReportUrl.isNotEmpty()){
            pReportUrl
        }else {
            mServerUrl
        }
    }

    fun isSdkDisable() = sdkDisable

    fun reportUrl(): String = reportUrl ?: mServerUrl

    fun setContext(context: Context): AnalyticsConfig {
        mContext = context
        return this
    }

    /**
     * 设置调试模式
     *
     * @param isDebug，是否开启debug
     * @param logLevel，log打印级别
     * @return OptionsConfig
     */
    @JvmOverloads
    fun setDebug(isDebug: Boolean, logLevel: Int = LogUtils.V): AnalyticsConfig {
        mEnabledDebug = isDebug
        mLogLevel = logLevel
        return this
    }

    fun setServerUrl(serverUrl: String): AnalyticsConfig {
        mServerUrl = serverUrl
        return this
    }

    /**
     * 设置两次数据发送的最小时间间隔，最小值 5 秒
     *
     * @param flushInterval 时间间隔，单位毫秒
     * @return OptionsConfig
     */
    fun setFlushInterval(flushInterval: Int): AnalyticsConfig {
        mFlushInterval = Math.max(5 * 1000, flushInterval)
        return this
    }

    /**
     * 设置本地缓存日志的最大条目数
     *
     * @param flushBulkSize 缓存数目
     * @return SAOptionsConfig
     */
    fun setFlushBulkSize(flushBulkSize: Int): AnalyticsConfig {
        mFlushBulkSize = Math.max(50, flushBulkSize)
        return this
    }

    /**
     * 设置本地缓存上限值，单位 byte，默认为 32MB：32 * 1024 * 1024，最小 16MB：16 * 1024 * 1024，若小于 16MB，则按 16MB 处理。
     *
     * @param maxCacheSize 单位 byte
     * @return SAOptionsConfig
     */
    fun setMaxCacheSize(maxCacheSize: Long): AnalyticsConfig {
        mMaxCacheSize = Math.max((16 * 1024 * 1024).toLong(), maxCacheSize)
        return this
    }


    /**
     * 设置数据的网络上传策略
     *
     * @param networkTypePolicy 数据的网络上传策略
     * @return OptionsConfig
     */
    fun setNetworkTypePolicy(networkTypePolicy: Int): AnalyticsConfig {
        mNetworkTypePolicy = networkTypePolicy
        return this
    }

    /**
     * app ID
     *
     * @param appId  ID
     * @return OptionsConfig
     */
    fun setAppId(appId: String?): AnalyticsConfig {
        mAppId = appId
        return this
    }

    /**
     * 禁用数据采集
     *
     * @return NTConfigOptions
     */
    fun enableTrack(enable: Boolean): AnalyticsConfig {
        mEnableTrack = enable
        return this
    }

    /**
     * 禁用数据上报
     *
     * @return NTConfigOptions
     */
    fun enableUpload(enable: Boolean): AnalyticsConfig {
        mEnableUpload = enable
        return this
    }

    /**
     * 设置渠道
     *
     * @return NTConfigOptions
     */
    fun setChannel(channel: String): AnalyticsConfig {
        mChannel = channel
        return this
    }

    /**
     * 是否上报数据
     */
    fun setManualEnableUpload(manualEnableUpload: Boolean): AnalyticsConfig {
        mManualUploadSwitch.set(!manualEnableUpload)
        return this
    }

    fun enableUpload() {
        if (mManualUploadSwitch.get()) {
            LogUtils.w("Track is already enabled, duplicated enableTrack() will take no effects!")
            return
        }
        LogUtils.d("Manually enabled upload!")
        mManualUploadSwitch.set(true)
        AnalyticsImp.getInstance().flush()
    }

    fun enableAutoTrack(event: PresetEvent) {
        event.enable()
    }

    fun disableAutoTrack(event: PresetEvent) {
        event.disable()
    }

    /**
     * 增加预置属性
     */
    fun addCommonProperties(commonProperties: JSONObject): AnalyticsConfig {
        mCommonProperties = commonProperties
        return this
    }

    fun getSDKVersion():String {
        mCommonProperties?.let {
            if (it.has(Constant.COMMON_PROPERTY_SDK_VERSION)) {
                return it.getString(Constant.COMMON_PROPERTY_SDK_VERSION)
            }
        }
        return BuildConfig.VERSION_NAME
    }

    fun getSDKType():String {
        mCommonProperties?.let {
            if (it.has(Constant.COMMON_PROPERTY_SDK_TYPE)) {
                return it.getString(Constant.COMMON_PROPERTY_SDK_TYPE)
            }
        }
        return Constant.SDK_TYPE_ANDROID
    }

}
