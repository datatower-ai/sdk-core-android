package com.roiquery.analytics.api

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.ProcessLifecycleOwner
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.github.gzuliyujiang.oaid.DeviceID
import com.github.gzuliyujiang.oaid.IGetter
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.roiquery.analytics.Constant
import com.roiquery.analytics.Constant.COMMON_PROPERTY_USER_AGENT
import com.roiquery.analytics.Constant.EVENT_INFO_SYN
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.core.AnalyticsManager
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.network.HttpPOSTResourceRemoteRepository
import com.roiquery.analytics.utils.*
import com.roiquery.cloudconfig.ROIQueryCloudConfig
import com.roiquery.quality.ROIQueryErrorParams
import com.roiquery.quality.ROIQueryQualityHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.qiyi.basecore.taskmanager.TM
import org.qiyi.basecore.taskmanager.TickTask
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext


abstract class AbstractAnalytics(context: Context?) : IAnalytics , CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()

    private var mContext: Context? = null

    // 事件信息，包含事件的基本数据
    private var mEventInfo: MutableMap<String, Any?>? = null

    // 事件通用属性
    private var mCommonProperties: MutableMap<String, Any?>? = null

    //事件采集管理
    protected var mTrackTaskManager: ExecutorService? = null

    //采集 、上报管理
    protected var mAnalyticsManager: AnalyticsManager? = null

    //本地数据适配器，包括sp、db的操作
    private var mDataAdapter: EventDateAdapter? = null


    companion object {
        const val TAG = "AnalyticsApi"

        // 配置
        internal var mConfigOptions: AnalyticsConfig? = null

        // SDK 配置是否初始化
        var mSDKConfigInit = false

    }


    init {
        try {
            mContext = context
            initConfig(mContext!!.packageName)
            initLocalData()
            initTracker(mContext!!)
            initTime()
            initProperties()
            initCloudConfig()
            initAppLifecycleListener()
            getUserAgentByUIThread()
            trackPresetEvent()
            getGAID()
            getOAID()
            mSDKConfigInit = true
            LogUtils.d("ROIQuery","init succeed")
        } catch (e: Exception) {
            if (!ROIQueryAnalytics.isSDKInitSuccess()) {
                ROIQueryQualityHelper.instance.reportQualityMessage(
                    ROIQueryErrorParams.SDK_INIT_ERROR,
                    "SDK  init error "
                )
            }
            LogUtils.printStackTrace(e)
        }
    }

    /**
     * Init time
     * 初始化网络时间，保存至内存中
     */
    private fun initTime() {
        TimeCalibration.instance.getReferenceTime()
    }

    /**
     * 初始化本地数据
     */
    private fun initLocalData() {
        mDataAdapter = EventDateAdapter.getInstance(mContext!!, mContext!!.packageName)
        mDataAdapter?.timeOffset = Constant.TIME_OFFSET_DEFAULT_VALUE
        mDataAdapter?.lastEngagementTime = Constant.TIME_OFFSET_DEFAULT_VALUE
        mDataAdapter?.enableUpload = true
    }

    /**
     * 初始化预置、通用属性
     */
    private fun initProperties() {
        initEventInfo()
        initCommonProperties()
    }

    /**
     * 监听应用生命周期
     */
    private fun initAppLifecycleListener() {
        TM.postUI {
            ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleObserverImpl())
        }
    }

    /**
     * 初始化数据采集
     */
    private fun initTracker(context: Context) {
        mTrackTaskManager = Executors.newSingleThreadExecutor()
        mAnalyticsManager = AnalyticsManager.getInstance(context)
    }

    protected fun trackEvent(
        eventName: String?,
        eventType: String,
        isPreset: Boolean,
        properties: JSONObject? = null
    ) {
        try {
            if (eventName.isNullOrEmpty()) return
            val realEventName = assertEvent(eventName, properties)
            var isTimeVerify: Boolean

            launch(Dispatchers.Default) {
                //设置事件的基本信息
                val eventInfo = JSONObject(mEventInfo).apply {
                    TimeCalibration.instance.getVerifyTimeAsync().apply {
                        isTimeVerify = this!=TimeCalibration.TIME_NOT_VERIFY_VALUE
                        // 如果时间已校准，则 保存当前时间，否则保存当前时间的系统休眠时间差用做上报时时间校准依据
                        put(Constant.EVENT_INFO_TIME, if (isTimeVerify) this else TimeCalibration.instance.getSystemHibernateTimeGap())
                        put(Constant.EVENT_INFO_NAME, realEventName)
                        put(Constant.EVENT_INFO_TYPE, eventType)
                        put(EVENT_INFO_SYN, DataUtils.getUUID())
                    }
                }

                //事件属性, 常规事件与用户属性类型区分
                val eventProperties = if (eventType == Constant.EVENT_TYPE_TRACK) {
                    JSONObject(mCommonProperties).apply {

                        //应用是否在前台, 需要动态添加
                        put(
                            Constant.COMMON_PROPERTY_IS_FOREGROUND,
                            mDataAdapter?.isAppForeground
                        )
                        //合并用户自定义属性和通用属性
                        DataUtils.mergeJSONObject(properties, this, null)
                    }
                } else {
                    properties
                }

                //设置事件属性
                eventInfo.put(Constant.EVENT_INFO_PROPERTIES, eventProperties)

                //将事件时间是否校准的结果保存至事件信息中，以供上报时校准时间使用
                val data = JSONObject().apply {
                    put(Constant.EVENT_BODY, eventInfo)
                    put(Constant.EVENT_TIME_CALIBRATED, isTimeVerify)
                }

                mAnalyticsManager?.enqueueEventMessage(
                    realEventName, data, eventInfo.optString(
                        EVENT_INFO_SYN
                    )
                )


            }
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
            trackQualityEvent("trackEvent&&$eventName&& ${e.message}")
        }
    }

    private fun trackQualityEvent(qualityInfo: String) {
        ROIQueryQualityHelper.instance.reportQualityMessage(
            ROIQueryErrorParams.TRACK_PROPERTIES_KEY_NULL,
            qualityInfo
        )
    }

    /**
     * 获取并配置 事件一些基本属性
     *
     * @return
     */
    protected open fun initEventInfo() {
        mEventInfo = EventUtils.getEventInfo(mContext!!, mDataAdapter)
    }

    fun updateEventInfo(key: String, value: String) {
        mEventInfo?.put(key, value)
    }

    fun getEventInfo() = mEventInfo

    /**
     * 获取并配置 事件通用属性
     *
     * @return
     */
    protected open fun initCommonProperties() {
        if (ProcessUtils.isMainProcess(mContext as Application?)) {
            mDataAdapter?.eventSession = DataUtils.getSession()
        }
        mCommonProperties = EventUtils.getCommonProperties(mContext!!, mDataAdapter)
        mConfigOptions.let { config ->
            if (config?.mCommonProperties != null) {
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


    fun updateCommonProperties(key: String, value: String) {
        mCommonProperties?.put(key, value)
    }


    fun getCommonProperties() = mCommonProperties

    /**
     * 事件校验
     */
    private fun assertEvent(
        eventName: String,
        properties: JSONObject? = null
    ): String {
        //检验事件名
        var realEventName = eventName
        if (eventName.startsWith(Constant.PRESET_EVENT_TAG)) {//预置事件
            realEventName = eventName.replace(Constant.PRESET_EVENT_TAG, "")
        } else if (!EventUtils.isValidEventName(eventName)) {
            realEventName = ""
        }
        //校验属性名、属性值
        if (!EventUtils.isValidProperty(properties)) {
            realEventName = ""
        }
        return realEventName
    }

    /**
     * 初始化配置
     */
    protected open fun initConfig(packageName: String) {
        var configBundle: Bundle? = null
        try {
            mContext?.let {
                val appInfo = it.applicationContext.packageManager
                    .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                configBundle = appInfo.metaData
            }

        } catch (e: PackageManager.NameNotFoundException) {
            LogUtils.printStackTrace(e)
        }
        if (null == configBundle) {
            configBundle = Bundle()
        }

        mConfigOptions?.let { configOptions ->
            configLog(configOptions.mEnabledDebug, configOptions.mLogLevel)
            registerNetworkStatusChangedListener()
            if (configOptions.mFlushInterval == 0) {
                configOptions.setFlushInterval(
                    2000
                )
            }
            if (configOptions.mFlushBulkSize == 0) {
                configOptions.setFlushBulkSize(
                    100
                )
            }
            if (configOptions.mMaxCacheSize == 0L) {
                configOptions.setMaxCacheSize(
                    32 * 1024 * 1024L
                )
            }
        }

    }

    /**
     * 初始化云控配置
     */
    private fun initCloudConfig() {
        ROIQueryCloudConfig.init(
            mContext!!,
            HttpPOSTResourceRemoteRepository.create(
                Constant.CLOUD_CONFIG_URL//拉取配置地址
                //拉取参数
            ) {
                mutableMapOf<String, String>().apply {
                    put("app_id", mConfigOptions?.mAppId ?: "")
                    put("did", DeviceUtils.getAndroidID(mContext!!) ?: "")
                }
            },
            mDataAdapter?.cloudConfigAesKey ?: "",
            {
                mDataAdapter?.cloudConfigAesKey = it
            }
        ) {
//            LogUtils.d("CloudConfig", it)
        }
    }

    /**
     * 初始化log
     * @param enable 是否开启
     * @param logLevel log 级别
     */
    fun configLog(
        enable: Boolean = mConfigOptions?.mEnabledDebug ?: false,
        logLevel: Int = mConfigOptions?.mLogLevel ?: LogUtils.V
    ) {
        LogUtils.getConfig().apply {
            isLogSwitch = enable
            globalTag = Constant.LOG_TAG
            setConsoleSwitch(enable)
            setConsoleFilter(logLevel)
        }
    }

    private fun registerNetworkStatusChangedListener() {
        NetworkUtil.registerNetworkStatusChangedListener(
            mContext,
            object : NetworkUtil.OnNetworkStatusChangedListener {
                override fun onDisconnected() {}
                override fun onConnected(networkType: NetworkUtil.NetworkType?) {
                    LogUtils.i("onNetConnChanged", networkType)
                    mAnalyticsManager?.flush()
                }
            })
    }


    /**
     * 获取当前时间，如果没有校准，则返回系统时间
     */
    @Synchronized
    open fun getRealTime(): Long {
        return try {
            if (isTimeCalibrated()) {
                System.currentTimeMillis() + (mDataAdapter?.timeOffset?.toLong() ?: 0L)
            } else {
                System.currentTimeMillis()
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    open fun isTimeCalibrated() = Constant.TIME_OFFSET_DEFAULT_VALUE != mDataAdapter?.timeOffset

    /**
     * 校准时间.
     * @param timestamp 当前时间戳
     */
    open fun calibrateTime(timestamp: Long) {
        if (timestamp != 0L) {
            mDataAdapter?.timeOffset = (timestamp - System.currentTimeMillis()).toString()
        }
    }


    /**
     * oaid 获取，异步
     */
    private fun getOAID() {
        try {
            if (!DeviceID.supportedOAID(mContext)) {
                // 不支持OAID，须自行生成GUID，然后存到`SharedPreferences`及`ExternalStorage`甚至`CloudStorage`。
                return
            }
            DeviceID.getOAID(mContext, object : IGetter {
                override fun onOAIDGetComplete(oaid: String) {
                    // 不同厂商的OAID格式是不一样的，可进行MD5、SHA1之类的哈希运算统一
                    mDataAdapter?.oaid = oaid
                    trackUser(Constant.PRESET_EVENT_USER_SET,JSONObject().apply {
                        put(Constant.USER_PROPERTY_LATEST_OAID,oaid)
                    })
                    updateEventInfo(Constant.EVENT_INFO_OAID, oaid)
                }

                override fun onOAIDGetError(exception: java.lang.Exception) {
                    // 获取OAID失败
                    LogUtils.d(exception)
                }
            })
        } catch (e: Exception) {
            LogUtils.d(e)
        }
    }

    /**
     * gaid 获取，异步
     */
    private fun getGAID() {
        ThreadUtils.getSinglePool().submit {
            try {
                LogUtils.d("getGAID", "start")
                val info = AdvertisingIdClient.getAdvertisingIdInfo(mContext!!)
                val id = info.id ?: ""
                mDataAdapter?.gaid = id
                trackUser(Constant.PRESET_EVENT_USER_SET,JSONObject().apply {
                    put(Constant.USER_PROPERTY_LATEST_GAID, id)
                })
                updateEventInfo(Constant.EVENT_INFO_GAID, id)
                LogUtils.d("getGAID", "onSuccess：$id")
            } catch (exception: Exception) {
                LogUtils.d("getGAID", "onException:" + exception.message.toString())
            }

        }
    }

    /**
     * 采集app 预置事件
     */
    private fun trackPresetEvent() {
        //子进程不采集
        if (!ProcessUtils.isMainProcess(mContext as Application?)) {
            LogUtils.i(
                "trackPresetEvent",
                ProcessUtils.getCurrentProcessName(mContext as Application) + "is not main process"
            )
            return
        }
        if (mDataAdapter?.isFirstOpen == true) {
            trackAppOpenEvent(true)
            mDataAdapter?.isFirstOpen = false
        } else {
            trackAppOpenEvent(false)
        }
        trackAppEngagementEvent()
        setLatestUserProperties()
        setActiveUserProperties()

//        setSystemUserProperties()
    }


    /**
     * 采集app 启动事件
     */
    private fun trackAppOpenEvent(isFirstOpen: Boolean) {
        trackNormal(if (isFirstOpen) Constant.PRESET_EVENT_APP_FIRST_OPEN else Constant.PRESET_EVENT_APP_OPEN)
    }


    private fun setLatestUserProperties() {
        trackUser(
            Constant.PRESET_EVENT_USER_SET,
            JSONObject(EventUtils.getLatestUserProperties(mContext!!, mDataAdapter))
        )
    }

    private fun setActiveUserProperties() {
        val activeUserProperties =
            JSONObject(EventUtils.getActiveUserProperties(mContext!!, mDataAdapter)).apply {
               updateSdkVersionProperty(this)
            }
        trackUser(
            Constant.PRESET_EVENT_USER_SET_ONCE,
            activeUserProperties
        )
    }

    private fun updateSdkVersionProperty(jsonObject: JSONObject){
        //接入 SDK 的类型可能是 Android 或 Unity ，因此这里需动态获取
        getCommonProperties()?.get(Constant.COMMON_PROPERTY_SDK_TYPE)?.toString()?.let {
            if (it.isNotEmpty()) {
                jsonObject.put(
                    Constant.USER_PROPERTY_ACTIVE_SDK_TYPE,
                    it
                )
            }
        }
        //SDK 版本
        getCommonProperties()?.get(Constant.COMMON_PROPERTY_SDK_VERSION)?.toString()?.let {
            if (it.isNotEmpty()) {
                jsonObject.put(
                    Constant.USER_PROPERTY_ACTIVE_SDK_VERSION,
                    it
                )
            }
        }
    }


    private fun startAppAttribute() {
        if(mDataAdapter?.isAttributeInsert == true) return
        try {
            getAppAttribute()
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
            trackAppAttributeEvent(
                ReferrerDetails(null),
                "Exception: " + e.message.toString()
            )
        }
    }

    /**
     * 获取 app 归因属性
     */
//     TODO: ANR
    private fun getAppAttribute() {
        val referrerClient: InstallReferrerClient? = InstallReferrerClient.newBuilder(mContext).build()
        referrerClient?.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                try {
                    when (responseCode) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            // Connection established.
                            trackAppAttributeEvent(referrerClient.installReferrer, "")
                        }
                        else -> trackAppAttributeEvent(
                            ReferrerDetails(null),
                            "responseCode:$responseCode"
                        )

                    }
                    referrerClient.endConnection()
                } catch (e: Exception) {
                    trackAppAttributeEvent(
                        ReferrerDetails(null),
                        "responseCode:$responseCode" + ",Exception: " + e.message.toString()
                    )
                }

            }

            override fun onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                try {
                    trackAppAttributeEvent(
                        ReferrerDetails(null),
                        "onInstallReferrerServiceDisconnected"
                    )
                    referrerClient.endConnection()
                } catch (e: Exception) {
                    trackAppAttributeEvent(
                        ReferrerDetails(null),
                        "onInstallReferrerServiceDisconnected,Exception: " + e.message.toString()
                    )
                }

            }
        })
    }

    /**
     * 采集 app 归因属性事件
     */
    private fun trackAppAttributeEvent(response: ReferrerDetails, failedReason: String) {
        val isOK = failedReason.isBlank()
        trackNormal(
            Constant.PRESET_EVENT_APP_ATTRIBUTE,
            PropertyBuilder.newInstance()
                .append(
                    HashMap<String?, Any>().apply {

                        val cnl = mConfigOptions?.mChannel ?: ""
                        put(
                            Constant.ATTRIBUTE_PROPERTY_REFERRER_URL,
                            if (isOK) response.installReferrer + "&cnl=$cnl" else "cnl=$cnl"
                        )
                        put(
                            Constant.ATTRIBUTE_PROPERTY_REFERRER_CLICK_TIME,
                            if (isOK) response.referrerClickTimestampSeconds else 0
                        )
                        put(
                            Constant.ATTRIBUTE_PROPERTY_APP_INSTALL_TIME,
                            if (isOK) response.installBeginTimestampSeconds else 0
                        )
                        put(
                            Constant.ATTRIBUTE_PROPERTY_INSTANT_EXPERIENCE_LAUNCHED,
                            if (isOK) response.googlePlayInstantParam else false
                        )
                        put(
                            Constant.ATTRIBUTE_PROPERTY_CNL,
                            mConfigOptions?.mChannel ?: ""
                        )
                        if (!isOK) {
                            put(
                                Constant.ATTRIBUTE_PROPERTY_FAILED_REASON,
                                failedReason
                            )
                        }
                        mDataAdapter?.firstOpenTime?.let {
                            put(
                                Constant.ATTRIBUTE_PROPERTY_FIRST_OPEN_TIME,
                                it
                            )
                        }
                    }
                ).toJSONObject())
    }

    /**
     * 如果超过六分钟，则可能 app_engagement 上报有中断，重新触发
     */
    fun checkAppEngagementEvent() {
        if (!mDataAdapter?.lastEngagementTime.isNullOrEmpty() &&
            getRealTime() - (mDataAdapter?.lastEngagementTime?.toLong()
                ?: 0L) > Constant.APP_ENGAGEMENT_INTERVAL_TIME_LONG + 60 * 1000L
        ) {
            trackAppEngagementEvent()
        }
    }

    /**
     * 采集 app 活跃事件
     */
    private fun trackAppEngagementEvent() {
        EngagementTask("EngagementTask")
            .setIntervalWithFixedRate(Constant.APP_ENGAGEMENT_INTERVAL_TIME_INT)
            .setMaxLoopTime(Int.MAX_VALUE)
            .postAsync()
    }

    /**
     * 获取浏览器user_agent
     */
    private fun getUserAgentByUIThread() {
        launch (Dispatchers.Default){
            mContext?.let {
                if (mDataAdapter?.uaWebview?.isEmpty() == true){
                    mDataAdapter?.uaWebview = NetworkUtils.getUserAgent(it)
                }
                updateCommonProperties(COMMON_PROPERTY_USER_AGENT, mDataAdapter?.uaWebview ?: "")
            }
        }
    }


    inner class EngagementTask(name: String?) : TickTask(name) {

        override fun onTick(loopTime: Int) {
            trackNormal(
                Constant.PRESET_EVENT_APP_ENGAGEMENT
            )
            mDataAdapter?.lastEngagementTime = getRealTime().toString()
            //补发，以免异常情况获取不到 app_attribute 事件
            startAppAttribute()
        }

    }


}