package com.nodetower.analytics.utils

import android.text.TextUtils
import com.nodetower.base.utils.LogUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.regex.Pattern

object DataHelper {
    private val TAG = "Analytics_DataHelper"
    private val KEY_PATTERN = Pattern.compile(
        "^((?!^distinct_id$|^original_id$|^time$|^properties$|^id$|^first_id$|^second_id$|^users$|^events$|^event$|^user_id$|^date$|^datetime$)[a-zA-Z_$][a-zA-Z\\d_$]{0,99})$",
        Pattern.CASE_INSENSITIVE
    )

    @Throws(InvalidDataException::class)
    fun assertPropertyTypes(properties: JSONObject?) {
        if (properties == null) {
            return
        }
        val iterator = properties.keys()
        while (iterator.hasNext()) {
            val key = iterator.next()

            // Check Keys
            assertKey(key)
            try {
                val value = properties[key]
                if (value === JSONObject.NULL) {
                    iterator.remove()
                    continue
                }
                if (!(value is CharSequence || value is Number || value is JSONArray || value is Boolean || value is Date)) {
                    throw InvalidDataException(
                        "The property value must be an instance of "
                                + "CharSequence/Number/Boolean/JSONArray. [key='" + key + "', value='" + value.toString()
                                + "']"
                    )
                }
                if (("app_crashed_reason" == key)) {
                    if (value is String && value.length > 8191 * 2) {
                        properties.put(key, value.substring(0, 8191 * 2) + "$")
                        LogUtils.d(
                            TAG, ("The property value is too long. [key='" + key
                                    + "', value='" + value.toString() + "']")
                        )
                    }
                } else {
                    if (value is String && value.length > 8191) {
                        properties.put(key, value.substring(0, 8191) + "$")
                        LogUtils.d(
                            TAG, ("The property value is too long. [key='" + key
                                    + "', value='" + value.toString() + "']")
                        )
                    }
                }
            } catch (e: JSONException) {
                throw InvalidDataException("Unexpected property key. [key='$key']")
            }
        }
    }

    @Throws(InvalidDataException::class)
    fun assertKey(key: String?) {
        if (null == key || key.length < 1) {
            throw InvalidDataException("The key is empty.")
        }
        if (!(KEY_PATTERN.matcher(key).matches())) {
            throw InvalidDataException("The key '$key' is invalid.")
        }
    }

    @Throws(InvalidDataException::class)
    fun assertValue(value: String) {
        if (TextUtils.isEmpty(value)) {
            throw InvalidDataException("The value is empty.")
        }
        if (value.length > 255) {
            throw InvalidDataException("The $value is too long, max length is 255.")
        }
    }

    fun appendLibMethodAutoTrack(jsonObject: JSONObject?): JSONObject {
        var jsonObject = jsonObject
        if (jsonObject == null) {
            jsonObject = JSONObject()
        }
        try {
            jsonObject.put("\$lib_method", "autoTrack")
        } catch (e: JSONException) {
            LogUtils.printStackTrace(e)
        }
        return jsonObject
    }
}
