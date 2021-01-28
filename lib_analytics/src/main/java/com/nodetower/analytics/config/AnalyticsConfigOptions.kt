package com.nodetower.analytics.config

import javax.net.ssl.SSLSocketFactory


class AnalyticsConfigOptions : AbstractAnalyticsConfigOptions {

    /**
     * 私有构造函数
     */
    private constructor()

    /**
     * 获取 SAOptionsConfig 实例
     *
     * @param serverUrl，数据上报服务器地址
     */
     constructor(appId: String?,serverUrl: String?) {
        mAppId = appId
        mServerUrl = serverUrl
    }


    /**
     * 设置数据上报地址
     *
     * @param serverUrl，数据上报地址
     * @return SAOptionsConfig
     */
    fun setServerUrl(serverUrl: String?): AnalyticsConfigOptions {
        mServerUrl = serverUrl
        return this
    }



    /**
     * 设置两次数据发送的最小时间间隔，最小值 5 秒
     *
     * @param flushInterval 时间间隔，单位毫秒
     * @return SAOptionsConfig
     */
    fun setFlushInterval(flushInterval: Int): AnalyticsConfigOptions {
        mFlushInterval = Math.max(5 * 1000, flushInterval)
        return this
    }

    /**
     * 设置本地缓存日志的最大条目数
     *
     * @param flushBulkSize 缓存数目
     * @return SAOptionsConfig
     */
    fun setFlushBulkSize(flushBulkSize: Int): AnalyticsConfigOptions {
        mFlushBulkSize = Math.max(50, flushBulkSize)
        return this
    }

    /**
     * 设置本地缓存上限值，单位 byte，默认为 32MB：32 * 1024 * 1024，最小 16MB：16 * 1024 * 1024，若小于 16MB，则按 16MB 处理。
     *
     * @param maxCacheSize 单位 byte
     * @return SAOptionsConfig
     */
    fun setMaxCacheSize(maxCacheSize: Long): AnalyticsConfigOptions {
        mMaxCacheSize = Math.max((16 * 1024 * 1024).toLong(), maxCacheSize)
        return this
    }

    /**
     * 是否打印日志
     *
     * @param enableLog 是否开启打印日志
     * @return SAOptionsConfig
     */
    fun enableLog(enableLog: Boolean): AnalyticsConfigOptions {
        mLogEnabled = enableLog
        return this
    }

    /**
     * 设置数据的网络上传策略
     *
     * @param networkTypePolicy 数据的网络上传策略
     * @return SAOptionsConfig
     */
    fun setNetworkTypePolicy(networkTypePolicy: Int): AnalyticsConfigOptions {
        mNetworkTypePolicy = networkTypePolicy
        return this
    }

    /**
     * app ID
     *
     * @param appId 匿名 ID
     * @return SAOptionsConfig
     */
    fun setAppId(appId: String?): AnalyticsConfigOptions {
        mAppId = appId
        return this
    }

    /**
     * 禁用数据采集
     *
     * @return NTConfigOptions
     */
    fun disableDataCollect(): AnalyticsConfigOptions {
        isDataCollectEnable = false
        return this
    }

    /**
     * 设置 SSLSocketFactory，HTTPS 请求连接时需要使用
     *
     * @param SSLSocketFactory 证书
     * @return NTConfigOptions
     */
    fun setSSLSocketFactory(SSLSocketFactory: SSLSocketFactory?): AnalyticsConfigOptions {
        mSSLSocketFactory = SSLSocketFactory
        return this
    }

}