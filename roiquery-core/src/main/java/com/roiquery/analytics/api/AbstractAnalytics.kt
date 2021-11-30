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
import com.roiquery.analytics.BuildConfig
import com.roiquery.analytics.Constant
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.core.AnalyticsManager
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.network.HttpPOSTResourceRemoteRepository
import com.roiquery.analytics.utils.*
import com.roiquery.cloudconfig.ROIQueryCloudConfig
import org.json.JSONObject
import org.qiyi.basecore.taskmanager.TM
import org.qiyi.basecore.taskmanager.TickTask
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledThreadPoolExecutor


abstract class AbstractAnalytics : IAnalytics {

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

    private var mUserAgent = ""

    private var mFirstOpenTime = ""

    companion object {
        const val TAG = "AnalyticsApi"

        // 配置
        internal var mConfigOptions: AnalyticsConfig? = null

        // SDK 配置是否初始化
        var mSDKConfigInit = false
    }


    constructor(context: Context?) {
        try {
            mContext = context
            initConfig(mContext!!.packageName)
            initLocalData()
            initTracker(mContext!!)
            initProperties()
            initCloudConfig()
            initAppLifecycleListener()
            getUserAgentByUIThread()
            trackPresetEvent()
            getGAID()
            getOAID()
            mSDKConfigInit = true
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
    }

    /**
     * 初始化本地数据
     */
    private fun initLocalData() {
        mDataAdapter = EventDateAdapter.getInstance(mContext!!, mContext!!.packageName)
        mDataAdapter?.timeOffset = ""
        mDataAdapter?.lastEngagementTime = ""
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
        eventName: String,
        properties: JSONObject? = null
    ) {
        try {
            val realEventName = assertEvent(eventName, properties)
            if(TextUtils.isEmpty(realEventName)) return
            //设置事件的基本信息
            val eventInfo = JSONObject(mEventInfo).apply {
                put(Constant.EVENT_INFO_TIME, getRealTime().toString())
                put(Constant.EVENT_INFO_NAME, realEventName)
                put(Constant.EVENT_INFO_SYN, DataUtils.getUUID())
                //向 app_attribute 增加 first_open_time 属性
                if (Constant.PRESET_EVENT_APP_FIRST_OPEN == eventName) {
                    getString(Constant.EVENT_INFO_TIME).apply {
                        mFirstOpenTime = this
                        mDataAdapter?.firstOpenTime = this
                    }
                }
                if (Constant.PRESET_EVENT_APP_ATTRIBUTE == eventName) {
                    if (properties?.has(Constant.ATTRIBUTE_PROPERTY_FIRST_OPEN_TIME) == false
                        || properties?.getString(Constant.ATTRIBUTE_PROPERTY_FIRST_OPEN_TIME)
                            ?.isEmpty() == true
                    ) {
                        properties.put(
                            Constant.ATTRIBUTE_PROPERTY_FIRST_OPEN_TIME,
                            if (mFirstOpenTime != "") mFirstOpenTime else mDataAdapter?.firstOpenTime
                        )
                    }
                }
            }
            //设置事件属性
            val eventProperties = JSONObject(mCommonProperties).apply {
                //合并用户自定义属性和通用属性
                DataUtils.mergeJSONObject(properties, this)
            }
            //设置事件属性
            eventInfo.put(Constant.EVENT_INFO_PROPERTIES, eventProperties)

            val data = JSONObject().apply {
                put(Constant.EVENT_BODY, eventInfo)
                put(Constant.EVENT_TIME_CALIBRATED, isTimeCalibrated())
            }

            mAnalyticsManager?.enqueueEventMessage(realEventName, data)

        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
            trackQualityEvent("trackEvent&&$eventName&& ${e.message}")
        }
    }

    fun trackQualityEvent(qualityInfo: String) {

    }

    /**
     * 获取并配置 事件一些基本属性
     *
     * @return
     */
    protected open fun initEventInfo() {
        mEventInfo = EventUtils.getEventInfo(mContext!!,mDataAdapter)
    }

    fun updateEventInfo(key: String, value: String) {
        mEventInfo?.put(key, value)
    }

    /**
     * 获取并配置 事件通用属性
     *
     * @return
     */
    protected open fun initCommonProperties() {
        if (ProcessUtils.isMainProcess(mContext as Application?)) {
            mDataAdapter?.eventSession = DataUtils.getSession()
        }
        mCommonProperties = EventUtils.getCommonProperties(mContext!!,mDataAdapter);
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
        } else if(!EventUtils.isValidEventName(eventName)){
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

    open fun isTimeCalibrated() = !(mDataAdapter?.timeOffset).isNullOrEmpty()

    /**
     * 校准时间.
     * @param timestamp 当前时间戳
     */
    open fun calibrateTime(timestamp: Long) {
        mDataAdapter?.timeOffset = (timestamp - System.currentTimeMillis()).toString()
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
                    updateEventInfo(Constant.EVENT_INFO_OAID, oaid)
                }

                override fun onOAIDGetError(exception: java.lang.Exception) {
                    // 获取OAID失败
                    LogUtils.printStackTrace(exception)
                    trackQualityEvent("getOAID&& ${exception.message}")

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
        LogUtils.d("getGAID", "start")
        GaidHelper.getAdInfo(
            mContext?.applicationContext,
            object : GaidHelper.GaidListener {
                override fun onSuccess(info: GaidHelper.AdIdInfo) {
                    mDataAdapter?.gaid = info.adId
                    updateEventInfo(Constant.EVENT_INFO_GAID, info.adId)
                    LogUtils.d("getGAID", "onSuccess")
                }

                override fun onException(exception: java.lang.Exception) {
                    LogUtils.d("getGAID", "onException:" + exception.message.toString())
                }
            })
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
        trackAppOpenEvent()
        startAppAttribute()
        trackAppEngagementEvent()
    }

    /**
     * 采集app 启动事件
     */
    private fun trackAppOpenEvent() {
        if (mDataAdapter?.isFirstOpen == true) {
            track(Constant.PRESET_EVENT_APP_FIRST_OPEN)
            mDataAdapter?.isFirstOpen = false
        } else {
            track(Constant.PRESET_EVENT_APP_OPEN)
        }
    }

    private fun checkAttribute(entrance: String) :Boolean{
        mDataAdapter?.attributedCount?.let {
            return if (it > 6){
                false
            } else {
                LogUtils.d("checkAttribute: $entrance", it)
                mDataAdapter?.attributedCount = it + 1
                true
            }
        }
        return false

    }

    private fun startAppAttribute() {
        try {
            getAppAttribute("startAppAttribute")
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
            trackAppAttributeEvent(
                "startAppAttribute Exception",
                ReferrerDetails(null),
                "Exception: " + e.message.toString()
            )
        }

    }

    /**
     * 获取 app 归因属性
     */
    private fun getAppAttribute(entrance: String) {
//        if (!checkAttribute(entrance)) return
        val referrerClient: InstallReferrerClient? = InstallReferrerClient.newBuilder(mContext).build()
        referrerClient?.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                try {
                    when (responseCode) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            // Connection established.
                            trackAppAttributeEvent("onInstallReferrerSetupFinished $responseCode",referrerClient.installReferrer, "")
                        }
                        else -> trackAppAttributeEvent(
                            "onInstallReferrerSetupFinished $responseCode",
                            ReferrerDetails(null),
                            "responseCode:$responseCode"
                        )

                    }
                    referrerClient.endConnection()
                } catch (e: Exception) {
                    trackAppAttributeEvent(
                        "onInstallReferrerSetupFinished Exception",
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
                        "onInstallReferrerServiceDisconnected",
                        ReferrerDetails(null),
                        "onInstallReferrerServiceDisconnected"
                    )
                    referrerClient.endConnection()
                } catch (e: Exception) {
                    trackAppAttributeEvent(
                        "onInstallReferrerServiceDisconnected Exception",
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
    private fun trackAppAttributeEvent(entrance: String,response: ReferrerDetails, failedReason: String) {
        if (!checkAttribute(entrance)) return
        val isOK = failedReason.isBlank()
        track(
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
                        put(
                            Constant.ATTRIBUTE_PROPERTY_USER_AGENT,
                            mUserAgent
                        )
                        if (!isOK) {
                            put(
                                Constant.ATTRIBUTE_PROPERTY_FAILED_REASON,
                                failedReason
                            )
                        }
                    }
                ).toJSONObject())
    }

    /**
     * 如果超过六分钟，则可能 app_engagement 上报有中断，重新触发
     */
    fun checkAppEngagementEvent(){
        if (!mDataAdapter?.lastEngagementTime.isNullOrEmpty() &&
        getRealTime() - (mDataAdapter?.lastEngagementTime?.toLong() ?: 0L) > Constant.APP_ENGAGEMENT_INTERVAL_TIME_LONG + 60 * 1000L){
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
        ThreadUtils.runOnUiThread {
            mContext?.let {
                mUserAgent = NetworkUtils.getUserAgent(it)
            }
        }
    }


   inner class EngagementTask(name: String?) : TickTask(name) {

        override fun onTick(loopTime: Int) {
            track(
                Constant.PRESET_EVENT_APP_ENGAGEMENT,
                PropertyBuilder.newInstance()
                    .append(
                        Constant.ENGAGEMENT_PROPERTY_IS_FOREGROUND,
                        mDataAdapter?.isAppForeground.toString()
                    )
                    .toJSONObject()
            )
            mDataAdapter?.lastEngagementTime = getRealTime().toString()
            //补发，以免异常情况获取不到 app_attribute 事件
            startAppAttribute()
        }

    }
}