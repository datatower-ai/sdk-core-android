package com.roiquery.analytics.core

import android.content.Context
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.roiquery.analytics.Constant
import com.roiquery.analytics.OnDataTowerIdListener
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.utils.*
import com.roiquery.quality.ROIQueryErrorParams
import com.roiquery.quality.ROIQueryQualityHelper
import org.json.JSONObject
import java.util.concurrent.ConcurrentLinkedQueue


class PropertyManager private constructor() {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            PropertyManager()
        }
    }

    // 事件信息，包含事件的基本数据
    private var eventInfo: MutableMap<String, Any?> = mutableMapOf()

    // 事件通用属性
    private var commonProperties: MutableMap<String, Any?> = mutableMapOf()
    //激活时用户属性
    private var activeProperties: MutableMap<String, Any?> = mutableMapOf()

    //本地数据适配器，包括sp、db的操作
    private var dataAdapter: EventDateAdapter? = null

    private var resumeFromBackground = false

    //检索用户是否启用了限制广告跟踪
    private var limitAdTrackingEnabled = false

    private var sessionStartTime = 0L

    private val dtidCallbacks: ConcurrentLinkedQueue<OnDataTowerIdListener?> =
        ConcurrentLinkedQueue()


    fun init(
        context: Context,
        initConfig: AnalyticsConfig?,
    ) {
        try {
            dataAdapter = EventDateAdapter.getInstance(context)
            initEventInfo(context)
            initCommonProperties(context, initConfig)
            getDataTowerId(context)
            registerFPSListener()
            registerNetworkStatusChangedListener(context)
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
    }

    private fun getDataTowerId(context: Context) {
        //这里每次更新，因为 gaid 或者Android id 有可能会变
        EventTrackManager.instance.addTask {
            var justUpdateOriginalId = false
            dataAdapter?.dtId?.let {
                if (it.isNotEmpty()) {
                    updateDTID(it)
                    onDataTowerIdCallback(it)
                    justUpdateOriginalId = true
                }
            }
            initDTIdOrUpdateOriginalId(context, justUpdateOriginalId)
        }
    }

    fun getDataTowerId(callBack: OnDataTowerIdListener) {
        if (getDTID().isNotEmpty()) {
            callBack.onDataTowerIdCompleted(getDTID())
        } else {
            dtidCallbacks.add(callBack)
        }
    }

    private fun onDataTowerIdCallback(id: String) {
        dtidCallbacks.forEach { callback ->
            callback?.onDataTowerIdCompleted(id)
        }
        dtidCallbacks.clear()
    }

    private fun initDTIdOrUpdateOriginalId(context: Context, justUpdateOriginalId: Boolean) {
        var gaid: String
        try {
            val info = AdvertisingIdClient.getAdvertisingIdInfo(context)
            gaid = info.id ?: ""
            limitAdTrackingEnabled = info.isLimitAdTrackingEnabled
        } catch (e: Exception) {
            //googleService not available
            gaid = ""
        }
        try {
            var originalId = ""
            //gaid 不可用，则使用 Android id
            if (gaid.isEmpty() || limitAdTrackingEnabled || gaid == "00000000-0000-0000-0000-000000000000") {
                originalId = DeviceUtils.getAndroidID(context)
                updateAndroidId(originalId)
            } else {
                originalId = gaid
                updateGAID(originalId)
            }
            //生成dtid
            if (!justUpdateOriginalId) {
                val dtid = initDTId(originalId)
                onDataTowerIdCallback(dtid)
            }
        } catch (e: Exception) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.CODE_GET_ORIGINAL_ID_EXCEPTION,
                e.message,
                ROIQueryErrorParams.INIT_EXCEPTION
            )
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
            val appId = AnalyticsConfig.instance.mAppId
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

    private fun initDTIdWithAndroidId(context: Context, justUpdateAndroidId: Boolean) {
        val originalId = DeviceUtils.getAndroidID(context)
        updateAndroidId(originalId)
        if (!justUpdateAndroidId) {
            val dtid = initDTId(originalId)
            onDataTowerIdCallback(dtid)
        }
    }

    /**
     * 获取并配置 事件一些基本属性
     *
     * @return
     */
    private fun initEventInfo(context: Context) {
        try {
            EventTrackManager.instance.addTask {
                EventUtils.getEventInfo(context, dataAdapter, eventInfo)
                Log.d(Constant.LOG_TAG, "initEventInfo"+SystemClock.elapsedRealtime().toString())
            }
        }catch (e:Exception){
        }
    }

    private fun updateEventInfo(key: String, value: String) {
        eventInfo[key] = value
    }

    fun getEventInfo() = eventInfo.toMutableMap()

    /**
     * 获取并配置 事件通用属性
     *
     * @return
     */
    private fun initCommonProperties(context: Context, initConfig: AnalyticsConfig?) {
        try {
            EventTrackManager.instance.addTask {
                EventUtils.getCommonProperties(context, commonProperties, activeProperties)
                Log.d(Constant.LOG_TAG, "initCommonProperties"+SystemClock.elapsedRealtime().toString())
                //增加外部出入的属性
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

        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
    }


    private fun updateCommonProperties(key: String, value: Any?) {
        commonProperties[key] = value
    }

    private fun removeCommonProperty(key: String) {
        if (commonProperties.containsKey(key)) {
            commonProperties.remove(key)
        }
    }

    fun getCommonProperties() = commonProperties.toMutableMap()

    fun getActiveProperties() = activeProperties.toMutableMap()

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
        EventTrackManager.instance.addTask {
            updateCommonProperties(
                Constant.COMMON_PROPERTY_IS_FOREGROUND,
                isForeground
            )
            val isFirstOpen = dataAdapter?.isFirstSessionStartInserted?.not()

            if (isForeground) {
                sessionStartTime = SystemClock.elapsedRealtime()

                updateCommonProperties(
                    Constant.COMMON_PROPERTY_EVENT_SESSION,
                    DataUtils.getSession()
                )

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
        EventTrackManager.instance.addTask{
            if (dataAdapter?.dtId?.isEmpty() == true) {
                dataAdapter?.dtId = id
            }
            updateEventInfo(
                Constant.EVENT_INFO_DT_ID, id
            )
        }
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
        EventTrackManager.instance.addTask{
            EventDateAdapter.getInstance()?.accountId = acid
            updateEventInfo(Constant.EVENT_INFO_ACID, acid)
        }
    }

    fun getACID(): String {
        (getEventInfo()[Constant.EVENT_INFO_ACID] as String?)?.let {
            if (it.isNotEmpty()) {
                return it
            }
        }
        return ""
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

    fun updateAdjustId(adjustId: String?) {
        if (adjustId?.isEmpty() == true) return
        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET,
            JSONObject().apply {
                put(Constant.USER_PROPERTY_LATEST_ADJUST_ID, adjustId)
            }
        )
    }


}