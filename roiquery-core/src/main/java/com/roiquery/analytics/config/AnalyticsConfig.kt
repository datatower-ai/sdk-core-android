package com.roiquery.analytics.config

import com.roiquery.analytics.utils.LogUtils
import org.json.JSONObject


class AnalyticsConfig : AbstractAnalyticsConfig {

    /**
     * 私有构造函数
     */
    private constructor()

    /**
     * 获取 OptionsConfig 实例
     *
     * @param serverUrl，数据上报服务器地址
     */
    constructor(appId: String?) {
        mAppId = appId
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
     * 增加预置属性
     */

    fun addCommonProperties(commonProperties: JSONObject): AnalyticsConfig {
        mCommonProperties = commonProperties
        return this
    }

}