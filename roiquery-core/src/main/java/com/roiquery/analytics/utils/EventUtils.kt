package com.roiquery.analytics.utils

import android.content.Context
import android.text.TextUtils
import com.roiquery.analytics.BuildConfig
import com.roiquery.analytics.Constant
import com.roiquery.analytics.api.AbstractAnalytics
import com.roiquery.analytics.data.EventDateAdapter
import org.json.JSONObject
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import java.util.regex.Pattern

object EventUtils {
    private const val TAG = "ROIQuery.EventUtils"
    private val KEY_PATTERN =
        Pattern.compile("^[a-zA-Z][a-zA-Z\\d_]{0,49}$", Pattern.CASE_INSENSITIVE)

     fun getEventInfo(context: Context,dataAdapter: EventDateAdapter?)
         = mutableMapOf<String, Any?>().apply {
            put(
                Constant.EVENT_INFO_DID,
                DeviceUtils.getAndroidID(context)
            )//设备 ID。即唯一ID，区分设备的最小ID
            put(
                Constant.EVENT_INFO_ACID,
                dataAdapter?.accountId
            )//登录账号id
            put(
                Constant.EVENT_INFO_GAID,
                dataAdapter?.gaid.toString()
            )//谷歌广告标识id,不同app在同一个设备上gaid一样
            put(
                Constant.EVENT_INFO_OAID,
                dataAdapter?.oaid.toString()
            )//华为广告标识id,不同app在同一个设备上oaid一样
            put(
                Constant.EVENT_INFO_APP_ID,
                AbstractAnalytics.mConfigOptions?.mAppId
            )//应用唯一标识,后台分配
            put(
                Constant.EVENT_INFO_PKG,
                context.packageName
            )//包名
            if (AbstractAnalytics.mConfigOptions?.mEnabledDebug == true) {
                put(Constant.EVENT_INFO_DEBUG, true)
            }
    }

    fun getCommonProperties(context: Context, dataAdapter: EventDateAdapter?) =
        mutableMapOf<String, Any?>().apply {
            put(
                Constant.COMMON_PROPERTY_EVENT_SESSION,
                dataAdapter?.eventSession
            )//系列行为标识
            put(
                Constant.COMMON_PROPERTY_FIREBASE_IID,
                dataAdapter?.fiid
            )//Firebase的app_instance_id
            put(
                Constant.COMMON_PROPERTY_APPSFLYER_ID,
                dataAdapter?.afid
            )//appsflyer_id
            put(
                Constant.COMMON_PROPERTY_KOCHAVA_ID,
                dataAdapter?.koid
            )//kochava_id
            put(
                Constant.COMMON_PROPERTY_ROIQUERY_ID,
                DeviceUtils.getROIQueryID(dataAdapter)
            )//roiquery_id
            put(
                Constant.COMMON_PROPERTY_MCC,
                DeviceUtils.getMcc(context)
            )//移动信号国家码
            put(
                Constant.COMMON_PROPERTY_MNC,
                DeviceUtils.getMnc(context)
            )//移动信号网络码
            put(
                Constant.COMMON_PROPERTY_OS_COUNTRY,
                DeviceUtils.getLocalCountry(context)
            )//系统国家
            put(
                Constant.COMMON_PROPERTY_OS_LANG,
                DeviceUtils.getLocaleLanguage()
            )//系统语言
            put(
                Constant.COMMON_PROPERTY_APP_VERSION_CODE,
                AppInfoUtils.getAppVersionCode(context)
            )//应用版本号
            put(
                Constant.COMMON_PROPERTY_APP_VERSION_NAME,
                AppInfoUtils.getAppVersionName(context)
            )//应用版本号
            put(
                Constant.COMMON_PROPERTY_SDK_TYPE,
                Constant.SDK_TYPE_ANDROID
            )//接入 SDK 的类型，如 Android，iOS,Unity ,Flutter
            put(
                Constant.COMMON_PROPERTY_SDK_VERSION,
                BuildConfig.VERSION_NAME
            )//SDK 版本,如 1.1.2
            put(
                Constant.COMMON_PROPERTY_OS,
                Constant.SDK_TYPE_ANDROID
            )//如 Android、iOS 等
            put(
                Constant.COMMON_PROPERTY_OS_VERSION,
                DeviceUtils.oS
            )//操作系统版本,iOS 11.2.2、Android 8.0.0 等
            put(
                Constant.COMMON_PROPERTY_DEVICE_MANUFACTURER,
                DeviceUtils.manufacturer
            )//用户设备的制造商，如 Apple，vivo 等
            put(
                Constant.COMMON_PROPERTY_DEVICE_BRAND,
                DeviceUtils.brand
            )//设备品牌,如 Galaxy、Pixel
            put(
                Constant.COMMON_PROPERTY_DEVICE_MODEL,
                DeviceUtils.model
            )//设备型号,用户设备的型号，如 iPhone 8 等
            val size = DeviceUtils.getDeviceSize(context)
            put(
                Constant.COMMON_PROPERTY_SCREEN_HEIGHT,
                size[0]
            )//屏幕高度
            put(
                Constant.COMMON_PROPERTY_SCREEN_WIDTH,
                size[1]
            )//屏幕宽度
        }

    fun isValidEventName(name: String): Boolean {
        if (TextUtils.isEmpty(name)) {
            LogUtils.e("Empty event name is not allowed.")
            return false
        }
        if (!KEY_PATTERN.matcher(name).matches()) {
            LogUtils.e(
                "event name[$name] is not valid. The property KEY must be string that starts with English letter, " +
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
                if (!KEY_PATTERN.matcher(key).matches()) {
                    LogUtils.e(
                        TAG,
                        "Property name[$key] is not valid. The property KEY must be string that starts with English letter, " +
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

}