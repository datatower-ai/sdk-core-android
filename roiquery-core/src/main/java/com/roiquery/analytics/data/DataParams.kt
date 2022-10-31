package com.roiquery.analytics.data



class DataParams private constructor(packageName: String) {


    companion object {

        /* 数据库名称 */
        const val DATABASE_NAME = "roiquery_analytics_db"

        /* Event 表字段 */
        const val KEY_DATA = "data"

        const val CONFIG_FIRST_OPEN_TIME = "first_open_time"
        const val CONFIG_IS_FIRST_OPEN_TIME_VERIFIED = "is_first_open_time_verified"
        const val CONFIG_GAID = "gaid"
        const val CONFIG_OAID = "oaid"
        const val CONFIG_DT_ID = "dt_id"
        const val CONFIG_ROIQUERY_ID = "roiquery_id"
        const val CONFIG_FIREBASE_IID = "firebase_iid"
        const val CONFIG_FCM_TOKEN = "fcm_token"
        const val CONFIG_APPSFLYER_ID = "appsflyer_id"
        const val CONFIG_KOCHAVA_ID = "kochava_id"
        const val CONFIG_APP_SET_ID = "app_set_id"
        const val CONFIG_ACCOUNT_ID = "account_id"
        const val CONFIG_ENABLE_UPLOADS = "enable_upload"
        const val CONFIG_ENABLE_TRACK = "enable_track"
        const val CONFIG_FIRST_OPEN = "first_open"
        const val CONFIG_APP_INSTALL_INSERT_STATE = "app_install_insert_state"
        const val CONFIG_FIRST_SESSION_START_INSERT_STATE = "first_session_start_insert_state"
        const val CONFIG_IS_FOREGROUND = "is_foreground"
        const val CLOUD_CONFIG_AES_KEY = "cloud_config_aes_key"
        const val USER_AGENT_WEBVIEW = "user_agent_webview"



        /* 数据库状态 */
        const val DB_INSERT_SUCCEED = 0
        const val DB_INSERT_ERROR = -1
        const val DB_OUT_OF_ROW_ERROR = -2
        const val DB_INSERT_EXCEPTION = -3
        const val DB_ADD_JSON_ERROR = -4
        const val DB_INSERT_DATA_REPEAT = -5
        const val VALUE = "value"

        const val CONFIG_MAX_ROWS = 500

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

}
