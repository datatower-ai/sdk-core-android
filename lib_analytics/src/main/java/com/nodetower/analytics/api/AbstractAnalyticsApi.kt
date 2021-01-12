package com.nodetower.analytics.api

import android.content.Context
import android.text.TextUtils
import com.nodetower.analytics.utils.DataHelper.assertKey
import com.nodetower.analytics.utils.DataHelper.assertPropertyTypes
import com.nodetower.analytics.utils.DataUtils
import com.nodetower.base.utils.DeviceUtils
import com.nodetower.base.utils.LogUtils
import org.json.JSONException
import org.json.JSONObject
import java.security.SecureRandom
import java.util.*


abstract class AbstractAnalyticsApi : IAnalyticsApi {

    private var mContext: Context? = null

    /* Debug 模式选项 */
    protected var mDebugMode: DebugMode = DebugMode.DEBUG_OFF

    /* AndroidID */
    protected var mAndroidId: String? = null

    /* 事件信息，包含事件的基本数据 */
    protected var mEventInfo: Map<String, Any>? = null

    /* 事件通用属性 */
    protected var mCommonProperties: Map<String, Any>? = null
    // SDK版本
//    val VERSION: String = BuildConfig.SDK_VERSION

    constructor(
        context: Context,
        serverUrl: String?,
        debugMode: DebugMode
    ) {
        mContext = context
        setDebugMode(debugMode)

        mAndroidId = DeviceUtils.getAndroidID(mContext!!)
        mEventInfo = setupEventInfo()
        mCommonProperties = setupCommonProperties()
    }

    private constructor() {
        mContext = null
    }

    override fun setServerUrl(serverUrl: String?) {

    }


    protected fun trackEvent(
        eventName: String,
        properties: JSONObject? = null
    ) {
        try {
            //检查事件名
            assertKey(eventName)
            //检查事件属性
            assertPropertyTypes(properties)

            try {
                //设置事件的基本信息
                val eventInfo = JSONObject(mEventInfo).apply {
                    put("#event_time", System.currentTimeMillis())
                    put("#event_name", eventName)
                    put("#event_syn", SecureRandom().nextInt())
                }

                //设置事件属性
                val eventProperties = JSONObject(mCommonProperties).apply {
                    // 屏幕方向
                    val screenOrientation: String? = getScreenOrientation()
                    if (!screenOrientation.isNullOrEmpty()) {
                        put("#screen_orientation", screenOrientation)
                    }
                    //合并用户自定义属性和通用属性
                    DataUtils.mergeJSONObject(properties, this)
                }
                //设置事件属性
                eventInfo.put("properties", eventProperties)


            } catch (e: JSONException) {
                LogUtils.printStackTrace(e)
            }
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
    }

    /**
     * 获取并配置 事件一些基本属性
     *
     * @return
     */
    protected open fun setupEventInfo(): Map<String, Any>? =
        Collections.unmodifiableMap(HashMap<String, Any>().apply {
            put("#did",DeviceUtils.getAndroidID(mContext!!)!!)//设备 ID。即唯一ID，区分设备的最小ID
            put("#acid",getAccountId()!!)//登录账号id
            put("#gaid","")//谷歌广告标识id,不同app在同一个设备上gdid一样
            put("#oaid","")//华为广告标识id,不同app在同一个设备上oaid一样
            put("#app_id",getAppId()!!)//应用唯一标识,后台分配
            put("#pkg",mContext?.packageName!!)//包名
        })

    /**
     * 获取并配置 事件通用属性
     *
     * @return
     */
    protected open fun setupCommonProperties(): Map<String, Any>? =
        Collections.unmodifiableMap(HashMap<String, Any>().apply {
            put("#mcc", "")//移动信号国家码
            put("#mnc", "")//移动信号网络码
            put("#os_country", "")//系统国家
            put("#os_lang", "")//系统语言
            put("#app_version_code", "")//应用版本号
            put("#sdk_type", "")//接入 SDK 的类型，如 Android，iOS,Unity ,Flutter
            put("#sdk_version", "")//SDK 版本,如 1.1.2
            put("#os", "")//如 Android、iOS 等
            put("#os_version", "")//操作系统版本,iOS 11.2.2、Android 8.0.0 等
            put("#browser_version", "")//浏览器版本,用户使用的浏览器的版本，如 Chrome 61.0，Firefox 57.0 等
            put("#device_manufacturer", "")//用户设备的制造商，如 Apple，vivo 等
            put("#device_brand", "")//设备品牌,如 Galaxy、Pixel
            put("#device_model", "")//设备型号,用户设备的型号，如 iPhone 8 等
            put("#screen_height", "")//屏幕高度
            put("#screen_width", "")//屏幕宽度
        })


    open fun setDebugMode(debugMode: DebugMode) {
        mDebugMode = debugMode
        if (debugMode === DebugMode.DEBUG_OFF) {
            enableLog(false)
            LogUtils.setDebug(false)
        } else {
            enableLog(true)
            LogUtils.setDebug(true)
        }
    }
}