package com.roiquery.analytics.utils

import org.json.JSONObject
import java.lang.NumberFormatException

/**
 * author: xiaosailing
 * date: 2021-10-25
 * description:变量转换工具类
 * version：1.0
 */

/**
 * 将json object转换为Map
 *
 * @param jsonObject
 * @return
 */
internal fun JSONObject?.transJSONObject2Map() = run {
    val map = HashMap<String, Any>()
    this?.let {
        val keys = this.keys()
        var key: String
        while (keys.hasNext()) {
            key = keys.next()
            map[key] = this.get(key)
        }
    }
    map
}

internal fun String?.transToLong()= run {
    var value = 0L
    this?.let {
        try {
            value = this.toLong()
        }catch (e:NumberFormatException){

        }
    }
    value
}

