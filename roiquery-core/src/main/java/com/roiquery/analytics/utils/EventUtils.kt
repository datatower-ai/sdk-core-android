package com.roiquery.analytics.utils

import android.content.Context
import android.os.Build
import android.text.TextUtils
import com.google.android.a.c
import com.roiquery.analytics.BuildConfig
import com.roiquery.analytics.Constant
import com.roiquery.analytics.api.AbstractAnalytics
import com.roiquery.analytics.data.EventDateAdapter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.regex.Pattern

object EventUtils {
    private const val TAG = "ROIQuery.EventUtils"
    private val KEY_PATTERN =
        Pattern.compile("^[a-zA-Z][a-zA-Z\\d_]{0,49}$", Pattern.CASE_INSENSITIVE)

    fun getEventInfo(context: Context, dataAdapter: EventDateAdapter?) =
        mutableMapOf<String, Any?>().apply {
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

    private fun getSystemPropertiesForUserSet(context: Context) =
        mutableMapOf<String, Any?>().apply {
            put(
                Constant.USER_PROPERTY_SYSTEM_GP_VERSION_CODE,
                AppInfoUtils.getAppVersionCode(context, "com.android.vending")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_GP_VERSION_NAME,
                AppInfoUtils.getAppVersionName(context, "com.android.vending")
            )
            put(Constant.USER_PROPERTY_SYSTEM_DIMS_SIZE, DensityUtils.getScreenSize(context))
            put(Constant.USER_PROPERTY_SYSTEM_DIMS_X_DP, DensityUtils.getScreenWidthWithDp(context))
            put(
                Constant.USER_PROPERTY_SYSTEM_DIMS_Y_DP,
                DensityUtils.getScreenHeightWithDp(context)
            )
            put(Constant.USER_PROPERTY_SYSTEM_DIMS_X_PX, DensityUtils.getScreenWidth(context))
            put(Constant.USER_PROPERTY_SYSTEM_DIMS_Y_PX, DensityUtils.getScreenHeight(context))
            put(Constant.USER_PROPERTY_SYSTEM_DIMS_D_DPI, DensityUtils.getDensityDpi(context))

            put(Constant.USER_PROPERTY_SYSTEM_BP_RO_ARCH, CommandUtils.getProperty("ro.arch"))
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_RO_CHIPNAME,
                CommandUtils.getProperty("ro.chipname")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_RO_DALVIK_VM_NATIVE_BRIDGE,
                CommandUtils.getProperty("ro.dalvik.vm.native.bridge")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_PERSIST_SYS_NATIVEBRIDGE,
                CommandUtils.getProperty("persist.sys.nativebridge")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_RO_ENABLE_NATIVE_BRIDGE_EXEC,
                CommandUtils.getProperty("ro.enable.native.bridge.exec")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_X86_FEATURES,
                CommandUtils.getProperty("dalvik.vm.isa.x86.features")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_X86_VARIANT,
                CommandUtils.getProperty("dalvik.vm.isa.x86.variant")
            )
            put(Constant.USER_PROPERTY_SYSTEM_BP_RO_ZYGOTE, CommandUtils.getProperty("ro.zygote"))
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_RO_ALLOW_MOCK_LOCATION,
                CommandUtils.getProperty("ro.allow.mock.location")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_RO_DALVIK_VM_ISA_ARM,
                CommandUtils.getProperty("ro.dalvik.vm.isa.arm")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_ARM_FEATURES,
                CommandUtils.getProperty("dalvik.vm.isa.arm.features")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_ARM_VARIANT,
                CommandUtils.getProperty("dalvik.vm.isa.arm.variant")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_ARM64_FEATURES,
                CommandUtils.getProperty("dalvik.vm.isa.arm64.features")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_ARM64_VARIANT,
                CommandUtils.getProperty("dalvik.vm.isa.arm64.variant")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_VZW_OS_ROOTED,
                CommandUtils.getProperty("vzw.os.rooted")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_RO_BUILD_USER,
                CommandUtils.getProperty("ro.build.user")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_RO_KERNEL_QEMU,
                CommandUtils.getProperty("ro.kernel.qemu")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_RO_HARDWARE,
                CommandUtils.getProperty("ro.hardware")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_RO_PRODUCT_CPU_ABI,
                CommandUtils.getProperty("ro.product.cpu.abi")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_RO_PRODUCT_CPU_ABILIST,
                CommandUtils.getProperty("ro.product.cpu.abilist")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_PRODUCT_CPU_ABILIST32,
                CommandUtils.getProperty("ro.product.cpu.abilist32")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_PRODUCT_CPU_ABILIST64,
                CommandUtils.getProperty("ro.product.cpu.abilist64")
            )
            put(
                Constant.USER_PROPERTY_SYSTEM_BP_BUILD_DISPLAY_ID,
                CommandUtils.getProperty("ro.build.display.id")
            )

            put(
                Constant.USER_PROPERTY_SYSTEM_LAUNCHER_PKG,
                AppInfoUtils.getLauncherPackageName(context)
            )

            put(Constant.USER_PROPERTY_SYSTEM_OS_SDK_VERSION, Build.VERSION.SDK_INT)

            put(Constant.USER_PROPERTY_SYSTEM_DATA_FOLDER_USED, DataUtils.dataFolderUsed())

            put(Constant.USER_PROPERTY_SYSTEM_DEVICE_SENSOR, DataUtils.getSensor(context))

//            const val USER_PROPERTY_SYSTEM_BUILD_INCREMENTAL   = "build_incremental"
//            const val USER_PROPERTY_SYSTEM_BUILD_SDK           = "build_sdk"
//            const val USER_PROPERTY_SYSTEM_USER_AGENT_HTTP     = "user_agent_http"
//            const val USER_PROPERTY_SYSTEM_BUILD_HOST          = "build_host"
//            const val USER_PROPERTY_SYSTEM_BUILD_FRINGERPRINT  = "build_fringerprint"
//            const val USER_PROPERTY_SYSTEM_BASEBAND            = "baseband"
//            const val USER_PROPERTY_SYSTEM_BUILD_BOARD         = "build_board"
//            const val USER_PROPERTY_SYSTEM_BUILD_ID            = "build_id"
            put(Constant.USER_PROPERTY_SYSTEM_BUILD_INCREMENTAL, Build.VERSION.INCREMENTAL)
            put(Constant.USER_PROPERTY_SYSTEM_BUILD_SDK, Build.VERSION.SDK)

            put(Constant.USER_PROPERTY_SYSTEM_BUILD_HOST, Build.HOST)
            put(Constant.USER_PROPERTY_SYSTEM_BUILD_FINGERPRINT, Build.FINGERPRINT)
            put(Constant.USER_PROPERTY_SYSTEM_BASEBAND, CommandUtils.getProperty("gsm.version.baseband"))
            put(Constant.USER_PROPERTY_SYSTEM_BUILD_BOARD, Build.BOARD)
            put(Constant.USER_PROPERTY_SYSTEM_BUILD_ID, Build.ID)

        }

    fun getCommonPropertiesForUserSet(context: Context, dataAdapter: EventDateAdapter?) =
        mutableMapOf<String, Any?>().apply {

            dataAdapter?.accountId?.let {
                if (it.isNotEmpty()) {
                    put(
                        Constant.EVENT_INFO_ACID,
                        it
                    )
                }
            }

            dataAdapter?.gaid?.let {
                if (it.isNotEmpty()) {
                    put(
                        Constant.EVENT_INFO_GAID,
                        it
                    )
                }
            }

            dataAdapter?.oaid?.let {
                if (it.isNotEmpty()) {
                    put(
                        Constant.EVENT_INFO_OAID,
                        it
                    )
                }
            }

            dataAdapter?.fiid?.let {
                if (it.isNotEmpty()) {
                    put(
                        Constant.COMMON_PROPERTY_FIREBASE_IID,
                        it
                    )
                }
            }
            dataAdapter?.fcmToken?.let {
                if (it.isNotEmpty()) {
                    put(
                        Constant.COMMON_PROPERTY_FCM_TOKEN,
                        it
                    )
                }
            }
            dataAdapter?.afid?.let {
                if (it.isNotEmpty()) {
                    put(
                        Constant.COMMON_PROPERTY_APPSFLYER_ID,
                        it
                    )
                }
            }
            dataAdapter?.koid?.let {
                if (it.isNotEmpty()) {
                    put(
                        Constant.COMMON_PROPERTY_KOCHAVA_ID,
                        it
                    )
                }
            }
            DeviceUtils.getROIQueryID(dataAdapter).let {
                if (it.isNotEmpty()) {
                    put(
                        Constant.COMMON_PROPERTY_ROIQUERY_ID,
                        it
                    )
                }
            }

            DeviceUtils.getMcc(context).let {
                if (it.isNotEmpty()) {
                    put(
                        Constant.COMMON_PROPERTY_MCC,
                        it
                    )
                }
            }

            DeviceUtils.getMnc(context).let {
                if (it.isNotEmpty()) {
                    put(
                        Constant.COMMON_PROPERTY_MNC,
                        it
                    )
                }
            }

            DeviceUtils.getLocalCountry(context).let {
                if (it.isNotEmpty()) {
                    put(
                        Constant.COMMON_PROPERTY_OS_COUNTRY,
                        it
                    )
                }
            }

            DeviceUtils.getLocaleLanguage().let {
                if (it.isNotEmpty()) {
                    put(
                        Constant.COMMON_PROPERTY_OS_LANG,
                        it
                    )
                }
            }

            AppInfoUtils.getAppVersionCode(context).let {
                if (it > 0) {
                    put(
                        Constant.COMMON_PROPERTY_APP_VERSION_CODE,
                        it
                    )
                }
            }

            AppInfoUtils.getAppVersionName(context).let {
                if (it.isNotEmpty()) {
                    put(
                        Constant.COMMON_PROPERTY_APP_VERSION_NAME,
                        it
                    )
                }
            }

            put(
                Constant.COMMON_PROPERTY_OS,
                Constant.SDK_TYPE_ANDROID
            )

            DeviceUtils.oS.let {
                if (it.isNotEmpty()) {
                    put(
                        Constant.COMMON_PROPERTY_OS_VERSION,
                        it
                    )
                }
            }


            DeviceUtils.manufacturer.let {
                if (it.isNotEmpty()) {
                    if (it.isNotEmpty()) {
                        put(
                            Constant.COMMON_PROPERTY_DEVICE_MANUFACTURER,
                            it
                        )
                    }
                }
            }

            DeviceUtils.brand.let {
                if (it.isNotEmpty()) {
                    if (it.isNotEmpty()) {
                        put(
                            Constant.COMMON_PROPERTY_DEVICE_BRAND,
                            it
                        )
                    }
                }
            }


            DeviceUtils.model.let {
                if (it.isNotEmpty()) {
                    if (it.isNotEmpty()) {
                        put(
                            Constant.COMMON_PROPERTY_DEVICE_MODEL,
                            it
                        )
                    }
                }
            }


            val size = DeviceUtils.getDeviceSize(context)
            if (size[0] > 0) {
                put(
                    Constant.COMMON_PROPERTY_SCREEN_WIDTH,
                    size[0]
                )//屏幕高度
            }

            if (size[1] > 0) {
                put(
                    Constant.COMMON_PROPERTY_SCREEN_HEIGHT,
                    size[1]
                )
            }

            put(Constant.COMMON_PROPERTY_DEVICE_DISPLAY, Build.DISPLAY)
            put(Constant.COMMON_PROPERTY_DEVICE_PRODUCT, Build.PRODUCT)
            put(Constant.COMMON_PROPERTY_DEVICE_DEVICE, Build.DEVICE)
            put(Constant.COMMON_PROPERTY_MEMORY_USED, MemoryUtils.getMemoryUsed(context))
            put(Constant.COMMON_PROPERTY_STORAGE_USED, MemoryUtils.getStorageUsed(context))
            put(Constant.COMMON_PROPERTY_USER_AGENT, AppInfoUtils.getDefaultUserAgent(context))

            putAll(getSystemPropertiesForUserSet(context))
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
                Constant.COMMON_PROPERTY_FCM_TOKEN,
                dataAdapter?.fcmToken
            )//firebase cloud message
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
                Constant.COMMON_PROPERTY_SCREEN_WIDTH,
                size[0]
            )//屏幕高度
            put(
                Constant.COMMON_PROPERTY_SCREEN_HEIGHT,
                size[1]
            )//屏幕宽度

            put(Constant.COMMON_PROPERTY_DEVICE_DISPLAY, Build.DISPLAY)
            put(Constant.COMMON_PROPERTY_DEVICE_PRODUCT, Build.PRODUCT)
            put(Constant.COMMON_PROPERTY_DEVICE_DEVICE, Build.DEVICE)
            put(Constant.COMMON_PROPERTY_MEMORY_USED, MemoryUtils.getMemoryUsed(context))
            put(Constant.COMMON_PROPERTY_STORAGE_USED, MemoryUtils.getStorageUsed(context))
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