package com.nodetower.analytics

object Constant {

    const val LOG_TAG = "Roiquery_Analytics"
    const val URL_REPORT = "/report"

    const val CONFIG_BUNDLE_KEY_MAIN_PROCESS_NAME =
        "com.nodetower.analytics.android.MainProcessName"

    //预置事件名称
    const val PRESET_EVENT_APP_FIRST_OPEN = "#app_first_open"
    const val PRESET_EVENT_APP_OPEN = "#app_open"
    const val PRESET_EVENT_APP_ATTRIBUTE = "#app_attribute"
    const val PRESET_EVENT_APP_ENGAGEMENT = "#app_engagement"
    const val PRESET_EVENT_APP_CLOSE = "#app_close"
    const val PRESET_EVENT_PAGE_OPEN = "#page_open"
    const val PRESET_EVENT_PAGE_CLOSE = "#page_close"
    const val PRESET_EVENT_AD_SHOW = "#ad_show"
    const val PRESET_EVENT_AD_CLICK = "#ad_click"

    const val APP_ENGAGEMENT_INTERVAL_TIME = 5 * 60 * 1000L //五分钟
}