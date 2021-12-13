package com.roiquery.analytics.data

import android.net.Uri


class DataParams private constructor(packageName: String) {
    /**
     * 获取 Event Uri
     *
     * @return Uri
     */
    val eventUri: Uri

    /**
     * 获取 AppStart Uri
     *
     * @return Uri
     */
    val activityStartCountUri: Uri

    /**
     * 获取 AppStartTime Uri
     *
     * @return Uri
     */
    val appStartTimeUri: Uri

    /**
     * 获取 AppPausedTime Uri
     *
     * @return uri
     */
    val appPausedUri: Uri

    /**
     * 开启数据采集 Uri
     * @return Uri
     */
    val dataCollectUri: Uri

    /**
     * 获取 AppEndData Uri
     *
     * @return Uri
     */
    val appEndDataUri: Uri

    /**
     * 获取 SessionTime Uri
     *
     * @return Uri
     */
    val sessionTimeUri: Uri

    /**
     * 获取 LoginId 的 Uri
     *
     * @return Uri
     */
    val loginIdUri: Uri

    /**
     * 获取 oaid 的 Uri
     *
     * @return Uri
     */
    val oaidUri: Uri

    /**
     * 获取 gaid 的 Uri
     *
     * @return Uri
     */
    val gaidUri: Uri

    /**
     * 获取 Channel 持久化 Uri
     *
     * @return Uri
     */
    val channelPersistentUri: Uri

    /**
     * 多进程上报数据标记位 Uri
     *
     * @return Uri
     */
    val subProcessUri: Uri

    /**
     * 是否首个启动的进程 Uri
     *
     * @return Uri
     */
    val firstProcessUri: Uri

    companion object {
        /* 数据库中的表名 */
        const val TABLE_EVENTS = "events"
        const val TABLE_CONFIGS = "configs"
        const val TABLE_CHANNEL_PERSISTENT = "t_channel"

        const val KEY_CHANNEL_EVENT_NAME = "event_name"
        const val KEY_CHANNEL_RESULT = "result"

        /* 数据库名称 */
        const val DATABASE_NAME = "roiquery_analytics_db"

        /* 数据库版本号 */
        const val DATABASE_VERSION = 1

        const val TABLE_ACTIVITY_START_COUNT = "activity_started_count"
        const val TABLE_APP_START_TIME = "app_start_time"
        const val TABLE_APP_END_TIME = "app_end_time"
        const val TABLE_APP_END_DATA = "app_end_data"
        const val TABLE_SUB_PROCESS_FLUSH_DATA = "sub_process_flush_data"
        const val TABLE_FIRST_PROCESS_START = "first_process_start"
        const val TABLE_SESSION_INTERVAL_TIME = "session_interval_time"
        const val TABLE_DATA_COLLECT = "data_collect"
        const val TABLE_LOGIN_ID = "events_login_id"
        const val TABLE_GAID = "events_gaid"
        const val TABLE_OAID = "events_oaid"
        const val TABLE_APP_FIRST_OPEN = "events_app_first_open"

        /* Event 表字段 */
        const val KEY_DATA = "data"
        const val KEY_CREATED_AT = "created_at"
        /* Config 表字段 */
        const val KEY_CONFIG_NAME = "name"
        const val KEY_CONFIG_VALUE = "value"

        const val CONFIG_FIRST_OPEN_TIME = "first_open_time"
        const val CONFIG_GAID = "gaid"
        const val CONFIG_OAID = "oaid"
        const val CONFIG_EVENT_SESSION = "event_session"
        const val CONFIG_ROIQUERY_ID = "roiquery_id"
        const val CONFIG_FIREBASE_IID = "firebase_iid"
        const val CONFIG_FCM_TOKEN = "fcm_token"
        const val CONFIG_APPSFLYER_ID = "appsflyer_id"
        const val CONFIG_KOCHAVA_ID = "kochava_id"
        const val CONFIG_ACCOUNT_ID = "account_id"
        const val CONFIG_ENABLE_UPLOADS = "enable_upload"
        const val CONFIG_ENABLE_TRACK = "enable_track"
        const val CONFIG_FIRST_OPEN = "first_open"
        const val CONFIG_ATTRIBUTE = "app_attribute"
        const val CONFIG_ATTRIBUTE_COUNT = "app_attribute_count"
        const val CONFIG_IS_FOREGROUND = "is_foreground"
        const val CLOUD_CONFIG_AES_KEY = "cloud_config_aes_key"
        const val TIME_SERVER_LOCAL_OFFSET = "time_server_local_offset"
        const val LAST_APP_ENGAGEMENT_TIME = "last_app_engagement_time"



        /* 数据库状态 */
        const val DB_INSERT_SUCCEED = 0
        const val DB_UPDATE_ERROR = -1
        const val DB_OUT_OF_MEMORY_ERROR = -2
        const val DB_INSERT_EXCEPTION = -3
        const val DB_INSERT_ERROR = -3L
        const val DB_UPDATE_CONFIG_ERROR = -4
        const val VALUE = "value"
        const val GZIP_DATA_EVENT = "1"
        const val GZIP_DATA_ENCRYPT = "9"

        /* 删除所有数据 */
        const val DB_DELETE_ALL = "DB_DELETE_ALL"
        private var instance: DataParams? = null
        fun getInstance(packageName: String): DataParams? {
            if (instance == null) {
                instance = DataParams(packageName)
            }
            return instance
        }

        fun getInstance(): DataParams? {
            checkNotNull(instance) { "The static method getInstance(String packageName) should be called before calling getInstance()" }
            return instance
        }
    }

    init {
        eventUri =
            Uri.parse("content://$packageName.AnalyticsDataContentProvider/$TABLE_EVENTS")
        activityStartCountUri =
            Uri.parse("content://$packageName.AnalyticsDataContentProvider/$TABLE_ACTIVITY_START_COUNT")
        appStartTimeUri =
            Uri.parse("content://$packageName.AnalyticsDataContentProvider/$TABLE_APP_START_TIME")
        appEndDataUri =
            Uri.parse("content://$packageName.AnalyticsDataContentProvider/$TABLE_APP_END_DATA")
        appPausedUri =
            Uri.parse("content://$packageName.AnalyticsDataContentProvider/$TABLE_APP_END_TIME")
        sessionTimeUri =
            Uri.parse("content://$packageName.AnalyticsDataContentProvider/$TABLE_SESSION_INTERVAL_TIME")
        loginIdUri =
            Uri.parse("content://$packageName.AnalyticsDataContentProvider/$TABLE_LOGIN_ID")
        gaidUri =
            Uri.parse("content://$packageName.AnalyticsDataContentProvider/$TABLE_GAID")
        oaidUri =
            Uri.parse("content://$packageName.AnalyticsDataContentProvider/$TABLE_OAID")
        channelPersistentUri =
            Uri.parse("content://$packageName.AnalyticsDataContentProvider/$TABLE_CHANNEL_PERSISTENT")
        subProcessUri =
            Uri.parse("content://$packageName.AnalyticsDataContentProvider/$TABLE_SUB_PROCESS_FLUSH_DATA")
        firstProcessUri =
            Uri.parse("content://$packageName.AnalyticsDataContentProvider/$TABLE_FIRST_PROCESS_START")
        dataCollectUri =
            Uri.parse("content://$packageName.AnalyticsDataContentProvider/$TABLE_DATA_COLLECT")
    }
}
