package com.nodetower.analytics.config

import com.nodetower.base.utils.LogUtils
import com.nodetower.base.utils.NetworkType
import javax.net.ssl.SSLSocketFactory

abstract class AbstractAnalyticsConfigOptions {

    /**
     * 数据上报服务器地址
     */
    var mServerUrl: String? = null

    /**
     * 两次数据发送的最小时间间隔，单位毫秒
     */
    var mFlushInterval = 0

    /**
     * 本地缓存日志的最大条目数
     */
    var mFlushBulkSize = 0

    /**
     * 本地缓存上限值，单位 byte，默认为 32MB：32 * 1024 * 1024
     */
    var mMaxCacheSize = 32 * 1024 * 1024L

    /**
     * 是否开启debug
     */
    var mEnabledDebug = false

    /**
     * 打印日志的级别
     */
    var mLogLevel  = LogUtils.V

    /**
     * 网络上传策略
     */
    var mNetworkTypePolicy: Int =
        NetworkType.TYPE_3G or NetworkType.TYPE_4G or NetworkType.TYPE_WIFI or NetworkType.TYPE_5G

    /**
     * mAppId
     */
    var mAppId: String? = null

    /**
     * 是否子进程上报数据
     */
    var isSubProcessFlushData = false

    /**
     * 关闭数据采集，默认开启数据采集
     */
    var isDataCollectEnable = true

}