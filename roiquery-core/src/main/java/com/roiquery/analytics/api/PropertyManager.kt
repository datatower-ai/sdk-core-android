package com.roiquery.analytics.api

import android.content.Context
import android.os.SystemClock
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.roiquery.analytics.Constant
import com.roiquery.analytics.ROIQueryCoroutineScope
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.core.EventTrackManager
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class PropertyManager private constructor() : ROIQueryCoroutineScope() {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            PropertyManager()
        }
    }

    // 事件信息，包含事件的基本数据
    private var eventInfo: MutableMap<String, Any?>? = null

    // 事件通用属性
    private var commonProperties: MutableMap<String, Any?>? = null

    //本地数据适配器，包括sp、db的操作
    private var dataAdapter: EventDateAdapter? = null

    private var resumeFromBackground = false

    //检索用户是否启用了限制广告跟踪
    private var limitAdTrackingEnabled = false

    private var sessionStartTime = 0L

    private var gaid = ""

    fun init(context: Context, initConfig: AnalyticsConfig?, dataTowerIdHandler: (id: String) -> Unit) {
        dataAdapter = EventDateAdapter.getInstance(context)
        initEventInfo(context)
        initCommonProperties(context, initConfig)
        getDataTowerId(context, dataTowerIdHandler)
    }

    private fun getDataTowerId(context: Context, dataTowerIdHandler: (id: String) -> Unit){
        scope.launch {
            getGAID(context)
            dataTowerIdHandler.invoke(initDTId(context))
        }
    }

    private fun initDTId(context: Context):String {
        val appId = AbstractAnalytics.mConfigOptions?.mAppId

        val dtIdOriginal = (if (gaid.isEmpty() || limitAdTrackingEnabled){
            val androidId = DeviceUtils.getAndroidID(context)
            updateAndroidId(androidId)
            androidId
        }  else gaid).plus("+$appId")

        val dtId = DataEncryption.instance.str2Sha1Str(dtIdOriginal)
        updateDTID(dtId)
        return dtId
    }

    /**
     * 获取并配置 事件一些基本属性
     *
     * @return
     */
    private fun initEventInfo(context: Context) {
        eventInfo = EventUtils.getEventInfo(context, dataAdapter)
    }

    private fun updateEventInfo(key: String, value: String) {
        eventInfo?.put(key, value)
    }

    fun getEventInfo() = eventInfo?.toMutableMap() ?: mutableMapOf()

    /**
     * 获取并配置 事件通用属性
     *
     * @return
     */
    private fun initCommonProperties(context: Context,initConfig: AnalyticsConfig?) {
        commonProperties = EventUtils.getCommonProperties(context, dataAdapter)
        initConfig?.let { config ->
            if (config.mCommonProperties != null) {
                val iterator = config.mCommonProperties!!.keys()
                while (iterator.hasNext()) {
                    val key = iterator.next()
                    try {
                        val value = config.mCommonProperties!![key]
                        updateCommonProperties(key, value.toString())
                    } catch (e: Exception) {
                        LogUtils.printStackTrace(e)
                    }
                }
            }
        }
    }


    private fun updateCommonProperties(key: String, value: Any?) {
        commonProperties?.put(key, value)
    }

    private fun removeCommonProperty(key: String){
        if(commonProperties?.containsKey(key) == true){
            commonProperties?.remove(key)
        }
    }

    fun getCommonProperties() = commonProperties?.toMutableMap() ?: mutableMapOf()


    /**
     * gaid 获取，异步
     */
    private suspend fun getGAID(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val info = AdvertisingIdClient.getAdvertisingIdInfo(context)
                val id = info.id ?: ""
                limitAdTrackingEnabled = info.isLimitAdTrackingEnabled

                if(id.isNotEmpty() && !limitAdTrackingEnabled ){
                    updateGAID(id)
                }
            } catch (exception: Exception) {
                LogUtils.d("getGAID", "onException:" + exception.message.toString())
            }
        }
    }


    fun updateSdkVersionProperty(jsonObject: JSONObject, typeKye: String, versionKey: String) {
        //接入 SDK 的类型可能是 Android 或 Unity ，因此这里需动态获取
        getCommonProperties()[Constant.COMMON_PROPERTY_SDK_TYPE]?.toString()?.let {
            if (it.isNotEmpty()) {
                jsonObject.put(
                    Constant.USER_PROPERTY_ACTIVE_SDK_TYPE,
                    it
                )
            }
        }
        //SDK 版本
        getCommonProperties()[Constant.COMMON_PROPERTY_SDK_VERSION]?.toString()?.let {
            if (it.isNotEmpty()) {
                jsonObject.put(
                    Constant.USER_PROPERTY_ACTIVE_SDK_VERSION,
                    it
                )
            }
        }
    }


    fun updateIsForeground(isForeground: Boolean) {
        updateCommonProperties(
            Constant.COMMON_PROPERTY_IS_FOREGROUND,
            isForeground
        )
        val isFirstOpen = dataAdapter?.isFirstOpen
        if (isFirstOpen == true) {
            dataAdapter?.isFirstOpen = false
        }
        if (isForeground) {
            sessionStartTime = SystemClock.elapsedRealtime()

            updateCommonProperties(Constant.COMMON_PROPERTY_EVENT_SESSION, DataUtils.getSession())

            EventTrackManager.instance.trackNormalPreset(Constant.PRESET_EVENT_SESSION_START,JSONObject().apply {
                put(Constant.SESSION_START_PROPERTY_IS_FIRST_TIME, isFirstOpen)
                put(Constant.SESSION_START_PROPERTY_RESUME_FROM_BACKGROUND, resumeFromBackground)
                put(Constant.SESSION_START_PROPERTY_START_REASON, "")
            })
        }else {
            resumeFromBackground = true
            removeCommonProperty(Constant.COMMON_PROPERTY_EVENT_SESSION)
            EventTrackManager.instance.trackNormalPreset(Constant.PRESET_EVENT_SESSION_END, JSONObject().apply {
                if (sessionStartTime != 0L){
                    val sessionDuration : Int = ((SystemClock.elapsedRealtime() - sessionStartTime)/1000).toInt()
                    put(Constant.SESSION_END_PROPERTY_SESSION_DURATION, sessionDuration)
                    sessionStartTime = 0L
                }
            })
        }
    }

    private fun updateDTID(dtId: String){
        if (dtId.isEmpty()){
            return
        }
        dataAdapter?.dtId = dtId
        updateEventInfo(
            Constant.EVENT_INFO_DT_ID, dtId
        )
    }


    private fun updateGAID(id: String){
        if(id.isEmpty() || limitAdTrackingEnabled){
            return
        }
        updateEventInfo(Constant.EVENT_INFO_GAID, id)

        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET, JSONObject().apply {
                put(Constant.USER_PROPERTY_LATEST_GAID, id)
            })

        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET_ONCE, JSONObject().apply {
                put(Constant.USER_PROPERTY_ACTIVE_GAID, id)
            })
    }

    private fun updateAndroidId(id: String){
        updateEventInfo(Constant.EVENT_INFO_ANDROID_ID, id)

        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET_ONCE, JSONObject().apply {
                put(Constant.USER_PROPERTY_ACTIVE_ANDROID_ID, id)
            })
    }

    fun updateNetworkType(networkType: NetworkUtil.NetworkType?) {
        updateCommonProperties(
            Constant.COMMON_PROPERTY_NETWORK_TYPE,
            NetworkUtil.convertNetworkTypeToString(networkType)
        )
    }


    fun updateACID(acid: String) {
        updateEventInfo(Constant.EVENT_INFO_ACID, acid)
    }

    fun updateFireBaseInstanceId(fiid: String) {
        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET,
            JSONObject().apply {
                put(Constant.USER_PROPERTY_LATEST_FIREBASE_IID, fiid)
            }
        )
    }

    fun updateFCMToken(fcmToken: String) {
        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET,
            JSONObject().apply {
                put(Constant.USER_PROPERTY_LATEST_FCM_TOKEN, fcmToken)
            }
        )
    }

    fun updateAFID(afid: String) {
        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET,
            JSONObject().apply {
                put(Constant.USER_PROPERTY_LATEST_APPSFLYER_ID, afid)
            }
        )
    }

    fun updateKOID(koid: String) {
        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET,
            JSONObject().apply {
                put(Constant.USER_PROPERTY_LATEST_KOCHAVA_ID, koid)
            }
        )
    }



}