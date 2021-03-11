package com.roiquery.analytics.config

import com.roiquery.analytics.utils.LogUtils
import com.roiquery.analytics.utils.NetworkType
import org.json.JSONObject

abstract class AbstractAnalyticsConfig {


    /**
     * 两次数据发送的最小时间间隔，单位毫秒
     */
    var mFlushInterval = 0

    /**
     * 允许本地缓存日志的最大条目数，即超过后立即上报
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
     * 数据采集，默认开启数据采集
     */
    var mEnableTrack = true

    /**
     * 数据上报，默认开启数据上报
     */
    var mEnableUpload = true

    /**
     * 设置公共属性
     */
    var mCommonProperties: JSONObject? = null
}