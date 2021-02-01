package com.nodetower.analytics.data.persistent

import android.content.SharedPreferences
import com.nodetower.analytics.data.DataParams
import com.nodetower.base.data.PersistentIdentity
import java.util.concurrent.Future


class PersistentAppFirstOpen(loadStoredPreferences: Future<SharedPreferences>) :
    PersistentIdentity<Boolean?>(
        loadStoredPreferences,
        DataParams.TABLE_APP_FIRST_OPEN,
        object : PersistentSerializer<Boolean?> {
            override fun load(value: String?): Boolean {
                return value.toBoolean()
            }

            override fun save(item: Boolean?): String? {
                return item?.toString() ?: create().toString()
            }

            override fun create(): Boolean {
                return true
            }
        })
