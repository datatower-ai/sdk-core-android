package ai.datatower.analytics.core

import ai.datatower.analytics.Constant
import ai.datatower.analytics.OnDataTowerIdListener
import ai.datatower.analytics.config.AnalyticsConfig
import ai.datatower.analytics.data.EventDataAdapter
import ai.datatower.analytics.taskqueue.MainQueue
import ai.datatower.analytics.taskqueue.MonitorQueue
import ai.datatower.analytics.utils.DataEncryption
import ai.datatower.analytics.utils.DataUtils
import ai.datatower.analytics.utils.DeviceUtils
import ai.datatower.analytics.utils.EventUtils
import ai.datatower.analytics.utils.LogUtils
import ai.datatower.analytics.utils.MemoryUtils
import ai.datatower.analytics.utils.NetworkUtil
import ai.datatower.analytics.utils.CommonPropsUtil
import ai.datatower.analytics.utils.PresetEvent
import ai.datatower.analytics.utils.PresetPropManager
import ai.datatower.quality.PerfAction
import ai.datatower.quality.PerfLogger
import ai.datatower.quality.DTErrorParams
import ai.datatower.quality.DTQualityHelper
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.util.concurrent.ConcurrentLinkedQueue


class PropertyManager private constructor() {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            PropertyManager()
        }
    }

    //本地数据适配器，包括sp、db的操作
    private var dataAdapter: EventDataAdapter? = null

//    private var resumeFromBackground = false

    //检索用户是否启用了限制广告跟踪
    private var limitAdTrackingEnabled = false

    private var sessionStartTime = 0L
    private var sessionEndTime = 0L

    private val dtidCallbacks: ConcurrentLinkedQueue<OnDataTowerIdListener?> =
        ConcurrentLinkedQueue()


    suspend fun init(
        context: Context,
        initConfig: AnalyticsConfig?,
    ) {
        try {
            dataAdapter = EventDataAdapter.getInstance(context)
            initDisableList(context)
            EventUtils.initUa(context)
            CommonPropsUtil.init()
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
        PerfLogger.doPerfLog(PerfAction.GETDTIDBEGIN, System.currentTimeMillis())

        //这里每次更新，因为 gaid 或者Android id 有可能会变
        MainQueue.get().postTask {
            var justUpdateOriginalId = false
            runBlocking { dataAdapter?.getDtId()?.await() }?.let {
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
        PerfLogger.doPerfLog(PerfAction.GETDTIDBEGIN, System.currentTimeMillis())

        MainQueue.get().postTask {
            if (getDTID().isNotEmpty()) {
                Handler(Looper.getMainLooper()).post {
                    PerfLogger.doPerfLog(PerfAction.GETDTIDEND, System.currentTimeMillis())

                    callBack.onDataTowerIdCompleted(getDTID())
                }
            } else {
                dtidCallbacks.add(callBack)
            }
        }
    }

    private fun onDataTowerIdCallback(id: String) {
        PerfLogger.doPerfLog(PerfAction.GETDTIDEND, System.currentTimeMillis())

        Handler(Looper.getMainLooper()).post {
            dtidCallbacks.forEach { callback ->
                callback?.onDataTowerIdCompleted(id)
            }
            dtidCallbacks.clear()
        }
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
                MonitorQueue.get()?.findReasonForGAIDFail(context)
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
            DTQualityHelper.instance.reportQualityMessage(
                DTErrorParams.CODE_GET_ORIGINAL_ID_EXCEPTION,
                e.message,
                DTErrorParams.INIT_EXCEPTION
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
            DTQualityHelper.instance.reportQualityMessage(
                DTErrorParams.CODE_INIT_DTID_EXCEPTION,
                e.message,
                DTErrorParams.INIT_EXCEPTION
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
     * 初始预置属性过滤列表.
     */
    private fun initDisableList(context: Context) {
        PresetPropManager.get(context)
    }

    /**
     * 获取并配置 事件一些基本属性
     *
     * @return
     */
    private suspend fun initEventInfo(context: Context) {
        EventUtils.getEventInfo(context, dataAdapter)
    }

    private fun updateEventInfo(key: String, value: String) {
        PresetPropManager.get()?.let {
            it.meta[key] = value
        }
    }

    fun getEventInfo() = PresetPropManager.get()?.meta?.map ?: mapOf()

    /**
     * 获取并配置 事件通用属性
     *
     * @return
     */
    private fun initCommonProperties(context: Context, initConfig: AnalyticsConfig?) {
        EventUtils.getCommonProperties(context)
    }


    private fun updateCommonProperties(key: String, value: Any?) {
        PresetPropManager.get()?.common?.set(key, value)
    }

    private fun removeCommonProperty(key: String) {
        PresetPropManager.get()?.common?.set(key, null)
    }

    fun getCommonProperties() = PresetPropManager.get()?.common?.map ?: mapOf()

    fun getActiveProperties() = PresetPropManager.get()?.userActive?.map ?: mapOf()

    /**
     * FPS状态监控
     */
    private fun registerFPSListener() {
        if (null == Looper.myLooper()) {
            Looper.prepare()
        }
        MemoryUtils.listenFPS(0)
    }

    /**
     * 网络状态监控
     */
    private fun registerNetworkStatusChangedListener(context: Context) {
        NetworkUtil.registerNetworkStatusChangedListener(
            context.applicationContext as Application,
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


    internal fun updateSdkVersionProperty(map: PresetPropManager.PPMap,) {
        //接入 SDK 的类型可能是 Android 或 Unity ，因此这里需动态获取
        getCommonProperties()[Constant.COMMON_PROPERTY_SDK_TYPE]?.toString()?.let {
            if (it.isNotEmpty()) {
                map[Constant.USER_PROPERTY_ACTIVE_SDK_TYPE] = it
            }
        }
        //SDK 版本
        getCommonProperties()[Constant.COMMON_PROPERTY_SDK_VERSION]?.toString()?.let {
            if (it.isNotEmpty()) {
                map[Constant.USER_PROPERTY_ACTIVE_SDK_VERSION] = it
            }
        }
    }


    fun updateIsForeground(isForeground: Boolean, resumeFromBackground: Boolean, startReason: String? = "") {
        val happenTime = SystemClock.elapsedRealtime()
        MainQueue.get().postTask {
            updateCommonProperties(
                Constant.COMMON_PROPERTY_IS_FOREGROUND,
                isForeground
            )

            if (isForeground) {
                sessionStartTime = happenTime

                updateCommonProperties(
                    Constant.COMMON_PROPERTY_EVENT_SESSION,
                    DataUtils.getSession()
                )

                dataAdapter?.isFirstSessionStartInserted()?.onSameQueueThen {

                    MainQueue.get().postTask {
                        // preset event disabled
                        if (!PresetEvent.SessionStart.isOn()) return@postTask

                        val isFirstOpen = it.not()

                        EventTrackManager.instance.trackNormalPreset(
                            Constant.PRESET_EVENT_SESSION_START,
                            happenTime,
                            JSONObject().also {
                                PresetPropManager.get()?.run {
                                    checkNSet(it, Constant.SESSION_START_PROPERTY_IS_FIRST_TIME, isFirstOpen)
                                    checkNSet(it,
                                        Constant.SESSION_START_PROPERTY_RESUME_FROM_BACKGROUND,
                                        resumeFromBackground
                                    )
                                    if (startReason != "" && startReason != "{}") {
                                        checkNSet(it, Constant.SESSION_START_PROPERTY_START_REASON, startReason)
                                    }
                                    if (resumeFromBackground && sessionEndTime != 0L) {
                                        checkNSet(it,
                                            Constant.SESSION_START_PROPERTY_BACKGROUND_DURATION,
                                            sessionStartTime - sessionEndTime
                                        )
                                    }
                                } ?: run {
                                    it.put(Constant.SESSION_START_PROPERTY_IS_FIRST_TIME, isFirstOpen)
                                    it.put(
                                        Constant.SESSION_START_PROPERTY_RESUME_FROM_BACKGROUND,
                                        resumeFromBackground
                                    )
                                    if (startReason != "" && startReason != "{}") {
                                        it.put(Constant.SESSION_START_PROPERTY_START_REASON, startReason)
                                    }
                                    if (resumeFromBackground && sessionEndTime != 0L) {
                                        it.put(
                                            Constant.SESSION_START_PROPERTY_BACKGROUND_DURATION,
                                            sessionStartTime - sessionEndTime
                                        )
                                    }
                                }
                            },
                            insertHandler = { code: Int, _: String ->
                                if (code == 0 && isFirstOpen) {
                                    EventDataAdapter.getInstance()
                                        ?.setIsFirstSessionStartInserted(true)
                                }
                            }
                        )
                    }
                }
            } else {
                // preset event disabled
                if (!PresetEvent.SessionEnd.isOn()) return@postTask

                EventTrackManager.instance.trackNormalPreset(
                    Constant.PRESET_EVENT_SESSION_END,
                    happenTime,
                    JSONObject().also {
                        if (sessionStartTime != 0L) {
                            sessionEndTime = SystemClock.elapsedRealtime()
                            val sessionDuration = sessionEndTime - sessionStartTime
                            PresetPropManager.get()?.run {
                                checkNSet(it, Constant.SESSION_END_PROPERTY_SESSION_DURATION, sessionDuration)
                            } ?: it.put(Constant.SESSION_END_PROPERTY_SESSION_DURATION, sessionDuration)
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
        MainQueue.get().postTask {
            dataAdapter?.setDtIdIfNeeded(id)
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

    fun getSDKVersion(): String {
        (getCommonProperties()[Constant.COMMON_PROPERTY_SDK_VERSION] as String?)?.let {
            if (it.isNotEmpty()) {
                return it
            }
        }
        return ""
    }

    fun getSDKType(): String {
        (getCommonProperties()[Constant.COMMON_PROPERTY_SDK_TYPE] as String?)?.let {
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

        PresetPropManager.get()?.userActive?.set(Constant.USER_PROPERTY_ACTIVE_GAID, id)
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

        PresetPropManager.get()?.userActive?.set(Constant.USER_PROPERTY_ACTIVE_ANDROID_ID, id)
    }

    fun updateNetworkType(networkType: NetworkUtil.NetworkType?) {
        updateCommonProperties(
            Constant.COMMON_PROPERTY_NETWORK_TYPE,
            NetworkUtil.convertNetworkTypeToString(networkType)
        )
    }

    fun updateACID(acid: String) {
        val id = acid.ifBlank { "" }
        MainQueue.get().postTask {
            EventDataAdapter.getInstance()?.setAccountId(id)
            updateEventInfo(Constant.EVENT_INFO_ACID, id)
        }
    }

    fun getACID(): String {
        return (getEventInfo()[Constant.EVENT_INFO_ACID] as String?) ?: ""
    }

    fun updateFireBaseInstanceId(fiid: String?) {
        if (fiid?.isEmpty() == true) return

        val happenTime = SystemClock.elapsedRealtime()

        MainQueue.get().postTask {
            CommonPropsUtil.updateInternalCommonProperties(Constant.COMMON_PROPERTY_FIREBASE_INSTANCE_ID, fiid)
        }

        MainQueue.get().postTask {
            EventTrackManager.instance.trackUser(
                Constant.PRESET_EVENT_USER_SET,
                happenTime,
                JSONObject().apply {
                    put(Constant.USER_PROPERTY_LATEST_FIREBASE_IID, fiid ?: "")
                }
            )
        }
    }

    fun updateAFID(afid: String?) {
        if (afid?.isEmpty() == true) return

        val happenTime = SystemClock.elapsedRealtime()

        MainQueue.get().postTask {
            CommonPropsUtil.updateInternalCommonProperties(Constant.COMMON_PROPERTY_APPSFLYER_ID, afid)
        }

        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET,
            happenTime,
            JSONObject().apply {
                put(Constant.USER_PROPERTY_LATEST_APPSFLYER_ID, afid ?: "")
            }
        )
    }

    fun updateKOID(koid: String?) {
        if (koid?.isEmpty() == true) return

        val happenTime = SystemClock.elapsedRealtime()

        MainQueue.get().postTask {
            CommonPropsUtil.updateInternalCommonProperties(Constant.COMMON_PROPERTY_KOCHAVA_ID, koid)
        }

        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET,
            happenTime,
            JSONObject().apply {
                put(Constant.USER_PROPERTY_LATEST_KOCHAVA_ID, koid ?: "")
            }
        )
    }

    fun updateAdjustId(adjustId: String?) {
        if (adjustId?.isEmpty() == true) return

        val happenTime = SystemClock.elapsedRealtime()

        MainQueue.get().postTask {
            CommonPropsUtil.updateInternalCommonProperties(Constant.COMMON_PROPERTY_ADJUST_ID, adjustId)
        }

        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET,
            happenTime,
            JSONObject().apply {
                put(Constant.USER_PROPERTY_LATEST_ADJUST_ID, adjustId ?: "")
            }
        )
    }

    fun updateTenjinId(tenjinId: String?) {
        if (tenjinId?.isEmpty() == true) return

        val happenTime = SystemClock.elapsedRealtime()

        MainQueue.get().postTask {
            CommonPropsUtil.updateInternalCommonProperties(Constant.COMMON_PROPERTY_TENJIN_ID, tenjinId)
        }

        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET,
            happenTime,
            JSONObject().apply {
                put(Constant.USER_PROPERTY_LATEST_TENJIN_ID, tenjinId ?: "")
            }
        )
    }


}
