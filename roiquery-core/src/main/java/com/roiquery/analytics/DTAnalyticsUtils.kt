package com.roiquery.analytics

import com.roiquery.analytics.api.AnalyticsImp
import org.json.JSONObject

open class DTAnalyticsUtils {

    companion object {

        /**
         * 初始化事件的计时器，计时单位为毫秒。
         *
         * @param eventName 事件的名称
         */
        @JvmStatic
        fun trackTimerStart(eventName: String){
            AnalyticsImp.getInstance().trackTimerStart(eventName)
        }

        /**
         * 暂停事件计时器，计时单位为毫秒。
         *
         * @param eventName 事件的名称
         */
        @JvmStatic
        fun trackTimerPause(eventName: String){
            AnalyticsImp.getInstance().trackTimerPause(eventName)
        }

        /**
         * 恢复事件计时器，计时单位为毫秒。
         *
         * @param eventName 事件的名称
         */
        @JvmStatic
        fun trackTimerResume(eventName: String){
            AnalyticsImp.getInstance().trackTimerResume(eventName)
        }

        /**
         * 停止事件计时器
         *
         * @param eventName 事件的名称
         * @param properties 事件的属性
         */
        @JvmStatic
        @JvmOverloads
        fun trackTimerEnd(eventName: String, properties: JSONObject = JSONObject()){
            AnalyticsImp.getInstance().trackTimerEnd(eventName, properties)
        }


        /**
         * 停止事件计时器
         *
         * @param eventName 事件的名称
         * @param properties 事件的属性
         */
        @JvmStatic
        fun trackTimerEnd(eventName: String,  properties: Map<String, Any>){
            AnalyticsImp.getInstance().trackTimerEnd(eventName, JSONObject(properties.toMutableMap() ?: mutableMapOf<String, Any?>()))
        }


    }
}