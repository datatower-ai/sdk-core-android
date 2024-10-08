package ai.datatower.analytics

import ai.datatower.analytics.api.AnalyticsImp
import ai.datatower.analytics.core.PropertyManager
import ai.datatower.analytics.utils.DataCheck.findFirstNonJsonArray
import ai.datatower.analytics.utils.LogUtils
import ai.datatower.thirdparty.ThirdPartShareDataFactory
import org.json.JSONObject

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
         * 设置自有用户系统的id，
         * 传 null 或 空字符串 以退出登陆，清空用户 id
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
        fun setAdjustId(id: String?){
            AnalyticsImp.getInstance().setAdjustId(id)
        }

        /**
         * 设置 tenjin id
         * @param id tenjin id
         */
        @JvmStatic
        fun setTenjinId(id: String?){
            AnalyticsImp.getInstance().setTenjinId(id)
        }

        /**
         * 透传 dt_id 至三方归因平台
         * @param type 归因平台 DTThirdPartyShareType.ADJUST
         */
        @Deprecated("Please refer to our API Docs for substitutions.")
        @JvmStatic
        fun enableThirdPartySharing(type: Int) {
            try {
                ThirdPartShareDataFactory.createThirdInstance(type)
                    ?.synThirdDTIdData(PropertyManager.instance.getDTID())
            } catch (error: Exception) {
                LogUtils.d(Constant.LOG_TAG,"Third Share error: ${error.message}")
            }
        }

        /**
         * 设置通用属性（动态）
         *
         * @param properties 通用属性，值需为 Json 支持的类型
         */
        @JvmStatic
        fun setDynamicCommonProperties(propertiesGetter: () -> JSONObject) {
            AnalyticsImp.getInstance().setDynamicCommonProperties(propertiesGetter)
        }

        /**
         * 移除通用属性（动态）
         */
        @JvmStatic
        fun clearDynamicCommonProperties() {
            AnalyticsImp.getInstance().clearCommonProperties()
        }

        /**
         * 设置通用属性（静态，持久化）
         * 持久化数据的读取时机在 initSDK 之后
         *
         * @param properties 通用属性，值需为 Json 支持的类型
         */
        @JvmStatic
        fun setStaticCommonProperties(properties: Map<String, Any?>) {
            AnalyticsImp.getInstance().setStaticCommonProperties(JSONObject(properties))
        }


        /**
         * 设置通用属性（静态，持久化）
         * 持久化数据的读取时机在 initSDK 之后
         *
         * @param properties 通用属性，值需为 Json 支持的类型
         */
        @JvmStatic
        fun setStaticCommonProperties(properties: JSONObject) {
            AnalyticsImp.getInstance().setStaticCommonProperties(properties)
        }

        /**
         * 移除通用属性（静态，持久化）
         */
        @JvmStatic
        fun clearStaticCommonProperties() {
            AnalyticsImp.getInstance().clearStaticCommonProperties()
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