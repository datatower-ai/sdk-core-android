package com.roiquery.analytics

import java.util.ArrayList


internal object Constant {

    const val LOG_TAG = "ROIQuery"

    //ROIQuery base url
    private val ANALYTICS_BASE_URL = if ("0" == BuildConfig.LINK_SITE)"http://35.238.73.110" else "https://api.roiquery.com"

//    private const val ANALYTICS_BASE_URL = "http://35.238.73.110"
    //upload url
    val EVENT_REPORT_URL = "$ANALYTICS_BASE_URL/report"
    // cloudConfig url
    val CLOUD_CONFIG_URL = "$ANALYTICS_BASE_URL/remote/configurator"
//    const val CLOUD_CONFIG_URL = "http://test.roiquery.com/remote/configurator"

    const val ERROR_REPORT_URL="https://debug.roiquery.com/report"
    //ntp server url
    const val NTP_TIME_OUT_M = 31428

    //event upload size
    const val EVENT_REPORT_SIZE = 10
    //event upload try times when filed
    const val EVENT_REPORT_TRY_COUNT = 3

    const val EVENT_INFO_DID         = "#did"
    const val EVENT_INFO_GAID        = "#gaid"
    const val EVENT_INFO_OAID        = "#oaid"
    const val EVENT_INFO_ACID        = "#acid"
    const val EVENT_INFO_PKG         = "#pkg"
    const val EVENT_INFO_APP_ID      = "#app_id"
    const val EVENT_INFO_DEBUG       = "#debug"
    const val EVENT_INFO_TIME        = "#event_time"
    const val EVENT_INFO_NAME        = "#event_name"
    const val EVENT_INFO_TYPE        = "#event_type"
    const val EVENT_INFO_SYN         = "#event_syn"
    const val EVENT_INFO_PROPERTIES  = "properties"

    const val EVENT_BODY  = "body"
    const val EVENT_TIME_CALIBRATED  = "time_calibrated"


    const val COMMON_PROPERTY_EVENT_SESSION       = "#event_session"
    const val COMMON_PROPERTY_FIREBASE_IID        = "#firebase_iid"
    const val COMMON_PROPERTY_FCM_TOKEN           = "#fcm_token"
    const val COMMON_PROPERTY_APPSFLYER_ID        = "#appsflyer_id"
    const val COMMON_PROPERTY_KOCHAVA_ID          = "#kochava_id"
    const val COMMON_PROPERTY_ROIQUERY_ID         = "#roiquery_id"
    const val COMMON_PROPERTY_MCC                 = "#mcc"
    const val COMMON_PROPERTY_MNC                 = "#mnc"
    const val COMMON_PROPERTY_OS_COUNTRY          = "#os_country"
    const val COMMON_PROPERTY_OS_LANG             = "#os_lang"
    const val COMMON_PROPERTY_APP_VERSION_CODE    = "#app_version_code"
    const val COMMON_PROPERTY_APP_VERSION_NAME    = "#app_version_name"
    const val COMMON_PROPERTY_SDK_TYPE            = "#sdk_type"
    const val COMMON_PROPERTY_SDK_VERSION         = "#sdk_version"
    const val COMMON_PROPERTY_OS                  = "#os"
    const val COMMON_PROPERTY_OS_VERSION          = "#os_version"
    const val COMMON_PROPERTY_DEVICE_MANUFACTURER = "#device_manufacturer"
    const val COMMON_PROPERTY_DEVICE_BRAND        = "#device_brand"
    const val COMMON_PROPERTY_DEVICE_DISPLAY      = "#device_display"
    const val COMMON_PROPERTY_DEVICE_PRODUCT      = "#device_product"
    const val COMMON_PROPERTY_DEVICE_DEVICE       = "#device"
    const val COMMON_PROPERTY_MEMORY_USED         = "#memory_used"
    const val COMMON_PROPERTY_STORAGE_USED        = "#storage_used"
    const val COMMON_PROPERTY_USER_AGENT          = "#user_agent"
    const val COMMON_PROPERTY_DEVICE_MODEL        = "#device_model"
    const val COMMON_PROPERTY_SCREEN_HEIGHT       = "#screen_height"
    const val COMMON_PROPERTY_SCREEN_WIDTH        = "#screen_width"
    const val ENGAGEMENT_PROPERTY_IS_FOREGROUND   = "#is_foreground"


    const val ATTRIBUTE_PROPERTY_FIRST_OPEN_TIME             = "first_open_time"
    const val ATTRIBUTE_PROPERTY_REFERRER_URL                = "referrer_url"
    const val ATTRIBUTE_PROPERTY_REFERRER_CLICK_TIME         = "referrer_click_time"
    const val ATTRIBUTE_PROPERTY_APP_INSTALL_TIME            = "app_install_time"
    const val ATTRIBUTE_PROPERTY_INSTANT_EXPERIENCE_LAUNCHED = "instant_experience_launched"
    const val ATTRIBUTE_PROPERTY_FAILED_REASON               = "failed_reason"
    const val ATTRIBUTE_PROPERTY_CNL                         = "cnl"
    const val ATTRIBUTE_PROPERTY_USER_AGENT                  = "user_agent"

    const val USER_PROPERTY_SYSTEM_GP_VERSION_CODE      = "gp_version_code"
    const val USER_PROPERTY_SYSTEM_GP_VERSION_NAME      = "gp_version_name"
    const val USER_PROPERTY_SYSTEM_DIMS_SIZE            = "dims_size"
    const val USER_PROPERTY_SYSTEM_DIMS_X_DP            = "dims_x_dp"
    const val USER_PROPERTY_SYSTEM_DIMS_Y_DP            = "dims_y_dp"
    const val USER_PROPERTY_SYSTEM_DIMS_X_PX            = "dims_x_px"
    const val USER_PROPERTY_SYSTEM_DIMS_Y_PX            = "dims_y_px"
    const val USER_PROPERTY_SYSTEM_DIMS_D_DPI           = "dims_d_dpi"
    const val USER_PROPERTY_SYSTEM_BP_RO_ARCH           = "bp_ro_arch"
    const val USER_PROPERTY_SYSTEM_BP_RO_CHIPNAME       = "bp_ro_chipname"
    const val USER_PROPERTY_SYSTEM_BP_RO_DALVIK_VM_NATIVE_BRIDGE        = "bp_ro_dalvik_vm_native_bridge"
    const val USER_PROPERTY_SYSTEM_BP_PERSIST_SYS_NATIVEBRIDGE          = "bp_persist_sys_nativebridge"
    const val USER_PROPERTY_SYSTEM_BP_RO_ENABLE_NATIVE_BRIDGE_EXEC      = "bp_ro_enable_native_bridge_exec"
    const val USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_X86_FEATURES        = "bp_dalvik_vm_isa_x86_features"
    const val USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_X86_VARIANT         = "bp_dalvik_vm_isa_x86_variant"
    const val USER_PROPERTY_SYSTEM_BP_RO_ZYGOTE                         = "bp_ro_zygote"
    const val USER_PROPERTY_SYSTEM_BP_RO_ALLOW_MOCK_LOCATION            = "bp_ro_allow_mock_location"
    const val USER_PROPERTY_SYSTEM_BP_RO_DALVIK_VM_ISA_ARM              = "bp_ro_dalvik_vm_isa_arm"
    const val USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_ARM_FEATURES        = "bp_dalvik_vm_isa_arm_features"
    const val USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_ARM_VARIANT         = "bp_dalvik_vm_isa_arm_variant"
    const val USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_ARM64_FEATURES      = "bp_dalvik_vm_isa_arm64_features"
    const val USER_PROPERTY_SYSTEM_BP_DALVIK_VM_ISA_ARM64_VARIANT       = "bp_dalvik_vm_isa_arm64_variant"
    const val USER_PROPERTY_SYSTEM_BP_BUILD_DISPLAY_ID       = "bp_ro_build_display_id"
    const val USER_PROPERTY_SYSTEM_BP_VZW_OS_ROOTED      = "bp_vzw_os_rooted"
    const val USER_PROPERTY_SYSTEM_BP_RO_BUILD_USER      = "bp_ro_build_user"
    const val USER_PROPERTY_SYSTEM_BP_RO_KERNEL_QEMU      = "bp_ro_kernel_qemu"
    const val USER_PROPERTY_SYSTEM_BP_RO_HARDWARE        = "bp_ro_hardware"
    const val USER_PROPERTY_SYSTEM_BP_RO_PRODUCT_CPU_ABI          = "bp_ro_product_cpu_abi"
    const val USER_PROPERTY_SYSTEM_BP_RO_PRODUCT_CPU_ABILIST      = "bp_ro_product_cpu_abilist"
    const val USER_PROPERTY_SYSTEM_BP_PRODUCT_CPU_ABILIST32       = "bp_product_cpu_abilist32"
    const val USER_PROPERTY_SYSTEM_BP_PRODUCT_CPU_ABILIST64       = "bp_product_cpu_abilist64"
    const val USER_PROPERTY_SYSTEM_LAUNCHER_PKG        = "launcher_pkg"
//    const val USER_PROPERTY_SYSTEM_DEVICE_DISPLAY      = "device_display"
//    const val USER_PROPERTY_SYSTEM_DEVICE_PRODUCT      = "device_product"
//    const val USER_PROPERTY_SYSTEM_DEVICE_BRAND      = "device_brand"
//    const val USER_PROPERTY_SYSTEM_DEVICE_DEVICE       = "device_device"
    const val USER_PROPERTY_SYSTEM_OS_SDK_VERSION      = "os_sdk_version"
    const val USER_PROPERTY_SYSTEM_DATA_FOLDER_USED    = "data_folder_used"
    const val USER_PROPERTY_SYSTEM_DEVICE_SENSOR       = "device_sensor"
    //new 0518
    const val USER_PROPERTY_SYSTEM_BUILD_INCREMENTAL   = "build_incremental"
    const val USER_PROPERTY_SYSTEM_BUILD_SDK           = "build_sdk"
    const val USER_PROPERTY_SYSTEM_USER_AGENT_HTTP     = "user_agent_http"
    const val USER_PROPERTY_SYSTEM_BUILD_HOST          = "build_host"
    const val USER_PROPERTY_SYSTEM_BUILD_FINGERPRINT  = "build_fingerprint"
    const val USER_PROPERTY_SYSTEM_BASEBAND            = "baseband"
    const val USER_PROPERTY_SYSTEM_BUILD_BOARD         = "build_board"
    const val USER_PROPERTY_SYSTEM_BUILD_ID            = "build_id"





    //preset event name about Analytics
    const val PRESET_EVENT_TAG = "@.#"
    const val PRESET_EVENT_APP_FIRST_OPEN  = PRESET_EVENT_TAG + "app_first_open"
    const val PRESET_EVENT_APP_OPEN        = PRESET_EVENT_TAG + "app_open"
    const val PRESET_EVENT_APP_ATTRIBUTE   = PRESET_EVENT_TAG + "app_attribute"
    const val PRESET_EVENT_APP_ENGAGEMENT  = PRESET_EVENT_TAG + "app_engagement"
    const val PRESET_EVENT_APP_CLOSE       = PRESET_EVENT_TAG + "app_close"
    const val PRESET_EVENT_PAGE_OPEN       = PRESET_EVENT_TAG + "page_open"
    const val PRESET_EVENT_PAGE_CLOSE      = PRESET_EVENT_TAG + "page_close"
    const val PRESET_EVENT_USER_PROPERTIES = PRESET_EVENT_TAG + "user_properties"
    const val PRESET_EVENT_APP_QUALITY     = PRESET_EVENT_TAG + "app_quality"
    const val PRESET_EVENT_APP_STATE_CHANGED     = PRESET_EVENT_TAG + "app_state_changed"

    const val EVENT_TYPE_TRACK = "track"
    const val EVENT_TYPE_USER_ADD = "user_add"
    const val EVENT_TYPE_USER_SET = "user_set"
    const val EVENT_TYPE_USER_SET_ONCE = "user_set_once"
    const val EVENT_TYPE_USER_UNSET = "user_unset"
    const val EVENT_TYPE_USER_APPEND = "user_append"
    const val EVENT_TYPE_USER_DEL = "user_delete"

    //engagement interval time
    const val APP_ENGAGEMENT_INTERVAL_TIME_INT = 5 * 60 * 1000 // 5min
    const val APP_ENGAGEMENT_INTERVAL_TIME_LONG = 5 * 60 * 1000L // 5min

    const val SDK_TYPE_ANDROID = "Android"
    const val SDK_TYPE_UNITY   = "Unity"
    const val SDK_TYPE_FLUTTER = "Flutter"

    // sdk switch，cloudConfig to config
    const val ENABLE_ANALYTICS_SDK_KEY = "enable_analytics_sdk"

    const val TIME_OFFSET_DEFAULT_VALUE="0"

}


object ROIQueryChannel{
    const val GP = "gp"
    const val APPSTORE = "app_store"
}