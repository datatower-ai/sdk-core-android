package com.nodetower.analytics.data.persistent

import android.content.SharedPreferences
import com.nodetower.base.data.PersistentIdentity
import java.util.concurrent.Future


class PersistentLoginId(loadStoredPreferences: Future<SharedPreferences>) :
    PersistentIdentity<String?>(
        loadStoredPreferences,
        PersistentLoader.PersistentName.LOGIN_ID,
        object : PersistentSerializer<String?> {
            override fun load(value: String?): String {
                return value!!
            }

            override fun save(item: String?): String? {
                return item
            }

            override fun create(): String? {
                return null
            }
        })
