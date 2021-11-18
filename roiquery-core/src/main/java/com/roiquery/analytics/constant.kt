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
    const val EVENT_INFO_SYN         = "#event_syn"
    const val EVENT_INFO_PROPERTIES  = "properties"

    const val EVENT_BODY  = "body"
    const val EVENT_TIME_CALIBRATED  = "time_calibrated"


    const val COMMON_PROPERTY_EVENT_SESSION       = "#event_session"
    const val COMMON_PROPERTY_FIREBASE_IID        = "#firebase_iid"
    const val COMMON_PROPERTY_APPSFLYER_ID        = "#appsflyer_id"
    const val COMMON_PROPERTY_KOCHAVA_ID          = "#kochava_id"
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
    const val COMMON_PROPERTY_DEVICE_MODEL        = "#device_model"
    const val COMMON_PROPERTY_SCREEN_HEIGHT       = "#screen_height"
    const val COMMON_PROPERTY_SCREEN_WIDTH        = "#screen_width"


    const val ATTRIBUTE_PROPERTY_FIRST_OPEN_TIME             = "first_open_time"
    const val ATTRIBUTE_PROPERTY_REFERRER_URL                = "referrer_url"
    const val ATTRIBUTE_PROPERTY_REFERRER_CLICK_TIME         = "referrer_click_time"
    const val ATTRIBUTE_PROPERTY_APP_INSTALL_TIME            = "app_install_time"
    const val ATTRIBUTE_PROPERTY_INSTANT_EXPERIENCE_LAUNCHED = "instant_experience_launched"
    const val ATTRIBUTE_PROPERTY_FAILED_REASON               = "failed_reason"
    const val ATTRIBUTE_PROPERTY_CNL                         = "cnl"
    const val ATTRIBUTE_PROPERTY_USER_AGENT                  =  "user_agent"


    const val ENGAGEMENT_PROPERTY_IS_FOREGROUND    = "is_foreground"


    const val USER_PROPERTIES_PROPERTY_KEY         = "property_key"
    const val USER_PROPERTIES_PROPERTY_VALUE       = "property_value"

    const val APP_QUALITY_INFO                     = "quality_info"


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


    //engagement interval time
    const val APP_ENGAGEMENT_INTERVAL_TIME = 5 * 60 * 1000L // 5min

    const val SDK_TYPE_ANDROID = "Android"
    const val SDK_TYPE_UNITY   = "Unity"
    const val SDK_TYPE_FLUTTER = "Flutter"

    // sdk switch，cloudConfig to config
    const val ENABLE_ANALYTICS_SDK_KEY = "enable_analytics_sdk"

    val BASE_EVENT_NAME: ArrayList<String> = object : ArrayList<String>() {
        init {
            add(PRESET_EVENT_APP_FIRST_OPEN)
            add(PRESET_EVENT_APP_OPEN)
            add(PRESET_EVENT_APP_ATTRIBUTE)
            add(PRESET_EVENT_APP_ENGAGEMENT)
            add(PRESET_EVENT_APP_CLOSE)
            add(PRESET_EVENT_PAGE_OPEN)
            add(PRESET_EVENT_PAGE_CLOSE)
            add(PRESET_EVENT_USER_PROPERTIES)
        }
    }

}


object ROIQueryChannel{
    const val GP = "gp"
    const val APPSTORE = "app_store"
}