package ai.datatower.analytics.config

import android.content.Context
import ai.datatower.analytics.utils.LogUtils
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
    var mNetworkTypePolicy: Int = 0

    /**
     * mAppId
     */
    lateinit var mContext: Context

    /**
     * mAppId
     */
    var mAppId: String? = null


    /**
     * mServerUrl
     */
    var mServerUrl: String = ""


    /**
     * 数据采集，默认开启数据采集
     */
    var mEnableTrack = true

    /**
     * 数据上报，默认开启数据上报
     */
    var mEnableUpload = true

    /**
     * 推广渠道
     */
    var mChannel = ""

    /**
     * 设置公共属性
     */
    var mCommonProperties: JSONObject? = null
}