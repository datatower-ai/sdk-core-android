package com.roiquery.analytics

import android.content.Context
import com.roiquery.analytics.api.AnalyticsImp
import com.roiquery.analytics.core.PropertyManager
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.utils.AppLifecycleHelper
import com.roiquery.analytics.utils.LogUtils
import com.roiquery.thirdparty.ThirdPartShareDataFactory

import org.json.JSONObject
import java.lang.Exception

open class DTAnalytics {

    companion object {

        private var mAppLifecycleListeners = mutableListOf<AppLifecycleHelper.OnAppStatusListener?>()

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
            AnalyticsImp.getInstance().trackNormal(eventName, false,properties)


        /**
         * 调用 track 接口，追踪一个带有属性的事件
         *
         * @param eventName 事件的名称
         * @param properties 事件属性
         */
        @JvmStatic
        fun track(eventName: String?, properties: JSONObject?) =
            AnalyticsImp.getInstance().trackNormal(eventName, false,properties)

        /**
         * 设置一般的用户属性
         *
         * @param properties 属性
         */
        @JvmStatic
        fun userSet(properties: JSONObject?){
            AnalyticsImp.getInstance().userSet(properties)
        }

        /**
         * 设置只要设置一次的用户属性
         *
         * @param properties 属性
         */
        @JvmStatic
        fun userSetOnce(properties: JSONObject?){
            AnalyticsImp.getInstance().userSetOnce(properties)
        }

        /**
         * 设置可累加的用户属性
         *
         * @param properties 属性
         */
        @JvmStatic
        fun userAdd(properties: JSONObject?){
            AnalyticsImp.getInstance().userAdd(properties)
        }

        /**
         * 清空用户属性
         *
         * @param properties 属性
         */
        @JvmStatic
        fun userUnset(vararg properties: String?){
            AnalyticsImp.getInstance().userUnset(*properties)
        }

        /**
         * 删除用户
         */
        @JvmStatic
        fun userDelete(){
            AnalyticsImp.getInstance().userDelete()
        }

        /**
         * 对 JSONArray 类型的用户属性进
         *
         * @param properties 属性
         */
        @JvmStatic
        fun userAppend(properties: JSONObject?){
            AnalyticsImp.getInstance().userAppend(properties)
        }


        /**
         * 获取 DataTower instance id
         */
        @JvmStatic
        fun getDataTowerId(onDataTowerIDListener: OnDataTowerIdListener) {
            AnalyticsImp.getInstance().getDTId(onDataTowerIDListener)
        }

        /**
         * 设置自有用户系统的id
         * @param id 用户系统id
         */
        @JvmStatic
        fun setAccountId(id: String?) {
            AnalyticsImp.getInstance().accountId = id
        }

        /**
         * 设置 Firebase 的 app_instance_id
         * @param id Firebase 的 app_instance_id
         */
        @JvmStatic
        fun setFirebaseAppInstanceId(id: String?) {
            AnalyticsImp.getInstance().setFirebaseInstanceId(id)
        }


        /**
         * 设置 AppsFlyer id
         * @param id appsflyer id
         */
        @JvmStatic
        fun setAppsFlyerId(id: String?) {
            AnalyticsImp.getInstance().setAppsFlyersId(id)
        }

        /**
         * 设置 kochava id
         * @param id kochava id
         */
        @JvmStatic
        fun setKochavaId(id: String?) {
            AnalyticsImp.getInstance().setKochavaId(id)
        }

        /**
         * 设置 adjust id
         * @param id adjust id
         */
        @JvmStatic
        fun  setAdjustId(id:String?){
            AnalyticsImp.getInstance().setAdjustId(id)
        }

        /**
         * 透传 dt_id 至三方归因平台
         * @param type 归因平台 DTThirdPartyShareType.ADJUST
         */
        @JvmStatic
        fun enableThirdPartySharing(type: Int) {
            try {
                ThirdPartShareDataFactory.createThirdInstance(type)
                    ?.synThirdDTIdData(PropertyManager.instance.getDTID())
            } catch (error: Exception) {
                LogUtils.d(Constant.LOG_TAG,"Third Share error: ${error.message}")
            }
        }

        /******************** internal *******************/

        /**
         * 调用 track 接口，追踪一个带有属性的事件
         *
         * @param eventName 事件的名称
         * @param properties 事件属性
         */
        @JvmStatic
        @JvmOverloads
        internal fun trackInternal(
            eventName: String?,
            properties: Map<String, Any>? = mutableMapOf()
        ) =
            AnalyticsImp.getInstance().trackNormal(eventName, true, properties)


        /**
         * 调用 track 接口，追踪一个带有属性的事件
         *
         * @param eventName 事件的名称
         * @param properties 事件属性
         */
        @JvmStatic
        internal fun trackInternal(eventName: String?, properties: JSONObject?) =
            AnalyticsImp.getInstance().trackNormal(eventName, true, properties)


        /**
         * app 进入前台
         *
         */
        internal fun onAppForeground(startReason: String?) {
            try {
                PropertyManager.instance.updateIsForeground(true,startReason)
                LogUtils.d("trackAppStateChanged","onAppForeground")
                for (listener in mAppLifecycleListeners) {
                    listener?.onAppForeground()
                }
            } catch (e: Exception) {
                LogUtils.printStackTrace("RoiqueryAnalytics", e)
            }
        }

        /**
         * app 进入后台
         */
        internal fun onAppBackground() {
            try {
                PropertyManager.instance.updateIsForeground(false)
                LogUtils.d("trackAppStateChanged","onAppBackground")
                for (listener in mAppLifecycleListeners) {
                    listener?.onAppBackground()
                }
            } catch (e: Exception) {
                LogUtils.printStackTrace("RoiqueryAnalytics", e)
            }
        }


        internal fun getContext(): Context? {
            return try {
                AnalyticsConfig.instance.mContext
            } catch (e: Exception) {
                LogUtils.printStackTrace("RoiqueryAnalytics", e)
                null
            }
        }


        internal fun addAppStatusListener(listener: AppLifecycleHelper.OnAppStatusListener?) {
            if (!isSDKInitSuccess()) return
            mAppLifecycleListeners.add(listener)
        }

        /**
         * sdk 是否初始化成功
         */
        internal fun isSDKInitSuccess(): Boolean = AnalyticsImp.getInstance().isInitSuccess()

    }
}