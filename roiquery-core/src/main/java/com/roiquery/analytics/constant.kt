package com.roiquery.analytics

internal object Constant {

    const val LOG_TAG = "Roiquery_Analytics"

    //ROIQuery base url
    private const val ANALYTICS_BASE_URL = "https://api.roiquery.com"
    //upload url
    const val EVENT_REPORT_URL = "$ANALYTICS_BASE_URL/report"
    // cloudConfig url
    const val CLOUD_CONFIG_URL = "$ANALYTICS_BASE_URL/cfg"
//    const val CLOUD_CONFIG_URL = "http://192.168.60.70:8000/apitest/cfg"

    //ntp server url
    const val NTP_HOST = "time.google.com"
    const val NTP_TIME_OUT_M = 31428


    //event upload size
    const val EVENT_REPORT_SIZE = 10
    //event upload try times when filed
    const val EVENT_REPORT_TRY_COUNT = 3

    const val CONFIG_BUNDLE_KEY_MAIN_PROCESS_NAME =
        "com.roiquery.analytics.android.MainProcessName"

    //preset event name about Analytics
    const val PRESET_EVENT_TAG = "@.#"
    const val PRESET_EVENT_APP_FIRST_OPEN = PRESET_EVENT_TAG + "app_first_open"
    const val PRESET_EVENT_APP_OPEN = PRESET_EVENT_TAG + "app_open"
    const val PRESET_EVENT_APP_ATTRIBUTE = PRESET_EVENT_TAG + "app_attribute"
    const val PRESET_EVENT_APP_ENGAGEMENT = PRESET_EVENT_TAG + "app_engagement"
    const val PRESET_EVENT_APP_CLOSE = PRESET_EVENT_TAG + "app_close"
    const val PRESET_EVENT_PAGE_OPEN = PRESET_EVENT_TAG + "page_open"
    const val PRESET_EVENT_PAGE_CLOSE = PRESET_EVENT_TAG + "page_close"
    const val PRESET_EVENT_USER_PROPERTIES = PRESET_EVENT_TAG + "user_properties"


    //engagement interval time
    const val APP_ENGAGEMENT_INTERVAL_TIME = 1 * 60 * 1000L // 5min

    const val SDK_TYPE_ANDROID = "Android"
    const val SDK_TYPE_UNITY = "Unity"
    const val SDK_TYPE_FLUTTER = "Flutter"

    // sdk switch，cloudConfig to config
    const val ENABLE_ANALYTICS_SDK_KEY = "enable_analytics_sdk"
}