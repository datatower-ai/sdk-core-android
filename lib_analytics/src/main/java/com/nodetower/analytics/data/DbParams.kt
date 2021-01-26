package com.nodetower.analytics.data

import android.net.Uri


class DbParams private constructor(packageName: String) {
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
        const val TABLE_CHANNEL_PERSISTENT = "t_channel"
        const val DB_OUT_OF_MEMORY_ERROR = -2
        const val KEY_CHANNEL_EVENT_NAME = "event_name"
        const val KEY_CHANNEL_RESULT = "result"

        /* 数据库名称 */
        const val DATABASE_NAME = "roiquerydata"

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

        /* Event 表字段 */
        const val KEY_DATA = "data"
        const val KEY_CREATED_AT = "created_at"

        /* 数据库状态 */
        const val DB_UPDATE_ERROR = -1
        const val VALUE = "value"
        const val GZIP_DATA_EVENT = "1"
        const val GZIP_DATA_ENCRYPT = "9"

        /* 删除所有数据 */
        const val DB_DELETE_ALL = "DB_DELETE_ALL"
        private var instance: DbParams? = null
        fun getInstance(packageName: String): DbParams? {
            if (instance == null) {
                instance = DbParams(packageName)
            }
            return instance
        }

        fun getInstance(): DbParams? {
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
