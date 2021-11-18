package com.roiquery.analytics

import android.annotation.SuppressLint
import android.content.Context
import com.roiquery.analytics.Constant.ENABLE_ANALYTICS_SDK_KEY
import com.roiquery.analytics.api.AbstractAnalytics
import com.roiquery.analytics.api.AnalyticsImp
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.utils.AppLifecycleHelper
import com.roiquery.analytics.utils.LogUtils
import com.roiquery.cloudconfig.ROIQueryCloudConfig

import org.json.JSONObject
import java.lang.Exception

open class ROIQueryAnalytics {

    companion object {

        @SuppressLint("StaticFieldLeak")
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
        fun track(
            eventName: String?,
            properties: Map<String, Any>? = mutableMapOf()
        ) =
            AnalyticsImp.getInstance(mContext).track(eventName, properties)

        /**
         * 调用 track 接口，追踪一个带有属性的事件
         *
         * @param eventName 事件的名称
         * @param properties 事件属性
         */
        @JvmStatic
        fun track(eventName: String?, properties: JSONObject?) =
            AnalyticsImp.getInstance(mContext).track(eventName, properties)


        /**
         * 采集 app 退出
         *
         * @param properties 事件属性，可为空
         */
        @JvmStatic
        @JvmOverloads
        fun trackAppClose(properties: Map<String, Any>? = mutableMapOf()) =
            AnalyticsImp.getInstance(mContext).trackAppClose(properties)


        /**
         * 采集 页面打开
         *
         * @param properties 事件属性，可为空
         */
        @JvmStatic
        @JvmOverloads
        fun trackPageOpen(properties: Map<String, Any>? = mutableMapOf()) =
            AnalyticsImp.getInstance(mContext).trackPageOpen(properties)

        /**
         * 采集 页面关闭
         *
         * @param properties 事件属性，可为空
         */
        @JvmStatic
        @JvmOverloads
        fun trackPageClose(properties: Map<String, Any>? = mutableMapOf()) =
            AnalyticsImp.getInstance(mContext).trackPageClose(properties)



        /**
         * 设置用户属性
         *
         * @param properties 事件属性
         */
        @JvmStatic
        @JvmOverloads
        fun setUserProperties(properties: Map<String, Any>? = mutableMapOf()) =
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
         * 设置Firebase的app_instance_id
         * @param id Firebase 的 app_instance_id
         */
        @JvmStatic
        fun setFirebaseAppInstanceId(id: String?) {
            AnalyticsImp.getInstance(mContext).fiid = id
        }

        /**
         * 设置AppsFlyer id
         * @param id appsflyer id
         */
        @JvmStatic
        fun setAppsFlyerId(id: String?) {
            AnalyticsImp.getInstance(mContext).afid = id
        }

        /**
         * 设置kochava id
         * @param id kochava id
         */
        @JvmStatic
        fun setKochavaId(id: String?) {
            AnalyticsImp.getInstance(mContext).koid = id
        }

        /**
         * 校准时间
         * @param timestamp 当前时间戳
         */
        @JvmStatic
        fun calibrateTime(timestamp: Long) {
            AnalyticsImp.getInstance(mContext).calibrateTime(timestamp)
        }


        /**
         * 获取当前时间，如果没有校准，则返回系统时间
         * @param timestamp 当前时间戳
         */
        @JvmStatic
        fun getRealTime() =
            AnalyticsImp.getInstance(mContext).getRealTime()



        /**
         * app 进入前台
         *
         */
        @JvmStatic
        fun onAppForeground() {
            try {
                if (!isSDKEnable()) return
                checkNotNull(mContext) { "Call ROIQuery.initSDK first" }
                EventDateAdapter.getInstance()?.isAppForeground = true
                for (listener in mAppLifecycleListeners) {
                    listener?.onAppForeground()
                }
                LogUtils.d("RoiqueryAnalytics", "onAppForeground")
            } catch (e: Exception) {
                LogUtils.printStackTrace("RoiqueryAnalytics", e)
            }
        }

        /**
         * app 进入后台
         */
        @JvmStatic
        fun onAppBackground() {
            try {
                if (!isSDKEnable()) return
                checkNotNull(mContext) { "Call ROIQuerySDK.init first" }
                EventDateAdapter.getInstance()?.isAppForeground = false
                for (listener in mAppLifecycleListeners) {
                    listener?.onAppBackground()
                }
                LogUtils.d("RoiqueryAnalytics", "onAppBackground")
            } catch (e: Exception) {
                LogUtils.printStackTrace("RoiqueryAnalytics", e)
            }

        }

        @JvmStatic
        fun getContext(): Context? {
            return try {
                if (!isSDKEnable()) return null
                checkNotNull(mContext) { "Call ROIQuerySDK.init first" }
                mContext as Context
            } catch (e: Exception) {
                LogUtils.printStackTrace("RoiqueryAnalytics", e)
                null
            }
        }

        @JvmStatic
        fun addAppStatusListener(listener: AppLifecycleHelper.OnAppStatusListener?) {
            if (!isSDKEnable()) return
            mAppLifecycleListeners.add(listener)
        }

        /**
         * sdk 是否可用，默认可用，由cloud config 控制
         */
        @JvmStatic
        internal fun isSDKEnable(): Boolean {
            return if (!AbstractAnalytics.mSDKConfigInit) true
            else
                ROIQueryCloudConfig.getBoolean(ENABLE_ANALYTICS_SDK_KEY, true).apply {
                    //switch
                    AnalyticsImp.getInstance(mContext).enableSDK = this
                }
        }

    }
}