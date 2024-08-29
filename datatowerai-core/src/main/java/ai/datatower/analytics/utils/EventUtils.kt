package ai.datatower.analytics.utils

import ai.datatower.analytics.Constant
import ai.datatower.analytics.config.AnalyticsConfig
import ai.datatower.analytics.data.EventDataAdapter
import android.app.Application
import android.content.Context
import android.os.Build
import android.text.TextUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Date
import java.util.regex.Pattern

object EventUtils {
    private const val TAG = "DT.EventUtils"
    private val KEY_PATTERN =
        Pattern.compile("^[a-zA-Z][a-zA-Z\\d_#]{0,49}", Pattern.CASE_INSENSITIVE)

    var ua: String = ""
        private set
    fun initUa(context: Context) {
        ua = DeviceUtils.getUserAgent(context)
    }

    suspend fun getEventInfo(context: Context, dataAdapter: EventDataAdapter?) {
        PresetPropManager.get(context).run {
            //登录账号id
            meta[Constant.EVENT_INFO_ACID] = dataAdapter?.getAccountId()?.await()

            //进程名
            meta[Constant.EVENT_INFO_BUNDLE_ID] = ProcessUtil.getCurrentProcessName(context)

            //应用唯一标识,后台分配
            meta[Constant.EVENT_INFO_APP_ID] = AnalyticsConfig.instance.mAppId

            //debug 标志
            if (AnalyticsConfig.instance.mEnabledDebug) {
                meta[Constant.EVENT_INFO_DEBUG] = true
            }

            //dt_id (gaid/androidId + appId)
            dataAdapter?.getDtId()?.await()?.let {
                if (it.isNotEmpty()) meta[Constant.EVENT_INFO_DT_ID] = it
            }
        }
    }


    fun getLatestUserProperties(context: Context) =
        mutableMapOf<String, Any?>().also {
            val ppm = PresetPropManager.get(context)
            // #debug: cannot be disable
            ppm.checkNSet(
                it,
                Constant.USER_PROPERTY_LATEST_DEBUG,
                AnalyticsConfig.instance.mEnabledDebug
            )

            ppm.checkNSet(
                it,
                Constant.USER_PROPERTY_LATEST_APP_VERSION_NAME,
                AppInfoUtils.getAppVersionName(context)
            )

            ppm.checkNSet(
                it,
                Constant.USER_PROPERTY_LATEST_APP_VERSION_CODE,
                AppInfoUtils.getAppVersionCode(context)
            )
        }

    fun getCommonProperties(
        context: Context
    ) {
        val presetPropManager = PresetPropManager.get(context)
        val activeProperties = presetPropManager.userActive
        val commonProperties = presetPropManager.common

        //进程名
        activeProperties[Constant.USER_PROPERTY_ACTIVE_BUNDLE_ID] = ProcessUtil.getCurrentProcessName(context)

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

        // User-Agent
        activeProperties[Constant.USER_PROPERTY_ACTIVE_USER_AGENT] = ua

        //应用版本号
        AppInfoUtils.getAppVersionCode(context).let {
            commonProperties[Constant.COMMON_PROPERTY_APP_VERSION_CODE] = it
            activeProperties[Constant.USER_PROPERTY_ACTIVE_APP_VERSION_CODE] = it
        }

        //应用版本名
        AppInfoUtils.getAppVersionName(context).let {
            commonProperties[Constant.COMMON_PROPERTY_APP_VERSION_NAME] = it
            activeProperties[Constant.USER_PROPERTY_ACTIVE_APP_VERSION_NAME] = it
        }

        //接入 SDK 的类型，如 Android，iOS,Unity
        AnalyticsConfig.instance.getSDKType().let {
            commonProperties[Constant.COMMON_PROPERTY_SDK_TYPE] = it
            activeProperties[Constant.USER_PROPERTY_ACTIVE_SDK_TYPE] = it
        }

        //SDK 版本,如 1.1.2
        AnalyticsConfig.instance.getSDKVersion().let {
            commonProperties[Constant.COMMON_PROPERTY_SDK_VERSION] = it
            activeProperties[Constant.USER_PROPERTY_ACTIVE_SDK_VERSION] = it
        }

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

        //build_device
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
        NetworkUtil.getNetworkTypeString(context.applicationContext as Application).let {
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

        //ram
        activeProperties[Constant.USER_PROPERTY_ACTIVE_MEMORY_USED] = MemoryUtils.getRAM(context)
        //disk
        activeProperties[Constant.USER_PROPERTY_ACTIVE_STORAGE_USED] = MemoryUtils.getDisk(context, false)

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
                    if (!(value is String || value is Number || value is Boolean || value is Date || value is JSONArray || value is JSONObject || value.equals(null))) {
                        LogUtils.e(
                            TAG,
                            "Property value must be type String, Number, Boolean, Date, JSONObject or JSONArray. " +
                                    "Invalid pair: \"$key\" = $value, type of value: ${value::class.java}"
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
