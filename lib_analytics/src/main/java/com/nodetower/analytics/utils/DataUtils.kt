package com.nodetower.analytics.utils

import com.nodetower.base.utils.LogUtils
import org.json.JSONObject
import java.util.*

object DataUtils {


    fun mergeJSONObject(source: JSONObject?, dest: JSONObject) {
        source?.let {
            try {
                val superPropertiesIterator = it.keys()
                while (superPropertiesIterator.hasNext()) {
                    val key = superPropertiesIterator.next()
                    val value = it[key]
                    dest.put(key, value)
                }
            } catch (ex: Exception) {
                LogUtils.printStackTrace(ex)
            }
        }

    }
}