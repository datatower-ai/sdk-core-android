package com.roiquery.analytics.config

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.roiquery.analytics.BuildConfig
import com.roiquery.analytics.Constant
import com.roiquery.analytics.core.PropertyManager
import com.roiquery.analytics.data.persistence.SharedPreferencesLoader
import com.roiquery.analytics.data.persistence.StorageDisableFlag
import com.roiquery.analytics.data.persistence.StorageReportUrl
import com.roiquery.analytics.network.HttpCallback
import com.roiquery.analytics.network.HttpMethod
import com.roiquery.analytics.network.RequestHelper
import com.roiquery.analytics.utils.AppInfoUtils
import com.roiquery.analytics.utils.LogUtils
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
    private val configUrl = "https://test.roiquery.com/sdk/cfg"
    private val sPrefsLoader: SharedPreferencesLoader = SharedPreferencesLoader()

    private var sdkDisableStorage: StorageDisableFlag? = null
    private var reportUrlStorage: StorageReportUrl? = null

    private var sdkDisable: Boolean = false
    private lateinit var reportUrl: String

    @Volatile
    private var hasGetRemoteConfig = false

    fun getRemoteConfig() {
        if (hasGetRemoteConfig) {
            return
        }
        initRemoteConfig()
        Thread {
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
            } catch (e: Exception) {

            }
        }.start()
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

    fun reportUrl() = reportUrl

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