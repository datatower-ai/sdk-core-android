package com.roiquery.analytics.utils

import android.content.Context
import android.os.Build
import android.text.TextUtils
import com.roiquery.analytics.BuildConfig
import com.roiquery.analytics.Constant
import com.roiquery.analytics.api.AbstractAnalytics
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.data.EventDateAdapter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.regex.Pattern

object EventUtils {
    private const val TAG = "ROIQuery.EventUtils"
    private val KEY_PATTERN =
        Pattern.compile("^[a-zA-Z][a-zA-Z\\d_#]{0,49}", Pattern.CASE_INSENSITIVE)

    fun getEventInfo(context: Context, dataAdapter: EventDateAdapter?,eventInfo: MutableMap<String, Any?>) {
        //登录账号id
        dataAdapter?.accountId?.let {
            if (it.isNotEmpty()) {
                eventInfo[Constant.EVENT_INFO_ACID] = it
            }
        }
        //应用唯一标识,后台分配
        eventInfo[Constant.EVENT_INFO_APP_ID] = AnalyticsConfig.instance.mAppId
        //包名
        eventInfo[Constant.EVENT_INFO_PKG] = context.packageName
        //进程名
        eventInfo[Constant.EVENT_INFO_BUNDLE_ID] = ProcessUtil.getCurrentProcessName(context)
        //debug 标志
        if (AnalyticsConfig.instance.mEnabledDebug) {
            eventInfo[Constant.EVENT_INFO_DEBUG] = true
        }
        //dt_id (gaid/androidId + appId)
        dataAdapter?.dtId?.let {
            if (it.isNotEmpty()) {
                eventInfo[Constant.EVENT_INFO_DT_ID] = it
            }
        }
    }


    fun getLatestUserProperties(context: Context, dataAdapter: EventDateAdapter?) =
        mutableMapOf<String, Any?>().apply {
            //debug 标志
            put(
                Constant.USER_PROPERTY_LATEST_DEBUG,
                AnalyticsConfig.instance.mEnabledDebug
            )
            put(
                Constant.USER_PROPERTY_LATEST_APP_VERSION_NAME,
                AppInfoUtils.getAppVersionName(context)
            )
            put(
                Constant.USER_PROPERTY_LATEST_APP_VERSION_CODE,
                AppInfoUtils.getAppVersionCode(context)
            )
        }

//    fun getActiveUserProperties(context: Context, dataAdapter: EventDateAdapter?) =
//        mutableMapOf<String, Any?>().apply {
//
//            DeviceUtils.getMcc(context).let {
//                if (it.isNotEmpty()) {
//                    put(Constant.USER_PROPERTY_ACTIVE_MCC, it)
//                }
//            }
//            DeviceUtils.getMnc(context).let {
//                if (it.isNotEmpty()) {
//                    put(Constant.USER_PROPERTY_ACTIVE_MNC, it)
//                }
//            }
//            put(
//                Constant.USER_PROPERTY_ACTIVE_OS_COUNTRY,
//                DeviceUtils.getLocalCountry(context)
//            )//系统国家
//            put(
//                Constant.USER_PROPERTY_ACTIVE_OS_LANG,
//                DeviceUtils.getLocaleLanguage()
//            )//系统语言
//            put(
//                Constant.USER_PROPERTY_ACTIVE_PKG,
//                context.packageName
//            )//包名
//            put(
//                Constant.USER_PROPERTY_ACTIVE_APP_VERSION_CODE,
//                AppInfoUtils.getAppVersionCode(context)
//            )//应用版本号
//            put(
//                Constant.USER_PROPERTY_ACTIVE_APP_VERSION_NAME,
//                AppInfoUtils.getAppVersionName(context)
//            )//应用版本名
//            put(
//                Constant.USER_PROPERTY_ACTIVE_OS,
//                Constant.SDK_TYPE_ANDROID
//            )//如 Android、iOS 等
//            put(
//                Constant.USER_PROPERTY_ACTIVE_OS_VERSION_NAME,
//                DeviceUtils.oS
//            )//操作系统版本,iOS 11.2.2、Android 8.0.0 等
//            put(
//                Constant.USER_PROPERTY_ACTIVE_OS_VERSION_CODE,
//                Build.VERSION.SDK_INT
//            )
//            put(
//                Constant.USER_PROPERTY_ACTIVE_DEVICE_MANUFACTURER,
//                DeviceUtils.manufacturer
//            )//用户设备的制造商，如 Apple，vivo 等
//            put(
//                Constant.USER_PROPERTY_ACTIVE_DEVICE_BRAND,
//                DeviceUtils.brand
//            )//设备品牌,如 Galaxy、Pixel
//            put(
//                Constant.USER_PROPERTY_ACTIVE_BUILD_DEVICE,
//                Build.DEVICE
//            )
//            put(
//                Constant.USER_PROPERTY_ACTIVE_DEVICE_MODEL,
//                DeviceUtils.model
//            )//设备型号,用户设备的型号，如 iPhone 8 等
//            val size = DeviceUtils.getDeviceSize(context)
//            put(
//                Constant.USER_PROPERTY_ACTIVE_SCREEN_WIDTH,
//                size[0]
//            )//屏幕宽度
//            put(
//                Constant.USER_PROPERTY_ACTIVE_SCREEN_HEIGHT,
//                size[1]
//            )//屏幕高度
////
////            put(
////                Constant.USER_PROPERTY_ACTIVE_DIMS_DPI,
////                DeviceUtils.getDensityDpi(context)
////            )
//            put(
//                Constant.USER_PROPERTY_ACTIVE_MEMORY_USED,
//                MemoryUtils.getRAM(context)
//            )
//
//            put(
//                Constant.USER_PROPERTY_ACTIVE_STORAGE_USED,
//                MemoryUtils.getDisk(context, false)
//            )
//
//            put(
//                Constant.USER_PROPERTY_ACTIVE_NETWORK_TYPE,
//                NetworkUtil.getNetworkTypeString(context)
//            )
//            put(
//                Constant.USER_PROPERTY_ACTIVE_SIMULATOR,
//                EmulatorDetector.isEmulator()
//            )
//
//            put(
//                Constant.USER_PROPERTY_ACTIVE_USER_AGENT,
//                DeviceUtils.getUserAgent(context)
//            )
//
//        }

    fun getCommonProperties(
        context: Context,
        commonProperties: MutableMap<String, Any?>,
        activeProperties: MutableMap<String, Any?>
    ) {
        //移动信号国家码
        DeviceUtils.getMcc(context).let {
            if (it.isNotEmpty()) {
                commonProperties[Constant.COMMON_PROPERTY_MCC] = it
                activeProperties[Constant.USER_PROPERTY_ACTIVE_MCC] = it
            }
        }
        //移动信号网络码
        DeviceUtils.getMnc(context).let {
            if (it.isNotEmpty()) {
                commonProperties[Constant.COMMON_PROPERTY_MNC] = it
                activeProperties[Constant.USER_PROPERTY_ACTIVE_MNC] = it
            }
        }
        //系统国家
        DeviceUtils.getLocalCountry(context).let {
            if (it.isNotEmpty()) {
                commonProperties[Constant.COMMON_PROPERTY_OS_COUNTRY] = it
                activeProperties[Constant.USER_PROPERTY_ACTIVE_OS_COUNTRY] = it
            }
        }
        //系统语言
        DeviceUtils.getLocaleLanguage().let {
            commonProperties[Constant.COMMON_PROPERTY_OS_LANG] = it
            activeProperties[Constant.USER_PROPERTY_ACTIVE_OS_LANG] = it
        }

        //应用版本号
        AppInfoUtils.getAppVersionCode(context).let {
            commonProperties[Constant.COMMON_PROPERTY_APP_VERSION_CODE] = it
            activeProperties[Constant.USER_PROPERTY_ACTIVE_APP_VERSION_CODE] = it
        }

        //应用版本号
        AppInfoUtils.getAppVersionName(context).let {
            commonProperties[Constant.COMMON_PROPERTY_APP_VERSION_NAME] = it
            activeProperties[Constant.USER_PROPERTY_ACTIVE_APP_VERSION_NAME] = it
        }

        //接入 SDK 的类型，如 Android，iOS,Unity
        commonProperties[Constant.COMMON_PROPERTY_SDK_TYPE] = Constant.SDK_TYPE_ANDROID
        activeProperties[Constant.USER_PROPERTY_ACTIVE_SDK_TYPE] = Constant.SDK_TYPE_ANDROID

        //SDK 版本,如 1.1.2
        commonProperties[Constant.COMMON_PROPERTY_SDK_VERSION] = BuildConfig.VERSION_NAME
        activeProperties[Constant.USER_PROPERTY_ACTIVE_SDK_VERSION] = BuildConfig.VERSION_NAME

        //如 Android、iOS 等
        commonProperties[Constant.COMMON_PROPERTY_OS] = Constant.SDK_TYPE_ANDROID
        activeProperties[Constant.USER_PROPERTY_ACTIVE_OS] = Constant.SDK_TYPE_ANDROID

        //操作系统版本名, Android 8.0.0 等
        DeviceUtils.oS.let {
            commonProperties[Constant.COMMON_PROPERTY_OS_VERSION_NAME] = it
            activeProperties[Constant.USER_PROPERTY_ACTIVE_OS_VERSION_NAME] = it
        }

        //操作系统版本号, 如 31
        commonProperties[Constant.COMMON_PROPERTY_OS_VERSION_CODE] = Build.VERSION.SDK_INT
        activeProperties[Constant.USER_PROPERTY_ACTIVE_OS_VERSION_CODE] = Build.VERSION.SDK_INT

        //用户设备的制造商，如 Apple，vivo 等
        DeviceUtils.manufacturer.let {
            commonProperties[Constant.COMMON_PROPERTY_DEVICE_MANUFACTURER] = it
            activeProperties[Constant.USER_PROPERTY_ACTIVE_DEVICE_MANUFACTURER] = it
        }

        //设备品牌,如 Galaxy、Pixel
        DeviceUtils.brand.let {
            commonProperties[Constant.COMMON_PROPERTY_DEVICE_BRAND] = it
            activeProperties[Constant.USER_PROPERTY_ACTIVE_DEVICE_BRAND] = it
        }

        commonProperties[Constant.COMMON_PROPERTY_BUILD_DEVICE] = Build.DEVICE
        activeProperties[Constant.USER_PROPERTY_ACTIVE_BUILD_DEVICE] = Build.DEVICE

        //设备型号,用户设备的型号，如 iPhone 8 等
        DeviceUtils.model.let {
            commonProperties[Constant.COMMON_PROPERTY_DEVICE_MODEL] = it
            activeProperties[Constant.USER_PROPERTY_ACTIVE_DEVICE_MODEL] = it
        }

        val size = DeviceUtils.getDeviceSize(context)
        //屏幕高度
        commonProperties[Constant.COMMON_PROPERTY_SCREEN_WIDTH] = size[0]
        activeProperties[Constant.USER_PROPERTY_ACTIVE_SCREEN_WIDTH] = size[0]

        //屏幕宽度
        commonProperties[Constant.COMMON_PROPERTY_SCREEN_HEIGHT] = size[1]
        activeProperties[Constant.USER_PROPERTY_ACTIVE_SCREEN_HEIGHT] = size[1]

        //网络状态
        NetworkUtil.getNetworkTypeString(context).let {
            commonProperties[Constant.COMMON_PROPERTY_NETWORK_TYPE] = it
            activeProperties[Constant.USER_PROPERTY_ACTIVE_NETWORK_TYPE] = it
        }

        //是否是模拟器
        EmulatorDetector.isEmulator().let {
            commonProperties[Constant.COMMON_PROPERTY_SIMULATOR] = it
            activeProperties[Constant.USER_PROPERTY_ACTIVE_SIMULATOR] = it
        }

        //时区
        commonProperties[Constant.COMMON_PROPERTY_EVENT_ZONE_OFFSET] =
            DataUtils.getTimezoneOffset(Date().time, null)

        activeProperties[Constant.USER_PROPERTY_ACTIVE_MEMORY_USED] = MemoryUtils.getRAM(context)

        activeProperties[Constant.USER_PROPERTY_ACTIVE_STORAGE_USED] = MemoryUtils.getDisk(context, false)

        if (ProcessUtil.isMainProcess(context)) {
            activeProperties[Constant.USER_PROPERTY_ACTIVE_USER_AGENT] = DeviceUtils.getUserAgent(context)
        }
        activeProperties[Constant.USER_PROPERTY_ACTIVE_PKG] = context.packageName
    }

    fun isValidEventName(name: String?): Boolean {
        if (name.isNullOrEmpty() || TextUtils.isEmpty(name)) {
            LogUtils.e("Empty event name is not allowed.")
            return false
        }
        if (hasPresetTag(name)) {
            LogUtils.e(" event name [$name] start with # or $ is not allowed.")
            return false
        }
        if (!KEY_PATTERN.matcher(name).matches()) {
            LogUtils.e(
                "event name [$name] is not valid. The property KEY must be string that starts with English letter, " +
                        "and contains letter, number, and '_'. The max length of the property KEY is 50. "
            )
            return false
        }
        return true
    }

    fun isValidProperty(properties: JSONObject?): Boolean {
        if (properties != null) {
            val iterator: Iterator<*> = properties.keys()
            while (iterator.hasNext()) {
                val key = iterator.next() as String
                if (TextUtils.isEmpty(key)) {
                    LogUtils.e(TAG, "Empty property name is not allowed.")
                    return false
                }
                if (hasPresetTag(key)) {
                    LogUtils.e(" property name [$key] start with # or $ is not allowed.")
                    return false
                }
                if (!KEY_PATTERN.matcher(key).matches()) {
                    LogUtils.e(
                        TAG,
                        "Property name [$key] is not valid. The property KEY must be string that starts with English letter, " +
                                "and contains letter, number, and '_'. The max length of the property KEY is 50. "
                    )
                    return false
                }
                try {
                    val value = properties[key]
                    if (!(value is String || value is Number || value is Boolean || value is Date || value is JSONArray || value is JSONObject)) {
                        LogUtils.e(
                            TAG,
                            "Property value must be type String, Number, Boolean, Date, JSONObject or JSONArray"
                        )
                        return false
                    }
                    if (value is Number) {
                        val number = value.toDouble()
                        if (number > 9999999999999.999 || number < -9999999999999.999) {
                            LogUtils.e(TAG, "The number value [$value] is invalid.")
                            return false
                        }
                    }
                } catch (e: JSONException) {
                    LogUtils.e(TAG, "Unexpected parameters.$e")
                    return false
                }
            }
        }
        return true
    }

    private fun hasPresetTag(key: String) = key.startsWith("#") || key.startsWith("$")

}