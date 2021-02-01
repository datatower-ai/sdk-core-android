package com.nodetower.analytics.data.persistent

import android.content.SharedPreferences
import com.nodetower.analytics.data.DataParams
import com.nodetower.base.data.PersistentIdentity
import java.util.concurrent.Future


class PersistentLoginId(loadStoredPreferences: Future<SharedPreferences>) :
    PersistentIdentity<String?>(
        loadStoredPreferences,
        DataParams.TABLE_LOGIN_ID,
        object : PersistentSerializer<String?> {
            override fun load(value: String?): String {
                return value!!
            }

            override fun save(item: String?): String? {
                return item
            }

            override fun create(): String? {
                return ""
            }
        })
