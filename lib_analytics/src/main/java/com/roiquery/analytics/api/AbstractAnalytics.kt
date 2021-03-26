package com.roiquery.analytics.api

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.RemoteException
import android.text.TextUtils
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.github.gzuliyujiang.oaid.DeviceID
import com.github.gzuliyujiang.oaid.IOAIDGetter
import com.instacart.library.truetime.TrueTime
import com.roiquery.analytics.BuildConfig
import com.roiquery.analytics.Constant
import com.roiquery.analytics.Constant.PRESET_EVENT_APP_ATTRIBUTE
import com.roiquery.analytics.Constant.PRESET_EVENT_APP_FIRST_OPEN
import com.roiquery.analytics.Constant.PRESET_EVENT_TAG
import com.roiquery.analytics.R
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.core.AnalyticsManager
import com.roiquery.analytics.core.TrackTaskManager
import com.roiquery.analytics.core.TrackTaskManagerThread
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.exception.InvalidDataException
import com.roiquery.analytics.network.HttpPOSTResourceRemoteRepository
import com.roiquery.analytics.utils.*
import com.roiquery.cloudconfig.ROIQueryCloudConfig
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


abstract class AbstractAnalytics : IAnalytics {

    protected val mContext: Context?

    // SDK 配置是否初始化
    protected var mSDKConfigInit = false

    // 是否为主进程
    var mIsMainProcess = false

    // 是否请求网络
    protected var mEnableNetworkRequest = true

    // 主进程名称
    private var mMainProcessName: String? = null

    // 事件信息，包含事件的基本数据
    private var mEventInfo: MutableMap<String, Any?>? = null

    // 事件通用属性
    private var mCommonProperties: MutableMap<String, Any?>? = null

    //是否允许采集did
    private var mDisableTrackDeviceId = false

    //事件采集管理
    protected var mTrackTaskManager: TrackTaskManager? = null

    //事件采集线程
    private var mTrackTaskManagerThread: TrackTaskManagerThread? = null

    //采集 app 活跃事件线程池
    private var mTrackEngagementEventExecutors: ScheduledThreadPoolExecutor? = null

    //采集 、上报管理
    protected var mAnalyticsManager: AnalyticsManager? = null

    //本地数据适配器，包括sp、db的操作
    private var mDataAdapter: EventDateAdapter? = null

    private var mPresetEvents = emptyArray<String>()
    private var mPresetProperties = emptyArray<String>()


    private var mFirstOpenTime = ""

    private var mFirstOpenTimeLock = Any()

    companion object {
        const val TAG = "AnalyticsApi"

        // 配置
        internal var mConfigOptions: AnalyticsConfig? = null
    }


    constructor(
        context: Context?
    ) {
        mContext = context
        initConfig(context!!.packageName)
        initLocalData()
        initProperties()
        initAppLifecycleListener()
        initCloudConfig()
        initTrack(context)
    }

    /**
     * 初始化本地数据
     */
    private fun initLocalData() {
        mDataAdapter = EventDateAdapter.getInstance(mContext!!, mContext.packageName)
    }

    /**
     * 初始化预置、通用属性
     */
    private fun initProperties() {
        initEventInfo()
        initCommonProperties()
        getGAID()
        getOAID()
    }

    /**
     * 监听应用生命周期
     */
    private fun initAppLifecycleListener() {
        if (!mIsMainProcess || getSdkType() == Constant.SDK_TYPE_UNITY) {
            return
        }
        AppLifecycleHelper()
            .register(mContext as Application?, object :
                AppLifecycleHelper.OnAppStatusListener {
                override fun onAppForeground() {
                    ROIQueryAnalytics.onAppForeground()
                }

                override fun onAppBackground() {
                    ROIQueryAnalytics.onAppBackground()
                }

            })
    }

    /**
     * 初始化数据采集
     */
    private fun initTrack(context: Context) {
        mTrackTaskManager = TrackTaskManager.instance
        mTrackTaskManager?.let {
            mConfigOptions?.mEnableTrack?.let { it1 -> it.setDataTrackEnable(it1) }
        }
        mTrackTaskManagerThread = TrackTaskManagerThread()
        //开启事件收集线程
        Thread(mTrackTaskManagerThread, "TaskQueueThread").start()

        mAnalyticsManager = AnalyticsManager.getInstance(context, this as AnalyticsImp)

    }


    protected fun trackEvent(
        eventName: String,
        properties: JSONObject? = null
    ) {
        try {
            try {
                val realEventName = assertEvent(eventName, properties)
                //设置事件的基本信息
                val eventInfo = JSONObject(mEventInfo).apply {
                    put("#event_time", TimeUtils.getTrueTime())
                    put("#event_name", realEventName)
                    put("#event_syn", DataUtils.getUUID())
                    if (PRESET_EVENT_APP_FIRST_OPEN == eventName) {
                        synchronized(mFirstOpenTimeLock) {
                            mFirstOpenTime = getString("#event_time")
                            LogUtils.e("first_open_time")
                        }
                    }
                    if (PRESET_EVENT_APP_ATTRIBUTE == eventName) {
                        if (properties?.getString("first_open_time")?.isEmpty() == true) {
                            properties.put("first_open_time", mFirstOpenTime)
                        }

                    }

                }
                //设置事件属性
                val eventProperties = JSONObject(mCommonProperties).apply {
                    //合并用户自定义属性和通用属性
                    DataUtils.mergeJSONObject(properties, this)
                }
                //设置事件属性
                eventInfo.put("properties", eventProperties)

                mAnalyticsManager?.enqueueEventMessage(realEventName, eventInfo)

            } catch (e: JSONException) {
                LogUtils.printStackTrace(e)
            }
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
    }

    /**
     * 获取并配置 事件一些基本属性
     *
     * @return
     */
    protected open fun initEventInfo() {
        mEventInfo = mutableMapOf<String, Any?>().apply {
            put("#did", DeviceUtils.getAndroidID(mContext!!))//设备 ID。即唯一ID，区分设备的最小ID
            put("#acid", mDataAdapter?.accountId)//登录账号id
            put("#gaid", mDataAdapter?.gaid.toString())//谷歌广告标识id,不同app在同一个设备上gdid一样
            put("#oaid", mDataAdapter?.oaid.toString())//华为广告标识id,不同app在同一个设备上oaid一样
            put("#app_id", mConfigOptions?.mAppId)//应用唯一标识,后台分配
            put("#pkg", mContext.packageName)//包名
        }

    }

    fun updateEventInfo(key: String, value: String) {
        mEventInfo?.remove(key)
        mEventInfo?.put(key, value)
    }

    /**
     * 获取并配置 事件通用属性
     *
     * @return
     */
    protected open fun initCommonProperties() {
        mCommonProperties = mutableMapOf<String, Any?>().apply {
            put("#mcc", DeviceUtils.getMcc(mContext!!))//移动信号国家码
            put("#mnc", DeviceUtils.getMnc(mContext))//移动信号网络码
            put("#os_country", DeviceUtils.getLocalCountry(mContext))//系统国家
            put("#os_lang", DeviceUtils.getLocaleLanguage())//系统语言
            put("#app_version_code", AppInfoUtils.getAppVersionCode(mContext).toString())//应用版本号
            put("#app_version_name", AppInfoUtils.getAppVersionName(mContext))//应用版本号
            put("#sdk_type", Constant.SDK_TYPE_ANDROID)//接入 SDK 的类型，如 Android，iOS,Unity ,Flutter
            put("#sdk_version", BuildConfig.VERSION_NAME)//SDK 版本,如 1.1.2
            put("#os", "Android")//如 Android、iOS 等
            put("#os_version", DeviceUtils.oS)//操作系统版本,iOS 11.2.2、Android 8.0.0 等
            put("#device_manufacturer", DeviceUtils.manufacturer)//用户设备的制造商，如 Apple，vivo 等
            put("#device_brand", DeviceUtils.brand)//设备品牌,如 Galaxy、Pixel
            put("#device_model", DeviceUtils.model)//设备型号,用户设备的型号，如 iPhone 8 等
            val size = DeviceUtils.getDeviceSize(mContext)
            put("#screen_height", size[0].toString())//屏幕高度
            put("#screen_width", size[1].toString())//屏幕宽度
        }
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


    private fun updateCommonProperties(key: String, value: String) {
        mCommonProperties?.remove(key)
        mCommonProperties?.put(key, value)
    }

    private fun getSdkType(): String {
        if (mCommonProperties?.contains("#sdk_type") == true) {
            return mCommonProperties?.get("#sdk_type").toString()
        }
        return ""
    }


    private fun assertEvent(
        eventName: String,
        properties: JSONObject? = null
    ): String {

        var realEventName = eventName
        if (mPresetEvents.isEmpty()) {
            mContext?.resources?.getStringArray(R.array.preset_events)?.let {
                mPresetEvents = it
            }
        }
        if (mPresetProperties.isEmpty()) {
            mContext?.resources?.getStringArray(R.array.preset_properties)?.let {
                mPresetProperties = it
            }
        }
        //检验事件名
        if (mPresetEvents.isNotEmpty() && eventName.isNotEmpty()) {
            if (eventName.startsWith(PRESET_EVENT_TAG)) {//预置事件
                realEventName = eventName.replace(PRESET_EVENT_TAG, "")
            } else if (mPresetEvents.contains(eventName)) {
                throw InvalidDataException("The eventName: $eventName is invalid.")
            }
        }
        //校验属性名、属性值
        if (mPresetProperties.isNotEmpty() && properties != null) {
            val iterator = properties.keys()
            while (iterator.hasNext()) {
                val key = iterator.next()
                if (null == key || key.isEmpty()) {
                    throw InvalidDataException("The key is empty.")
                }
                if (mPresetProperties.contains(key)) {
                    throw InvalidDataException("The property key: $key is invalid.")
                }
                try {
                    val value = properties[key]
                    if (value === JSONObject.NULL) {
                        iterator.remove()
                        continue
                    }
                    if (!(value is CharSequence || value is Number || value is JSONArray || value is Boolean || value is Date)) {
                        throw InvalidDataException(
                            "The property value must be an instance of "
                                    + "CharSequence/Number/Boolean/JSONArray. [key='" + key + "', value='" + value.toString()
                                    + "']"
                        )
                    }
                } catch (e: JSONException) {
                    throw InvalidDataException("Unexpected property key. [key='$key']")
                }
            }
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
            initNTP(configOptions.mEnabledDebug)
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

        this.mMainProcessName = AppInfoUtils.getMainProcessName(mContext)
        if (TextUtils.isEmpty(this.mMainProcessName)) {
            this.mMainProcessName =
                configBundle!!.getString(Constant.CONFIG_BUNDLE_KEY_MAIN_PROCESS_NAME)
        }
        mMainProcessName?.let {
            mIsMainProcess = AppInfoUtils.isMainProcess(mContext, it)
        }

        this.mSDKConfigInit = true
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
                JSONObject(mEventInfo).apply {
                    DataUtils.mergeJSONObject(JSONObject(mCommonProperties), this)
                }
            },
            mDataAdapter?.cloudConfigAesKey ?: "",
            {
                mDataAdapter?.cloudConfigAesKey = it
            }
        ) {
            LogUtils.d("CloudConfig", it)
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

    private fun initNTP(enableLog: Boolean) {
        if (mContext != null) {
            InitTrueTimeAsyncTask(mContext, enableLog).execute()
        }
    }

    /**
     * oaid 获取，异步
     */
    private fun getOAID() {
        try {
            val deviceId = DeviceID.with(mContext)
            if (!deviceId.supportOAID()) {
                // 不支持OAID，须自行生成GUID，然后存到`SharedPreferences`及`ExternalStorage`甚至`CloudStorage`。
                return
            }
            deviceId.doGet(object : IOAIDGetter {
                override fun onOAIDGetComplete(oaid: String) {
                    // 不同厂商的OAID格式是不一样的，可进行MD5、SHA1之类的哈希运算统一
                    //存入sp
                    mDataAdapter?.oaid = oaid
                    updateEventInfo("#oaid", oaid)
                }

                override fun onOAIDGetError(exception: java.lang.Exception) {
                    // 获取OAID失败
                    LogUtils.printStackTrace(exception)
                }
            })
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }

    }

    /**
     * gaid 获取，异步
     */
    private fun getGAID() {
        GaidHelper.getAdInfo(
            mContext?.applicationContext,
            object : GaidHelper.GaidListener {
                override fun onSuccess(info: GaidHelper.AdIdInfo) {
                    mDataAdapter?.gaid = info.adId
                    updateEventInfo("#gaid", info.adId)
                    //由于id 比较重要，所以在id回调之后再进行事件采集
                    trackPresetEvent()
                }

                override fun onException(exception: java.lang.Exception) {
                    trackPresetEvent()
                    LogUtils.printStackTrace(exception)
                }
            })
    }

    /**
     * 采集app 预置事件
     */
    private fun trackPresetEvent() {
        //子进程不采集
        if (!mIsMainProcess) return
        trackAppOpenEvent()
        tackAppEngagementEvent()
    }

    /**
     * 采集app 启动事件
     */
    private fun trackAppOpenEvent() {
        if (mDataAdapter?.isFirstOpen == true) {
            track(Constant.PRESET_EVENT_APP_FIRST_OPEN)
            mDataAdapter?.isFirstOpen = false
            try {
                getAppAttribute()
            } catch (e: RemoteException) {
                LogUtils.printStackTrace(e)
                trackAppAttributeEvent(
                    ReferrerDetails(null),
                    e.message.toString()
                )
            }
        } else {
            track(Constant.PRESET_EVENT_APP_OPEN)
        }
    }

    /**
     * 获取 app 归因属性
     */
    private fun getAppAttribute() {
        var referrerClient: InstallReferrerClient? =
            InstallReferrerClient.newBuilder(mContext).build()
        referrerClient?.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        // Connection established.
                        trackAppAttributeEvent(referrerClient.installReferrer, "")
                    }
                    else -> trackAppAttributeEvent(
                        referrerClient.installReferrer,
                        responseCode.toString()
                    )
                }
                referrerClient.endConnection()
            }

            override fun onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                trackAppAttributeEvent(
                    referrerClient.installReferrer,
                    "onInstallReferrerServiceDisconnected"
                )
                referrerClient.endConnection()
            }
        })
    }

    /**
     * 采集 app 归因属性事件
     */
    private fun trackAppAttributeEvent(response: ReferrerDetails, failedReason: String) {
        val property = if (failedReason.isBlank()) {
            synchronized(mFirstOpenTimeLock) {
                PropertyBuilder.newInstance()
                    .append(
                        HashMap<String?, Any>().apply {
                            put("referrer_url", response.installReferrer)
                            put("referrer_click_time", response.referrerClickTimestampSeconds)
                            put("app_install_time", response.installBeginTimestampSeconds)
                            put("first_open_time", mFirstOpenTime)
                            put("instant_experience_launched", response.googlePlayInstantParam)
                        }
                    ).toJSONObject()
            }
        } else {
            PropertyBuilder.newInstance().append("failed_reason", failedReason).toJSONObject()
        }

        track(Constant.PRESET_EVENT_APP_ATTRIBUTE, property)
    }

    /**
     * 采集 app 活跃事件
     */
    private fun tackAppEngagementEvent() {
        if (mTrackEngagementEventExecutors != null && !mTrackEngagementEventExecutors?.isShutdown!!) {
            mTrackEngagementEventExecutors?.shutdown()
        }
        mTrackEngagementEventExecutors = ScheduledThreadPoolExecutor(1)
        mTrackEngagementEventExecutors?.scheduleAtFixedRate(
            {
                track(
                    Constant.PRESET_EVENT_APP_ENGAGEMENT,
                    PropertyBuilder.newInstance()
                        .append("is_foreground", mDataAdapter?.isAppForeground.toString())
                        .toJSONObject()
                )
            },
            0,
            Constant.APP_ENGAGEMENT_INTERVAL_TIME,
            TimeUnit.MILLISECONDS
        )

    }


    // a little part of me died, having to use this
    private class InitTrueTimeAsyncTask(
        var context: Context,
        var isLog: Boolean
    ) :
        AsyncTask<Void?, Void?, Void?>() {
        override fun doInBackground(vararg params: Void?): Void? {
            try {
                TrueTime.build()
                    .withNtpHost(Constant.NTP_HOST)
                    .withLoggingEnabled(isLog)
                    .withSharedPreferencesCache(context.applicationContext)
                    .withConnectionTimeout(Constant.NTP_TIME_OUT_M)
                    .initialize()
            } catch (e: IOException) {
                e.printStackTrace()
                LogUtils.e("something went wrong when trying to initialize TrueTime", e)
            }
            return null
        }
    }
}