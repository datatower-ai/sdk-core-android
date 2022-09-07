package com.roiquery.analytics

internal object Constant {

    const val LOG_TAG = "ROIQuery"

    //ROIQuery base url
    private val ANALYTICS_BASE_URL = if ("0" == BuildConfig.LINK_SITE) "https://test.roiquery.com" else ( if ("1" == BuildConfig.LINK_SITE)  "https://report-inner.roiquery.com" else "https://report.roiquery.com")

//    private const val ANALYTICS_BASE_URL = "https://test.roiquery.com"
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
    const val PRE_EVENT_INFO_NAME    = "event_name"
    const val EVENT_INFO_TYPE        = "#event_type"
    const val PRE_EVENT_INFO_SYN     = "event_syn"
    const val EVENT_INFO_SYN         = "#event_syn"
    const val EVENT_INFO_PROPERTIES  = "properties"

    const val EVENT_BODY  = "body"
    const val EVENT_TIME_CALIBRATED  = "time_calibrated"


    const val COMMON_PROPERTY_EVENT_SESSION       = "#app_session"
    const val COMMON_PROPERTY_FIREBASE_IID        = "#firebase_iid"
    const val COMMON_PROPERTY_FCM_TOKEN           = "#fcm_token"
    const val COMMON_PROPERTY_APPSFLYER_ID        = "#appsflyer_id"
    const val COMMON_PROPERTY_KOCHAVA_ID          = "#kochava_id"
    const val COMMON_PROPERTY_APP_SET_ID          = "#app_set_id"
    const val COMMON_PROPERTY_ROIQUERY_ID         = "#instance_id"
    const val COMMON_PROPERTY_MCC                 = "#mcc"
    const val COMMON_PROPERTY_MNC                 = "#mnc"
    const val COMMON_PROPERTY_OS_COUNTRY          = "#os_country"
    const val COMMON_PROPERTY_OS_LANG             = "#os_lang"
    const val COMMON_PROPERTY_APP_VERSION_CODE    = "#app_version_code"
    const val COMMON_PROPERTY_APP_VERSION_NAME    = "#app_version_name"
    const val COMMON_PROPERTY_SDK_TYPE            = "#sdk_type"
    const val COMMON_PROPERTY_SDK_VERSION         = "#sdk_version"
    const val COMMON_PROPERTY_OS                  = "#os"
    const val COMMON_PROPERTY_OS_VERSION_NAME     = "#os_version_name"
    const val COMMON_PROPERTY_OS_VERSION_CODE     = "#os_version_code"
    const val COMMON_PROPERTY_DEVICE_MANUFACTURER = "#device_manufacturer"
    const val COMMON_PROPERTY_DEVICE_BRAND        = "#device_brand"
    const val COMMON_PROPERTY_DIMS_DPI            = "#dims_dpi"
    const val COMMON_PROPERTY_NETWORK_TYPE        = "#network_type"
    const val COMMON_PROPERTY_SIMULATOR           = "#simulator"
    const val COMMON_PROPERTY_MEMORY_USED         = "#memory_used"
    const val COMMON_PROPERTY_STORAGE_USED        = "#storage_used"
    const val COMMON_PROPERTY_USER_AGENT          = "#user_agent"
    const val COMMON_PROPERTY_DEVICE_MODEL        = "#device_model"
    const val COMMON_PROPERTY_SCREEN_HEIGHT       = "#screen_height"
    const val COMMON_PROPERTY_SCREEN_WIDTH        = "#screen_width"
    const val COMMON_PROPERTY_IS_FOREGROUND       = "#is_foreground"

    //latest
    const val USER_PROPERTY_LATEST_INSTANCE_ID    = "#latest_instance_id"
    const val USER_PROPERTY_LATEST_ACID           = "#latest_acid"
    const val USER_PROPERTY_LATEST_DID            = "#latest_did"
    const val USER_PROPERTY_LATEST_APP_SET_ID     = "#latest_app_set_id"
    const val USER_PROPERTY_LATEST_GAID           = "#latest_gaid"
    const val USER_PROPERTY_LATEST_OAID           = "#latest_oaid"
    const val USER_PROPERTY_LATEST_FIREBASE_IID   = "#latest_firebase_iid"
    const val USER_PROPERTY_LATEST_FCM_TOKEN      = "#latest_fcm_token"
    const val USER_PROPERTY_LATEST_APPSFLYER_ID   = "#latest_appsflyer_id"
    const val USER_PROPERTY_LATEST_KOCHAVA_ID     = "#latest_kochava_id"
    const val USER_PROPERTY_LATEST_APP_VERSION_CODE     = "#latest_app_version_code"
    const val USER_PROPERTY_LATEST_APP_VERSION_NAME     = "#latest_app_version_name"

    //active
    const val USER_PROPERTY_ACTIVE_MCC                 = "#active_mcc"
    const val USER_PROPERTY_ACTIVE_MNC                 = "#active_mnc"
    const val USER_PROPERTY_ACTIVE_OS_COUNTRY          = "#active_os_country"
    const val USER_PROPERTY_ACTIVE_OS_LANG             = "#active_os_lang"
    const val USER_PROPERTY_ACTIVE_EVENT_TIME          = "#active_event_time"
    const val USER_PROPERTY_ACTIVE_PKG                 = "#active_pkg"
    const val USER_PROPERTY_ACTIVE_APP_VERSION_CODE    = "#active_app_version_code"
    const val USER_PROPERTY_ACTIVE_APP_VERSION_NAME    = "#active_app_version_name"
    const val USER_PROPERTY_ACTIVE_SDK_TYPE            = "#active_sdk_type"
    const val USER_PROPERTY_ACTIVE_SDK_VERSION         = "#active_sdk_version"
    const val USER_PROPERTY_ACTIVE_OS                  = "#active_os"
    const val USER_PROPERTY_ACTIVE_OS_VERSION_NAME     = "#active_os_version_name"
    const val USER_PROPERTY_ACTIVE_OS_VERSION_CODE     = "#active_os_version_code"
    const val USER_PROPERTY_ACTIVE_DEVICE_MANUFACTURER = "#active_device_manufacturer"
    const val USER_PROPERTY_ACTIVE_DEVICE_BRAND        = "#active_device_brand"
    const val USER_PROPERTY_ACTIVE_DEVICE_MODEL        = "#active_device_model"
    const val USER_PROPERTY_ACTIVE_SCREEN_HEIGHT       = "#active_screen_height"
    const val USER_PROPERTY_ACTIVE_SCREEN_WIDTH        = "#active_screen_width"
    const val USER_PROPERTY_ACTIVE_DIMS_DPI            = "#active_dims_dpi"
    const val USER_PROPERTY_ACTIVE_MEMORY_USED         = "#active_memory_used"
    const val USER_PROPERTY_ACTIVE_STORAGE_USED        = "#active_storage_used"
    const val USER_PROPERTY_ACTIVE_NETWORK_TYPE        = "#active_network_type"
    const val USER_PROPERTY_ACTIVE_SIMULATOR           = "#active_simulator"
    const val USER_PROPERTY_ACTIVE_USER_AGENT          = "#active_user_agent"
    const val USER_PROPERTY_ACTIVE_BUILD_DEVICE        = "#active_build_device"

    const val ATTRIBUTE_PROPERTY_FIRST_OPEN_TIME             = "#first_open_time"
    const val ATTRIBUTE_PROPERTY_REFERRER_URL                = "#referrer_url"
    const val ATTRIBUTE_PROPERTY_REFERRER_CLICK_TIME         = "#referrer_click_time"
    const val ATTRIBUTE_PROPERTY_APP_INSTALL_TIME            = "#app_install_time"
    const val ATTRIBUTE_PROPERTY_INSTANT_EXPERIENCE_LAUNCHED = "#instant_experience_launched"
    const val ATTRIBUTE_PROPERTY_FAILED_REASON               = "#failed_reason"
    const val ATTRIBUTE_PROPERTY_CNL                         = "#cnl"


    //preset event name about Analytics
    const val PRESET_EVENT_TAG = "#"
    const val EVENT_TYPE_TRACK = "track"
    const val EVENT_TYPE_USER  = "user"

    const val PRESET_EVENT_APP_FIRST_OPEN  = "#app_first_open"
    const val PRESET_EVENT_APP_OPEN        = "#app_open"
    const val PRESET_EVENT_APP_ATTRIBUTE   = "#app_attribute"
    const val PRESET_EVENT_APP_ENGAGEMENT  = "#app_engagement"
    const val PRESET_EVENT_APP_CLOSE       = "#app_close"
    const val PRESET_EVENT_PAGE_OPEN       = "#page_open"
    const val PRESET_EVENT_PAGE_CLOSE      = "#page_close"
    const val PRESET_EVENT_USER_PROPERTIES = "#user_properties"
    const val PRESET_EVENT_APP_QUALITY     = "#app_quality"
    const val PRESET_EVENT_APP_STATE_CHANGED  =  "#app_state_changed"

    const val PRESET_EVENT_USER_ADD      = "#user_add"
    const val PRESET_EVENT_USER_SET      = "#user_set"
    const val PRESET_EVENT_USER_SET_ONCE = "#user_set_once"
    const val PRESET_EVENT_USER_UNSET    = "#user_unset"
    const val PRESET_EVENT_USER_APPEND   = "#user_append"
    const val PRESET_EVENT_USER_DEL      = "#user_delete"

    //engagement interval time
    const val APP_ENGAGEMENT_INTERVAL_TIME_INT = 5 * 60 * 1000 // 5min
    const val APP_ENGAGEMENT_INTERVAL_TIME_LONG = 5 * 60 * 1000L // 5min

    const val SDK_TYPE_ANDROID = "Android"

    const val TIME_OFFSET_DEFAULT_VALUE = "0"

}


object ROIQueryChannel{
    const val GP = "gp"
    const val APPSTORE = "app_store"
}