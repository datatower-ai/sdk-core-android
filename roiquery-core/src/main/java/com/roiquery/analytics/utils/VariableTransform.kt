package com.roiquery.analytics.utils

import org.json.JSONObject

/**
 * author: xiaosailing
 * date: 2021-10-25
 * description:变量转换工具类
 * version：1.0
 */
 object VariableTransform {


    /**
     * 将json object转换为Map
     *
     * @param jsonObject
     * @return
     */
    internal fun transJSONObject2Map(jsonObject: JSONObject?):Map<String,Any>{
        var map = HashMap<String,Any>()
        val keys = jsonObject?.keys()
        var key:String
        while (keys?.hasNext() == true){
            key=keys.next()
            map[key]=jsonObject.get(key)
        }
        return map
    }
}