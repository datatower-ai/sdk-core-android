package com.nodetower.analytics.api

import android.content.Context
import android.text.TextUtils
import com.nodetower.analytics.EventTimer
import com.nodetower.analytics.utils.AppInfoUtils
import com.nodetower.analytics.utils.DataHelper.assertKey
import com.nodetower.analytics.utils.DataHelper.assertPropertyTypes
import com.nodetower.analytics.utils.DeviceUtils
import com.nodetower.base.utils.LogUtils
import org.json.JSONException
import org.json.JSONObject
import java.util.*


abstract class AbstractAnalyticsApi : IAnalyticsApi {

    private var mContext: Context? = null

    /* 事件计时器集合 */
    protected var mTrackTimer: MutableMap<String, EventTimer>? = null
    /* Debug 模式选项 */
    protected var mDebugMode: DebugMode = DebugMode.DEBUG_OFF
    /* AndroidID */
    protected var mAndroidId: String? = null
    // SDK版本
//    val VERSION: String = BuildConfig.SDK_VERSION

    constructor(
        context: Context,
        serverUrl: String?,
        debugMode: DebugMode
    ) {
        mContext = context
        setDebugMode(debugMode)
        mTrackTimer = HashMap()
    }

    private constructor() {
        mContext = null
        mTrackTimer = null
    }

    override fun setServerUrl(serverUrl: String?) {

    }


    protected fun trackEvent(
        eventName: String,
        properties: JSONObject
    ) {
        try {
            //获取事件计时器
            var eventTimer: EventTimer? = null
            if (!TextUtils.isEmpty(eventName)) {
                synchronized(mTrackTimer!!) {
                    eventTimer = mTrackTimer?.get(eventName)
                    mTrackTimer?.remove(eventName)
                }
            }
            //检查事件名
            assertKey(eventName)
            //检查事件属性
            assertPropertyTypes(properties)

            try {
                var sendProperties: JSONObject

            }catch (e: JSONException){

            }
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }


    }

    /**
     * 获取并配置 App 的一些基本属性
     *
     * @return 设备信息
     */
    protected open fun setupDeviceInfo(): Map<String, Any>? {
        val deviceInfo: MutableMap<String, Any> = HashMap()
        deviceInfo["\$lib"] = "Android"
//        deviceInfo["\$lib_version"] = VERSION
        deviceInfo["\$os"] = "Android"
        deviceInfo["\$os_version"] = DeviceUtils.oS
        deviceInfo["\$manufacturer"] = DeviceUtils.manufacturer
        deviceInfo["\$model"] = DeviceUtils.model
        deviceInfo["\$app_version"] = AppInfoUtils.getAppVersionName(mContext)
        val size: IntArray = DeviceUtils.getDeviceSize(mContext!!)
        deviceInfo["\$screen_width"] = size[0]
        deviceInfo["\$screen_height"] = size[1]
//        val carrier: String = DataUtils.getCarrier(mContext)
//        if (!TextUtils.isEmpty(carrier)) {
//            deviceInfo["\$carrier"] = carrier
//        }
//        if (!mDisableTrackDeviceId && !TextUtils.isEmpty(mAndroidId)) {
//            deviceInfo["\$device_id"] = mAndroidId!!
//        }
//        val zone_offset: Int = TimeUtils.getZoneOffset()
//        if (zone_offset != null) {
//            deviceInfo["\$timezone_offset"] = zone_offset
//        }
        deviceInfo["\$app_id"] = AppInfoUtils.getProcessName(mContext)
        deviceInfo["\$app_name"] = AppInfoUtils.getAppName(mContext)
        return Collections.unmodifiableMap(deviceInfo)
    }


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