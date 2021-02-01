package com.nodetower.analytics.data.persistent

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.nodetower.analytics.data.DataParams.Companion.TABLE_APP_FIRST_OPEN
import com.nodetower.analytics.data.DataParams.Companion.TABLE_GAID
import com.nodetower.analytics.data.DataParams.Companion.TABLE_LOGIN_ID
import com.nodetower.analytics.data.DataParams.Companion.TABLE_OAID
import com.nodetower.base.data.PersistentIdentity
import java.util.concurrent.Future


class PersistentLoader private constructor() {


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
                TABLE_LOGIN_ID -> storedPreferences?.let { PersistentLoginId(it) }
                TABLE_OAID -> storedPreferences?.let { PersistentOaid(it) }
                TABLE_GAID -> storedPreferences?.let { PersistentGaid(it) }
                TABLE_APP_FIRST_OPEN -> storedPreferences?.let { PersistentAppFirstOpen(it) }
                else -> null
        }
    }

}}
