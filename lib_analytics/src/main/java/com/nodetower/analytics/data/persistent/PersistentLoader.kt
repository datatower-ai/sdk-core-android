package com.nodetower.analytics.data.persistent

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.nodetower.analytics.data.DbParams
import com.nodetower.base.data.PersistentIdentity
import java.util.concurrent.Future


class PersistentLoader private constructor(context: Context) {

    interface PersistentName {
        companion object {
            const val APP_END_DATA = DbParams.TABLE_APP_END_DATA
            const val APP_PAUSED_TIME = DbParams.TABLE_APP_END_TIME
            const val APP_START_TIME = DbParams.TABLE_APP_START_TIME
            const val SUB_PROCESS_FLUSH_DATA = DbParams.TABLE_SUB_PROCESS_FLUSH_DATA
            const val DISTINCT_ID = "events_distinct_id"
            const val FIRST_DAY = "first_day"
            const val FIRST_START = "first_start"
            const val FIRST_INSTALL = "first_track_installation"
            const val FIRST_INSTALL_CALLBACK = "first_track_installation_with_callback"
            const val LOGIN_ID = "events_login_id"
            const val REMOTE_CONFIG = "sensorsdata_sdk_configuration"
            const val SUPER_PROPERTIES = "super_properties"
        }
    }

    companion object {
        @Volatile
        private var instance: PersistentLoader? = null
        private var context: Context? = null
        private var storedPreferences: Future<SharedPreferences>?  = null

        fun initLoader(context: Context): PersistentLoader? {
            if (instance == null) {
                instance = PersistentLoader(context)
            }
            return instance
        }

        fun loadPersistent(persistentKey: String?): PersistentIdentity<*>? {
            if (instance == null) {
                throw RuntimeException("you should call 'PersistentLoader.initLoader(Context)' first")
            }
            return if (TextUtils.isEmpty(persistentKey)) {
                null
            } else when (persistentKey) {
//                PersistentName.APP_END_DATA -> PersistentAppEndData(storedPreferences)
//                PersistentName.APP_PAUSED_TIME -> PersistentAppPaused(storedPreferences)
//                PersistentName.APP_START_TIME -> PersistentAppStartTime(storedPreferences)
//                PersistentName.DISTINCT_ID -> PersistentDistinctId(storedPreferences, context)
//                PersistentName.FIRST_DAY -> PersistentFirstDay(storedPreferences)
//                PersistentName.FIRST_INSTALL -> PersistentFirstTrackInstallation(
//                    storedPreferences
//                )
//                PersistentName.FIRST_INSTALL_CALLBACK -> PersistentFirstTrackInstallationWithCallback(
//                    storedPreferences
//                )
//                PersistentName.FIRST_START -> PersistentFirstStart(storedPreferences)
                PersistentName.LOGIN_ID -> storedPreferences?.let { PersistentLoginId(it) }
//                PersistentName.REMOTE_CONFIG -> PersistentRemoteSDKConfig(storedPreferences)
//                PersistentName.SUPER_PROPERTIES -> PersistentSuperProperties(storedPreferences)
//                PersistentName.SUB_PROCESS_FLUSH_DATA -> PersistentFlushDataState(
//                    storedPreferences
//                )
                else -> null
            }
        }
    }

    init {
        Companion.context = context.getApplicationContext()
        val sPrefsLoader = SharedPreferencesLoader()
        val prefsName = "com.sensorsdata.analytics.android.sdk.SensorsDataAPI"
        storedPreferences = sPrefsLoader.loadPreferences(context, prefsName)
    }
}
