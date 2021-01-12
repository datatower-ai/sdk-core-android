package com.nodetower.analytics.config

import com.nodetower.analytics.utils.NetworkType
import javax.net.ssl.SSLSocketFactory

abstract class AbstractAnalyticsConfigOptions {

    /**
     * 请求配置地址，默认从 ServerUrl 解析
     */
    var mRemoteConfigUrl: String? = null

    /**
     * 远程配置请求最小间隔时长，单位：小时，默认 24
     */
    var mMinRequestInterval = 24

    /**
     * 远程配置请求最大间隔时长，单位：小时，默认 48
     */
    var mMaxRequestInterval = 48

    /**
     * 禁用随机时间请求远程配置
     */
    var mDisableRandomTimeRequestRemoteConfig = false

    /**
     * 设置 SSLSocketFactory
     */
    var mSSLSocketFactory: SSLSocketFactory? = null

    /**
     * 数据上报服务器地址
     */
    var mServerUrl: String? = null

    /**
     * AutoTrack 类型
     */
    var mAutoTrackEventType = 0

    /**
     * 是否开启 TrackAppCrash
     */
    var mEnableTrackAppCrash = false

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
     * 点击图是否可用
     */
    var mHeatMapEnabled = false

    /**
     * 点击图对话框是否可用
     */
    var mHeatMapConfirmDialogEnabled = false

    /**
     * 可视化全埋点是否可用
     */
    var mVisualizedEnabled = false

    /**
     * 可视化全埋点对话框是否可用
     */
    var mVisualizedConfirmDialogEnabled = false

    /**
     * 是否开启打印日志
     */
    var mLogEnabled = false

    /**
     * 开启 RN 采集
     */
    var mRNAutoTrackEnabled = false

    /**
     * 采集屏幕方向
     */
    var mTrackScreenOrientationEnabled = false

    /**
     * 网络上传策略
     */
    var mNetworkTypePolicy: Int =
        NetworkType.TYPE_3G or NetworkType.TYPE_4G or NetworkType.TYPE_WIFI or NetworkType.TYPE_5G

    /**
     * AnonymousId，匿名 ID
     */
    var mAnonymousId: String? = null

    /**
     * 是否使用上次启动时保存的 utm 属性.
     */
    var mEnableSaveDeepLinkInfo = false

    /**
     * 是否自动进行 H5 打通
     */
    var isAutoTrackWebView = false

    /**
     * WebView 是否支持 JellyBean
     */
    var isWebViewSupportJellyBean = false

    /**
     * 是否在手动埋点事件中自动添加渠道匹配信息
     */
    var isAutoAddChannelCallbackEvent = false

    /**
     * 是否开启多渠道匹配，开启后 trackInstallation 中由 profile_set_once 操作改为 profile_set 。
     */
    var mEnableMultipleChannelMatch = false

    /**
     * 是否子进程上报数据
     */
    var isSubProcessFlushData = false

    /**
     * 是否开启加密
     */
    var mEnableEncrypt = false

    /**
     * 密钥存储相关接口
     */
//    IPersistentSecretKey mPersistentSecretKey;

    /**
     * 密钥存储相关接口
     */
    //    IPersistentSecretKey mPersistentSecretKey;
    /**
     * 关闭数据采集，默认开启数据采集
     */
    var isDataCollectEnable = true

}