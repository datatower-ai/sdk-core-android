package com.roiquery.analytics.core

import android.content.Context
import android.os.Looper
import android.os.SystemClock
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.roiquery.analytics.Constant
import com.roiquery.analytics.ROIQueryCoroutineScope
import com.roiquery.analytics.api.AbstractAnalytics
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.utils.*
import com.roiquery.quality.ROIQueryErrorParams
import com.roiquery.quality.ROIQueryQualityHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


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


    fun init(
        context: Context,
        initConfig: AnalyticsConfig?,
    ) {
        dataAdapter = EventDateAdapter.getInstance(context)
        initEventInfo(context)
        initCommonProperties(context, initConfig)
        registerFPSListener()
        registerNetworkStatusChangedListener(context)
    }

    internal inline fun getDataTowerId(context: Context, crossinline dataTowerIdHandler: (id: String) -> Unit) {
        scope.launch {
            //这里每次更新，因为 gaid 或者Android id 有可能会变
            val originalId = getOriginalId(context)
            dataAdapter?.dtId?.let {
                if (it.isNotEmpty()) {
                    updateDTID(it)
                    dataTowerIdHandler.invoke(it)
                    return@launch
                }
            }
            dataTowerIdHandler.invoke(initDTId(originalId))
        }
    }

    private suspend fun getOriginalId(context: Context) =
        suspendCoroutine<String> {
            scope.launch {
                try {
                    getGAIDFromClient(context)
                    val gaid = getGAID()
                    if (gaid.isEmpty() || limitAdTrackingEnabled) {
                        val androidId = DeviceUtils.getAndroidID(context)
                        updateAndroidId(androidId)
                        it.resume(androidId)
                    } else {
                        it.resume(gaid)
                    }
                } catch (e: Exception) {
                    ROIQueryQualityHelper.instance.reportQualityMessage(
                        ROIQueryErrorParams.CODE_GET_ORIGINAL_ID_EXCEPTION,
                        e.message,
                        ROIQueryErrorParams.INIT_EXCEPTION
                    )
                    it.resume("")
                }
            }
        }


    /**
     * 异常情况下，允许空值，dt_id 为空的数据不会上报，等待 dt_id 有值时再同步dt_id为空的数据
     */
    private fun initDTId(originalId: String): String {
        try {
            if (originalId.isEmpty()) {
                return ""
            }
            val appId = AbstractAnalytics.mConfigOptions?.mAppId
            val dtIdOriginal = originalId.plus("+$appId")
            val dtId = DataEncryption.instance.str2Sha1Str(dtIdOriginal)
            updateDTID(dtId)
            return dtId
        } catch (e: Exception) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.CODE_INIT_DTID_EXCEPTION,
                e.message,
                ROIQueryErrorParams.INIT_EXCEPTION
            )
            return ""
        }
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
    private fun initCommonProperties(context: Context, initConfig: AnalyticsConfig?) {
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

    private fun removeCommonProperty(key: String) {
        if (commonProperties?.containsKey(key) == true) {
            commonProperties?.remove(key)
        }
    }

    fun getCommonProperties() = commonProperties?.toMutableMap() ?: mutableMapOf()

    /**
     * FPS状态监控
     */
    private fun registerFPSListener() {
        if (null == Looper.myLooper()) {
            Looper.prepare()
        }
        MemoryUtils.listenFPS()
    }

    /**
     * 网络状态监控
     */
    private fun registerNetworkStatusChangedListener(context: Context) {
        NetworkUtil.registerNetworkStatusChangedListener(
            context,
            object : NetworkUtil.OnNetworkStatusChangedListener {
                override fun onDisconnected() {
                    updateNetworkType(NetworkUtil.NetworkType.NETWORK_NO)
                }

                override fun onConnected(networkType: NetworkUtil.NetworkType?) {
                    LogUtils.i("onNetConnChanged", networkType)
                    updateNetworkType(networkType)
                    EventUploadManager.getInstance()?.flush()
                }
            })
    }

    /**
     * gaid 获取，异步
     */
    private suspend fun getGAIDFromClient(context: Context) {
        withContext(Dispatchers.IO) {
            try {
                val info = AdvertisingIdClient.getAdvertisingIdInfo(context)
                val id = info.id ?: ""
                limitAdTrackingEnabled = info.isLimitAdTrackingEnabled
                updateGAID(id)
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


    fun updateIsForeground(isForeground: Boolean, startReason: String? = "") {
        updateCommonProperties(
            Constant.COMMON_PROPERTY_IS_FOREGROUND,
            isForeground
        )
        val isFirstOpen = dataAdapter?.isFirstSessionStartInserted?.not()

        if (isForeground) {
            sessionStartTime = SystemClock.elapsedRealtime()

            updateCommonProperties(Constant.COMMON_PROPERTY_EVENT_SESSION, DataUtils.getSession())

            EventTrackManager.instance.trackNormalPreset(
                Constant.PRESET_EVENT_SESSION_START,
                JSONObject().apply {
                    put(Constant.SESSION_START_PROPERTY_IS_FIRST_TIME, isFirstOpen)
                    put(
                        Constant.SESSION_START_PROPERTY_RESUME_FROM_BACKGROUND,
                        resumeFromBackground
                    )
                    startReason?.isNotEmpty().let {
                        put(Constant.SESSION_START_PROPERTY_START_REASON, startReason)
                    }
                },
                insertHandler = { code: Int, _: String ->
                    if (code == 0 && isFirstOpen == true) {
                        EventDateAdapter.getInstance()?.isFirstSessionStartInserted = true
                    }
                }
            )
        } else {
            resumeFromBackground = true
            EventTrackManager.instance.trackNormalPreset(
                Constant.PRESET_EVENT_SESSION_END,
                JSONObject().apply {
                    if (sessionStartTime != 0L) {
                        val sessionDuration = SystemClock.elapsedRealtime() - sessionStartTime
                        put(Constant.SESSION_END_PROPERTY_SESSION_DURATION, sessionDuration)
                        sessionStartTime = 0L
                    }
                },
                insertHandler = { code: Int, _: String ->
                    if (code == 0) {
                        removeCommonProperty(Constant.COMMON_PROPERTY_EVENT_SESSION)
                    }
                }
            )
        }
    }

    fun getDTID(): String {
        (getEventInfo()[Constant.EVENT_INFO_DT_ID] as String?)?.let {
            if (it.isNotEmpty()) {
                return it
            }
        }
        return ""
    }

    private fun updateDTID(id: String) {
        if (id.isEmpty()) {
            return
        }
        if (dataAdapter?.dtId?.isEmpty() == true) {
            dataAdapter?.dtId = id
        }
        updateEventInfo(
            Constant.EVENT_INFO_DT_ID, id
        )
    }

    fun getGAID(): String {
        (getEventInfo()[Constant.EVENT_INFO_GAID] as String?)?.let {
            if (it.isNotEmpty()) {
                return it
            }
        }
        return ""
    }

    private fun updateGAID(id: String) {
        if (id.isEmpty() || limitAdTrackingEnabled) {
            return
        }
        updateEventInfo(Constant.EVENT_INFO_GAID, id)

        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET_ONCE, JSONObject().apply {
                put(Constant.USER_PROPERTY_ACTIVE_GAID, id)
            })
    }

    fun getAndroidId(): String {
        (getEventInfo()[Constant.EVENT_INFO_ANDROID_ID] as String?)?.let {
            if (it.isNotEmpty()) {
                return it
            }
        }
        return ""
    }

    private fun updateAndroidId(id: String) {
        if (id.isEmpty()) return
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
        if (acid.isEmpty()) return
        EventDateAdapter.getInstance()?.accountId = acid
        updateEventInfo(Constant.EVENT_INFO_ACID, acid)
    }

    fun updateFireBaseInstanceId(fiid: String?) {
        if (fiid?.isEmpty() == true) return
        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET,
            JSONObject().apply {
                put(Constant.USER_PROPERTY_LATEST_FIREBASE_IID, fiid)
            }
        )
    }

    fun updateAFID(afid: String?) {
        if (afid?.isEmpty() == true) return
        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET,
            JSONObject().apply {
                put(Constant.USER_PROPERTY_LATEST_APPSFLYER_ID, afid)
            }
        )
    }

    fun updateKOID(koid: String?) {
        if (koid?.isEmpty() == true) return
        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET,
            JSONObject().apply {
                put(Constant.USER_PROPERTY_LATEST_KOCHAVA_ID, koid)
            }
        )
    }

    fun updateAdjustId(adjustId:String?){
        if (adjustId?.isEmpty() == true) return
        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET,
            JSONObject().apply {
                put(Constant.USER_PROPERTY_LATEST_ADJUST_ID, adjustId)
            }
        )
    }


}