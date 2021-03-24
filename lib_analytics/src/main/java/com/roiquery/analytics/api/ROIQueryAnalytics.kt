package com.roiquery.analytics.api

import android.content.Context
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.utils.AppLifecycleHelper
import com.roiquery.analytics.utils.LogUtils

import org.json.JSONObject

open class ROIQueryAnalytics {

    companion object {

        internal var mContext: Context? = null

        private var mAppLifecycleListeners =
            mutableListOf<AppLifecycleHelper.OnAppStatusListener?>()

        /**
         * 初始化
         *
         * @param context 上下文
         * @param configOptions 配置
         */
        @JvmStatic
        internal fun init(context: Context?, configOptions: AnalyticsConfig?) {
            AnalyticsImp.init(context, configOptions)
        }

        /**
         * 调用 track 接口，追踪一个带有属性的事件
         *
         * @param eventName 事件的名称
         * @param properties 事件属性
         */
        @JvmStatic
        @JvmOverloads
        fun track(eventName: String?, properties: JSONObject? = JSONObject()) =
            AnalyticsImp.getInstance(mContext).track(eventName, properties)


        /**
         * 调用 track 接口，追踪一个带有属性的事件
         *
         * @param eventName 事件的名称
         * @param properties 事件属性
         */
        @JvmStatic
        fun track(eventName: String?, properties: Map<String,Any>?) =
            AnalyticsImp.getInstance(mContext).track(eventName, properties)

        /**
         * 采集 app 退出
         *
         * @param properties 事件属性，可为空
         */
        @JvmStatic
        @JvmOverloads
        fun trackAppClose(properties: JSONObject? = JSONObject()) =
            AnalyticsImp.getInstance(mContext).trackAppClose(properties)

        /**
         * 采集 app 退出
         *
         * @param properties 事件属性，可为空
         */
        @JvmStatic
        fun trackAppClose(properties: Map<String,Any>?) =
            AnalyticsImp.getInstance(mContext).trackAppClose(properties)

        /**
         * 采集 页面打开
         *
         * @param properties 事件属性，可为空
         */
        @JvmStatic
        @JvmOverloads
        fun trackPageOpen(properties: JSONObject? = JSONObject()) =
            AnalyticsImp.getInstance(mContext).trackPageOpen(properties)

        /**
         * 采集 页面打开
         *
         * @param properties 事件属性，可为空
         */
        @JvmStatic
        fun trackPageOpen(properties: Map<String,Any>?) =
            AnalyticsImp.getInstance(mContext).trackPageOpen(properties)

        /**
         * 采集 页面关闭
         *
         * @param properties 事件属性，可为空
         */
        @JvmStatic
        fun trackPageClose(properties: Map<String,Any>?) =
            AnalyticsImp.getInstance(mContext).trackPageClose(properties)

        /**
         * 采集 页面关闭
         *
         * @param properties 事件属性，可为空
         */
        @JvmStatic
        @JvmOverloads
        fun trackPageClose(properties: JSONObject? = JSONObject()) =
            AnalyticsImp.getInstance(mContext).trackPageClose(properties)

        /**
         * 设置用户属性
         *
         * @param properties 事件属性
         */
        @JvmStatic
        fun setUserProperties(properties: Map<String,Any>?) =
            AnalyticsImp.getInstance(mContext).setUserProperties(properties)

        /**
         * 设置用户属性
         *
         * @param properties 事件属性
         */
        @JvmStatic
        fun setUserProperties(properties: JSONObject?) =
            AnalyticsImp.getInstance(mContext).setUserProperties(properties)

        /**
         * 主动上报本地数据事件
         *
         */
        @JvmStatic
        fun flush() = AnalyticsImp.getInstance(mContext).flush()

        /**
         * 设置自有用户系统的id
         * @param id 用户系统id
         */
        @JvmStatic
        fun setAccountId(id: String) {
            AnalyticsImp.getInstance(mContext).accountId = id
        }

        /**
         * app 进入前台
         *
         */
        @JvmStatic
        fun onAppForeground() {
            checkNotNull(mContext) { "Call ROIQuerySDK.init first" }
            EventDateAdapter.getInstance()?.isAppForeground = true
            for (listener in mAppLifecycleListeners) {
                listener?.onAppForeground()
            }
            LogUtils.d("RoiqueryAnalytics", "onAppForeground")
        }

        /**
         * app 进入后台
         */
        @JvmStatic
        fun onAppBackground() {
            checkNotNull(mContext) { "Call ROIQuerySDK.init first" }
            EventDateAdapter.getInstance()?.isAppForeground = false
            for (listener in mAppLifecycleListeners) {
                listener?.onAppBackground()
            }
            LogUtils.d("RoiqueryAnalytics", "onAppBackground")
        }

        @JvmStatic
        fun getContext():Context  {
            checkNotNull(mContext) { "Call ROIQuerySDK.init first" }
            return mContext as Context
        }

        @JvmStatic
        fun addAppStatusListener(listener: AppLifecycleHelper.OnAppStatusListener?) {
            mAppLifecycleListeners.add(listener)
        }
    }
}