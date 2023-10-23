package com.roiquery.analytics

import com.roiquery.analytics.api.AnalyticsImp
import com.roiquery.analytics.core.PropertyManager
import com.roiquery.analytics.utils.DataCheck.findFirstNonJsonArray
import com.roiquery.analytics.utils.LogUtils
import com.roiquery.thirdparty.ThirdPartShareDataFactory

import org.json.JSONObject
import java.lang.Exception
import java.lang.IllegalArgumentException

open class DTAnalytics {

    companion object {


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
         * 对 JSONArray 类型的用户属性进追加
         *
         * @param properties 属性
         */
        @JvmStatic
        fun userAppend(properties: JSONObject?){
            properties?.findFirstNonJsonArray()?.let {
                throw IllegalArgumentException("The argument 'properties' only accepts JSONArray as the value of key-value! Given key-value is invalid (\"$it\": ${properties.get(it)})")
            }

            AnalyticsImp.getInstance().userAppend(properties)
        }

        /**
         * 对 JSONArray 类型的用户属性进追加, 并去重
         *
         * @param properties 属性
         */
        @JvmStatic
        fun userUniqAppend(properties: JSONObject?){
            properties?.findFirstNonJsonArray()?.let {
                throw IllegalArgumentException("The argument 'properties' only accepts JSONArray as the value of key-value! Given key-value is invalid (\"$it\": ${properties.get(it)})")
            }

            AnalyticsImp.getInstance().userUniqAppend(properties)
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
         * sdk 是否初始化成功
         */
        internal fun isSDKInitSuccess(): Boolean = AnalyticsImp.getInstance().isInitSuccess()

    }
}