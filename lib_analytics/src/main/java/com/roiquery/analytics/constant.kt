package com.roiquery.analytics

internal object Constant {

    const val LOG_TAG = "Roiquery_Analytics"

    //事件上报路径
    private const val ANALYTICS_BASE_URL = "https://api.roiquery.com"
    const val EVENT_REPORT_URL = "$ANALYTICS_BASE_URL/report"

    //事件上报条数
    const val EVENT_REPORT_SIZE = 10

    const val CONFIG_BUNDLE_KEY_MAIN_PROCESS_NAME =
        "com.roiquery.analytics.android.MainProcessName"

    //预置事件名称
    const val PRESET_EVENT_APP_FIRST_OPEN = "app_first_open"
    const val PRESET_EVENT_APP_OPEN = "app_open"
    const val PRESET_EVENT_APP_ATTRIBUTE = "app_attribute"
    const val PRESET_EVENT_APP_ENGAGEMENT = "app_engagement"
    const val PRESET_EVENT_APP_CLOSE = "app_close"
    const val PRESET_EVENT_PAGE_OPEN = "page_open"
    const val PRESET_EVENT_PAGE_CLOSE = "page_close"
    const val PRESET_EVENT_USER_PROPERTIES = "user_properties"

    //活跃度事件采集间隔
    const val APP_ENGAGEMENT_INTERVAL_TIME = 1 * 60 * 1000L //五分钟

    const val SDK_TYPE_ANDROID = "Android"
    const val SDK_TYPE_UNITY = "Unity"
    const val SDK_TYPE_FLUTTER = "Flutter"
}