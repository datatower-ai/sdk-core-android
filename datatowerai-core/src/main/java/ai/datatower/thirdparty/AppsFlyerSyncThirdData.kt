package ai.datatower.thirdparty

import ai.datatower.analytics.config.AnalyticsConfig
import ai.datatower.analytics.utils.LogUtils

class AppsFlyerSyncThirdData : SyncThirdPartDataImpl {
    override fun synThirdDTIdData(id: String) {
        if (AnalyticsConfig.instance.isSdkDisable()) {
            return
        }
        LogUtils.d( "开始同步Appsflyer数据")
        val maps: MutableMap<String, Any> = HashMap()
        maps[Constant.SHARE_DATA_DT_ID] = id
        try {
            val mAppsFlyerClazz = Class.forName("com.appsflyer.AppsFlyerLib")
            val getInstanceMethod = mAppsFlyerClazz.getMethod("getInstance")
            //拿到AppsFlyerLib实例
            val afObject = getInstanceMethod.invoke(null)
            val mSetAdditionalDataMethod = mAppsFlyerClazz.getMethod(
                "setAdditionalData",
                MutableMap::class.java
            )
            mSetAdditionalDataMethod.invoke(afObject, maps)
            LogUtils.d("AppsFlyer数据同步成功")
        } catch (e: NoSuchMethodException) {
            LogUtils.e("AppsFlyer数据同步异常:" + e.message)
            syncThirdPartyData5(maps)
        } catch (e: Exception) {
            LogUtils.e( "AppsFlyer数据同步异常:" + e.message)
        }
    }

    private fun syncThirdPartyData5(maps: MutableMap<String, Any>) {
        LogUtils.d("重新开始同步Appsflyer数据")
        try {
            val mAppsFlyerClazz = Class.forName("com.appsflyer.AppsFlyerLib")
            val getInstanceMethod = mAppsFlyerClazz.getMethod("getInstance")
            //拿到AppsFlyerLib实例
            val afObject = getInstanceMethod.invoke(null)
            val mSetAdditionalDataMethod = mAppsFlyerClazz.getMethod(
                "setAdditionalData",
                java.util.HashMap::class.java
            )
            mSetAdditionalDataMethod.invoke(afObject, maps)
            LogUtils.d("同步Appsflyer数据成功")
        } catch (e: java.lang.Exception) {
            LogUtils.e( "AppsFlyer数据同步异常:" + e.message)
        }
    }
}