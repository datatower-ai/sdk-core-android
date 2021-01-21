package com.nodetower.analytics.data.persistent

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.nodetower.base.data.PersistentIdentity
import java.util.concurrent.Future

class PersistentLoader {

    companion object {

        private val context: Context? = null
        private var storedPreferences: Future<SharedPreferences>? = null

        private var instance: PersistentLoader? = null
            get() {
                if (field == null) {
                    field = PersistentLoader()
                }
                return field
            }
        @Synchronized
        fun get(): PersistentLoader{
            return instance!!
        }
    }



    private fun PersistentLoader(context: Context) {
        PersistentLoader.context = context.applicationContext
        val sPrefsLoader = SharedPreferencesLoader()
        val prefsName = "com.sensorsdata.analytics.android.sdk.SensorsDataAPI"
        storedPreferences = sPrefsLoader.loadPreferences(context, prefsName)
    }

    fun initLoader(context: Context): PersistentLoader? {
        if (instance == null) {
            instance = PersistentLoader(context)
        }
        return instance
    }

    fun loadPersistent(persistentKey: String?): PersistentIdentity? {
        if (instance == null) {
            throw RuntimeException("you should call 'PersistentLoader.initLoader(Context)' first")
        }
        return if (TextUtils.isEmpty(persistentKey)) {
            null
        } else when (persistentKey) {
//            PersistentName.APP_END_DATA -> PersistentAppEndData(storedPreferences)
//            PersistentName.APP_PAUSED_TIME -> PersistentAppPaused(storedPreferences)
//            PersistentName.APP_START_TIME -> PersistentAppStartTime(storedPreferences)
//            PersistentName.DISTINCT_ID -> PersistentDistinctId(storedPreferences, context)
//            PersistentName.FIRST_DAY -> PersistentFirstDay(storedPreferences)
//            PersistentName.FIRST_INSTALL -> PersistentFirstTrackInstallation(storedPreferences)
//            PersistentName.FIRST_INSTALL_CALLBACK -> PersistentFirstTrackInstallationWithCallback(
//                storedPreferences
//            )
//            PersistentName.FIRST_START -> PersistentFirstStart(storedPreferences)
            PersistentName.LOGIN_ID -> PersistentLoginId(storedPreferences)
//            PersistentName.REMOTE_CONFIG -> PersistentRemoteSDKConfig(storedPreferences)
//            PersistentName.SUPER_PROPERTIES -> PersistentSuperProperties(storedPreferences)
//            PersistentName.SUB_PROCESS_FLUSH_DATA -> PersistentFlushDataState(storedPreferences)
            else -> null
        }
    }

    interface PersistentName {
        companion object {
//            val APP_END_DATA: String = DbParams.TABLE_APP_END_DATA
//            val APP_PAUSED_TIME: String = DbParams.TABLE_APP_END_TIME
//            val APP_START_TIME: String = DbParams.TABLE_APP_START_TIME
//            val SUB_PROCESS_FLUSH_DATA: String = DbParams.TABLE_SUB_PROCESS_FLUSH_DATA
//            const val DISTINCT_ID = "events_distinct_id"
//            const val FIRST_DAY = "first_day"
//            const val FIRST_START = "first_start"
//            const val FIRST_INSTALL = "first_track_installation"
//            const val FIRST_INSTALL_CALLBACK = "first_track_installation_with_callback"
            const val LOGIN_ID = "events_login_id"
//            const val REMOTE_CONFIG = "sensorsdata_sdk_configuration"
//            const val SUPER_PROPERTIES = "super_properties"
        }
    }
}