package com.nodetower.analytics.data.persistent

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.nodetower.analytics.data.DbParams
import com.nodetower.base.data.PersistentIdentity
import java.util.concurrent.Future


class PersistentLoader private constructor() {

    interface PersistentName {
        companion object {
            const val LOGIN_ID = "events_login_id"
            const val GAID = "events_gaid"
            const val OAID = "events_oaid"
        }
    }

   private constructor(context: Context) : this() {
        mContext = context.applicationContext
        val sPrefsLoader = SharedPreferencesLoader()
        val prefsName = "com.nodetower.analytics.android.sdk.RoiqueryAnalyticsAPI"
        storedPreferences = context.let { sPrefsLoader.loadPreferences(it, prefsName) }
    }

    companion object {
        @Volatile
        private var instance: PersistentLoader? = null
        private var mContext: Context? = null
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
                PersistentName.LOGIN_ID -> storedPreferences?.let { PersistentLoginId(it) }
                PersistentName.OAID -> storedPreferences?.let { PersistentOaid(it) }
                PersistentName.GAID -> storedPreferences?.let { PersistentGaid(it) }
                else -> null
        }
    }

}}
