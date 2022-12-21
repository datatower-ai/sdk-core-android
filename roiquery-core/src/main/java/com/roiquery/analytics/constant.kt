package com.roiquery.analytics


internal object Constant {

    const val LOG_TAG = "DataTower"

    //ROIQuery base url
    const val SERVER_URL_TEST       = "https://test.roiquery.com"
    const val SERVER_URL_INNER      = "https://report-inner.roiquery.com"
    const val SERVER_URL_EXTERNAL   = "https://report.roiquery.com"

    //upload url
    const val EVENT_REPORT_PATH = "/report"

    const val ERROR_REPORT_URL = "https://debug.roiquery.com/debug"

    //event upload size
    const val EVENT_REPORT_SIZE = 10
    //event upload try times when filed
    const val EVENT_REPORT_TRY_COUNT = 3

    const val EVENT_INFO_ANDROID_ID  = "#android_id"
    const val EVENT_INFO_GAID        = "#gaid"
    const val EVENT_INFO_ACID        = "#acid"
    const val EVENT_INFO_DT_ID       = "#dt_id"
    const val EVENT_INFO_PKG         = "#bundle_id"
    const val EVENT_INFO_APP_ID      = "#app_id"
    const val EVENT_INFO_DEBUG       = "#debug"
    const val EVENT_INFO_TIME        = "#event_time"
    const val EVENT_INFO_NAME        = "#event_name"
    const val PRE_EVENT_INFO_NAME    = "event_name"
    const val EVENT_INFO_TYPE        = "#event_type"
    const val PRE_EVENT_INFO_SYN     = "event_syn"
    const val EVENT_INFO_SYN         = "#event_syn"
    const val EVENT_INFO_PROPERTIES  = "properties"

    const val EVENT_BODY  = "body"
    const val EVENT_TIME_CALIBRATED  = "time_calibrated"


    const val COMMON_PROPERTY_EVENT_SESSION       = "#session_id"
    const val COMMON_PROPERTY_MCC                 = "#mcc"
    const val COMMON_PROPERTY_MNC                 = "#mnc"
    const val COMMON_PROPERTY_OS_COUNTRY          = "#os_country_code"
    const val COMMON_PROPERTY_OS_LANG             = "#os_lang_code"
    const val COMMON_PROPERTY_APP_VERSION_CODE    = "#app_version_code"
    const val COMMON_PROPERTY_APP_VERSION_NAME    = "#app_version_name"
    const val COMMON_PROPERTY_SDK_TYPE            = "#sdk_type"
    const val COMMON_PROPERTY_SDK_VERSION         = "#sdk_version_name"
    const val COMMON_PROPERTY_OS                  = "#os"
    const val COMMON_PROPERTY_OS_VERSION_NAME     = "#os_version_name"
    const val COMMON_PROPERTY_OS_VERSION_CODE     = "#os_version_code"
    const val COMMON_PROPERTY_DEVICE_MANUFACTURER = "#device_manufacturer"
    const val COMMON_PROPERTY_DEVICE_BRAND        = "#device_brand"
    const val COMMON_PROPERTY_BUILD_DEVICE        = "#build_device"
//    const val COMMON_PROPERTY_DIMS_DPI            = "#dims_dpi"
    const val COMMON_PROPERTY_NETWORK_TYPE        = "#network_type"
    const val COMMON_PROPERTY_SIMULATOR           = "#simulator"
    const val COMMON_PROPERTY_MEMORY_USED         = "#memory_used"
    const val COMMON_PROPERTY_FPS                 = "#fps"
    const val COMMON_PROPERTY_STORAGE_USED        = "#storage_used"
    const val COMMON_PROPERTY_DEVICE_MODEL        = "#device_model"
    const val COMMON_PROPERTY_SCREEN_HEIGHT       = "#screen_height"
    const val COMMON_PROPERTY_SCREEN_WIDTH        = "#screen_width"
    const val COMMON_PROPERTY_IS_FOREGROUND       = "#is_foreground"
    const val COMMON_PROPERTY_EVENT_DURATION      = "#event_duration"
    const val COMMON_PROPERTY_EVENT_ZONE_OFFSET      = "#zone_offset"

    //latest
    const val USER_PROPERTY_LATEST_DEBUG                = "#latest_debug"
    const val USER_PROPERTY_LATEST_FIREBASE_IID         = "#latest_firebase_iid"
    const val USER_PROPERTY_LATEST_APPSFLYER_ID         = "#latest_appsflyer_id"
    const val USER_PROPERTY_LATEST_KOCHAVA_ID           = "#latest_kochava_id"
    const val USER_PROPERTY_LATEST_APP_VERSION_CODE     = "#latest_app_version_code"
    const val USER_PROPERTY_LATEST_APP_VERSION_NAME     = "#latest_app_version_name"
    const val USER_PROPERTY_LATEST_ADJUST_ID            =  "#latest_adjust_id"

    //active
    const val USER_PROPERTY_ACTIVE_GAID                = "#active_gaid"
    const val USER_PROPERTY_ACTIVE_ANDROID_ID          = "#active_android_id"
    const val USER_PROPERTY_ACTIVE_MCC                 = "#active_mcc"
    const val USER_PROPERTY_ACTIVE_MNC                 = "#active_mnc"
    const val USER_PROPERTY_ACTIVE_OS_COUNTRY          = "#active_os_country_code"
    const val USER_PROPERTY_ACTIVE_OS_LANG             = "#active_os_lang_code"
    const val USER_PROPERTY_ACTIVE_PKG                 = "#active_bundle_id"
    const val USER_PROPERTY_ACTIVE_APP_VERSION_CODE    = "#active_app_version_code"
    const val USER_PROPERTY_ACTIVE_APP_VERSION_NAME    = "#active_app_version_name"
    const val USER_PROPERTY_ACTIVE_SDK_TYPE            = "#active_sdk_type"
    const val USER_PROPERTY_ACTIVE_SDK_VERSION         = "#active_sdk_version_name"
    const val USER_PROPERTY_ACTIVE_OS                  = "#active_os"
    const val USER_PROPERTY_ACTIVE_OS_VERSION_NAME     = "#active_os_version_name"
    const val USER_PROPERTY_ACTIVE_OS_VERSION_CODE     = "#active_os_version_code"
    const val USER_PROPERTY_ACTIVE_DEVICE_MANUFACTURER = "#active_device_manufacturer"
    const val USER_PROPERTY_ACTIVE_DEVICE_BRAND        = "#active_device_brand"
    const val USER_PROPERTY_ACTIVE_DEVICE_MODEL        = "#active_device_model"
    const val USER_PROPERTY_ACTIVE_SCREEN_HEIGHT       = "#active_screen_height"
    const val USER_PROPERTY_ACTIVE_SCREEN_WIDTH        = "#active_screen_width"
//    const val USER_PROPERTY_ACTIVE_DIMS_DPI            = "#active_dims_dpi"
    const val USER_PROPERTY_ACTIVE_MEMORY_USED         = "#active_memory_used"
    const val USER_PROPERTY_ACTIVE_STORAGE_USED        = "#active_storage_used"
    const val USER_PROPERTY_ACTIVE_NETWORK_TYPE        = "#active_network_type"
    const val USER_PROPERTY_ACTIVE_SIMULATOR           = "#active_simulator"
    const val USER_PROPERTY_ACTIVE_USER_AGENT          = "#active_user_agent"
    const val USER_PROPERTY_ACTIVE_BUILD_DEVICE        = "#active_build_device"

    //app_install
    const val ATTRIBUTE_PROPERTY_REFERRER_URL                = "#referrer_url"
    const val ATTRIBUTE_PROPERTY_REFERRER_CLICK_TIME         = "#referrer_click_time"
    const val ATTRIBUTE_PROPERTY_APP_INSTALL_TIME            = "#app_install_time"
    const val ATTRIBUTE_PROPERTY_INSTANT_EXPERIENCE_LAUNCHED = "#instant_experience_launched"
    const val ATTRIBUTE_PROPERTY_FAILED_REASON               = "#failed_reason"
    const val ATTRIBUTE_PROPERTY_CNL                         = "#cnl"

    //session_start
    const val SESSION_START_PROPERTY_IS_FIRST_TIME           = "#is_first_time"
    const val SESSION_START_PROPERTY_RESUME_FROM_BACKGROUND  = "#resume_from_background"
    const val SESSION_START_PROPERTY_START_REASON            = "#start_reason"

    //session_end
    const val SESSION_END_PROPERTY_SESSION_DURATION          = "#session_duration"

    //preset event name about Analytics
    const val EVENT_TYPE_TRACK = "track"
    const val EVENT_TYPE_USER  = "user"


    const val PRESET_EVENT_APP_INITIALIZE    = "#app_initialize"
    const val PRESET_EVENT_APP_INSTALL       = "#app_install"
    const val PRESET_EVENT_SESSION_START     = "#session_start"
    const val PRESET_EVENT_SESSION_END       = "#session_end"


    const val PRESET_EVENT_USER_ADD      = "#user_add"
    const val PRESET_EVENT_USER_SET      = "#user_set"
    const val PRESET_EVENT_USER_SET_ONCE = "#user_set_once"
    const val PRESET_EVENT_USER_UNSET    = "#user_unset"
    const val PRESET_EVENT_USER_APPEND   = "#user_append"
    const val PRESET_EVENT_USER_DEL      = "#user_delete"



    const val SDK_TYPE_ANDROID = "Android"

    const val TIME_FROM_ROI_NET_BODY = "[]"

}

@Deprecated("this class will be remove",ReplaceWith("DTChannel"))
object ROIQueryChannel{
    const val GP = "gp"
}

object DTChannel{
    const val GP = "gp"
}
object DTThirdPartyShareType {
    const val ADJUST = 1
}


interface OnDataTowerIdListener {
    fun onDataTowerIdCompleted(dataTowerId: String)
}
