package com.roiquery.analytics

import androidx.annotation.IntDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

internal object Constant {

    const val LOG_TAG = "Roiquery_Analytics"

    //事件上报路径
    private const val ANALYTICS_BASE_URL = "https://api.roiquery.com"

    const val EVENT_REPORT_URL = "$ANALYTICS_BASE_URL/report"

    const val NTP_HOST = "time.google.com"
    const val NTP_TIME_OUT_M = 31428

    //事件上报条数
    const val EVENT_REPORT_SIZE = 10

    const val CONFIG_BUNDLE_KEY_MAIN_PROCESS_NAME =
        "com.roiquery.analytics.android.MainProcessName"

    //预置事件名称
    const val PRESET_EVENT_TAG = "@.#"
    const val PRESET_EVENT_APP_FIRST_OPEN = PRESET_EVENT_TAG + "app_first_open"
    const val PRESET_EVENT_APP_OPEN = PRESET_EVENT_TAG + "app_open"
    const val PRESET_EVENT_APP_ATTRIBUTE = PRESET_EVENT_TAG + "app_attribute"
    const val PRESET_EVENT_APP_ENGAGEMENT = PRESET_EVENT_TAG + "app_engagement"
    const val PRESET_EVENT_APP_CLOSE = PRESET_EVENT_TAG + "app_close"
    const val PRESET_EVENT_PAGE_OPEN = PRESET_EVENT_TAG + "page_open"
    const val PRESET_EVENT_PAGE_CLOSE = PRESET_EVENT_TAG + "page_close"
    const val PRESET_EVENT_USER_PROPERTIES = PRESET_EVENT_TAG + "user_properties"


    //活跃度事件采集间隔
    const val APP_ENGAGEMENT_INTERVAL_TIME = 1 * 60 * 1000L //五分钟

    const val SDK_TYPE_ANDROID = "Android"
    const val SDK_TYPE_UNITY = "Unity"
    const val SDK_TYPE_FLUTTER = "Flutter"


    object MemoryConstants {
        const val BYTE = 1
        const val KB = 1024
        const val MB = 1048576
        const val GB = 1073741824

        @IntDef(*[BYTE, KB, MB, GB])
        @Retention(RetentionPolicy.SOURCE)
        annotation class Unit
    }
}