package com.roiquery.analytics.api

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.github.gzuliyujiang.oaid.DeviceID
import com.github.gzuliyujiang.oaid.IOAIDGetter
import com.instacart.library.truetime.TrueTime
import com.instacart.library.truetime.utils.*
import com.roiquery.analytics.BuildConfig
import com.roiquery.analytics.Constant
import com.roiquery.analytics.R
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.core.AnalyticsManager
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.exception.InvalidDataException
import com.roiquery.analytics.network.HttpPOSTResourceRemoteRepository
import com.roiquery.analytics.utils.*
import com.roiquery.cloudconfig.ROIQueryCloudConfig
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.collections.HashMap


abstract class AbstractAnalytics : IAnalytics {

    protected val mContext: Context?

    // SDK 配置是否初始化
    protected var mSDKConfigInit = false

    // 事件信息，包含事件的基本数据
    private var mEventInfo: MutableMap<String, Any?>? = null

    // 事件通用属性
    private var mCommonProperties: MutableMap<String, Any?>? = null

    //是否允许采集did
    private var mDisableTrackDeviceId = false

    //事件采集管理
    protected var mTrackTaskManager: ExecutorService? = null

    //事件采集线程
//    private var mTrackTaskManagerThread: TrackTaskManagerThread? = null

    //采集 app 活跃事件线程池
    private var mEngagemenExecutors: ScheduledThreadPoolExecutor? = null

    //采集 、上报管理
    protected var mAnalyticsManager: AnalyticsManager? = null

    //本地数据适配器，包括sp、db的操作
    private var mDataAdapter: EventDateAdapter? = null

    //时间校准
    private var sCalibratedTime: ICalibratedTime? = null
    private val sCalibratedTimeLock = ReentrantReadWriteLock()
    private var mIsCalibrateTime: Boolean = false

    private var mPresetEvents = emptyArray<String>()
    private var mPresetProperties = emptyArray<String>()
    private var mFirstOpenTime = ""

    companion object {
        const val TAG = "AnalyticsApi"

        // 配置
        internal var mConfigOptions: AnalyticsConfig? = null
    }


    constructor(context: Context?) {
        mContext = context
        initConfig(context!!.packageName)
        initLocalData()
        initProperties()
        initCloudConfig()
        initAppLifecycleListener()
        initTrack(context)
        this.mSDKConfigInit = true
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
        if (!ProcessUtils.isMainProcess(mContext as Application?) || getSdkType() == Constant.SDK_TYPE_UNITY) {
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
        mTrackTaskManager = Executors.newSingleThreadExecutor()
        mAnalyticsManager = AnalyticsManager.getInstance(context, this as AnalyticsImp)
    }


    protected fun trackEvent(
        eventName: String,
        properties: JSONObject? = null
    ) {
        try {
            val realEventName = assertEvent(eventName, properties)
            //设置事件的基本信息
            val eventInfo = JSONObject(mEventInfo).apply {
                put(Constant.EVENT_INFO_TIME, getTime())
                put(Constant.EVENT_INFO_NAME, realEventName)
                put(Constant.EVENT_INFO_SYN, DataUtils.getUUID())
                if (Constant.PRESET_EVENT_APP_FIRST_OPEN == eventName) {
                    mFirstOpenTime = getString(Constant.EVENT_INFO_TIME)
                }
                if (Constant.PRESET_EVENT_APP_ATTRIBUTE == eventName) {
                    if (properties?.has(Constant.ATTRIBUTE_PROPERTY_FIRST_OPEN_TIME) == false
                        || properties?.getString(Constant.ATTRIBUTE_PROPERTY_FIRST_OPEN_TIME)
                            ?.isEmpty() == true
                    ) {
                        properties.put(Constant.ATTRIBUTE_PROPERTY_FIRST_OPEN_TIME, mFirstOpenTime)
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

            mAnalyticsManager?.enqueueEventMessage(realEventName, eventInfo)

        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
            trackQualityEvent("trackEvent&&$eventName&& ${e.message}")
        }
    }

    fun trackQualityEvent(qualityInfo: String) {
//        track(
//            Constant.PRESET_EVENT_APP_QUALITY,
//            JSONObject().apply {
//                put(Constant.APP_QUALITY_INFO, qualityInfo)
//            })
    }

    /**
     * 获取并配置 事件一些基本属性
     *
     * @return
     */
    protected open fun initEventInfo() {
        mEventInfo = mutableMapOf<String, Any?>().apply {
            put(
                Constant.EVENT_INFO_DID,
                DeviceUtils.getAndroidID(mContext!!)
            )//设备 ID。即唯一ID，区分设备的最小ID
            put(
                Constant.EVENT_INFO_ACID,
                mDataAdapter?.accountId
            )//登录账号id
            put(
                Constant.EVENT_INFO_GAID,
                mDataAdapter?.gaid.toString()
            )//谷歌广告标识id,不同app在同一个设备上gaid一样
            put(
                Constant.EVENT_INFO_OAID,
                mDataAdapter?.oaid.toString()
            )//华为广告标识id,不同app在同一个设备上oaid一样
            put(
                Constant.EVENT_INFO_APP_ID,
                mConfigOptions?.mAppId
            )//应用唯一标识,后台分配
            put(
                Constant.EVENT_INFO_PKG,
                mContext.packageName
            )//包名
            if (mConfigOptions?.mEnabledDebug == true) {
                put(Constant.EVENT_INFO_DEBUG, "true")
            }
        }

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
        mCommonProperties = mutableMapOf<String, Any?>().apply {
            put(
                Constant.COMMON_PROPERTY_EVENT_SESSION,
                mDataAdapter?.eventSession
            )//系列行为标识
            put(
                Constant.COMMON_PROPERTY_FIREBASE_IID,
                mDataAdapter?.fiid
            )//Firebase的app_instance_id
            put(
                Constant.COMMON_PROPERTY_MCC,
                DeviceUtils.getMcc(mContext!!)
            )//移动信号国家码
            put(
                Constant.COMMON_PROPERTY_MNC,
                DeviceUtils.getMnc(mContext)
            )//移动信号网络码
            put(
                Constant.COMMON_PROPERTY_OS_COUNTRY,
                DeviceUtils.getLocalCountry(mContext)
            )//系统国家
            put(
                Constant.COMMON_PROPERTY_OS_LANG,
                DeviceUtils.getLocaleLanguage()
            )//系统语言
            put(
                Constant.COMMON_PROPERTY_APP_VERSION_CODE,
                AppInfoUtils.getAppVersionCode(mContext).toString()
            )//应用版本号
            put(
                Constant.COMMON_PROPERTY_APP_VERSION_NAME,
                AppInfoUtils.getAppVersionName(mContext)
            )//应用版本号
            put(
                Constant.COMMON_PROPERTY_SDK_TYPE,
                Constant.SDK_TYPE_ANDROID
            )//接入 SDK 的类型，如 Android，iOS,Unity ,Flutter
            put(
                Constant.COMMON_PROPERTY_SDK_VERSION,
                BuildConfig.VERSION_NAME
            )//SDK 版本,如 1.1.2
            put(
                Constant.COMMON_PROPERTY_OS,
                Constant.SDK_TYPE_ANDROID
            )//如 Android、iOS 等
            put(
                Constant.COMMON_PROPERTY_OS_VERSION,
                DeviceUtils.oS
            )//操作系统版本,iOS 11.2.2、Android 8.0.0 等
            put(
                Constant.COMMON_PROPERTY_DEVICE_MANUFACTURER,
                DeviceUtils.manufacturer
            )//用户设备的制造商，如 Apple，vivo 等
            put(
                Constant.COMMON_PROPERTY_DEVICE_BRAND,
                DeviceUtils.brand
            )//设备品牌,如 Galaxy、Pixel
            put(
                Constant.COMMON_PROPERTY_DEVICE_MODEL,
                DeviceUtils.model
            )//设备型号,用户设备的型号，如 iPhone 8 等
            val size = DeviceUtils.getDeviceSize(mContext)
            put(
                Constant.COMMON_PROPERTY_SCREEN_HEIGHT,
                size[0].toString()
            )//屏幕高度
            put(
                Constant.COMMON_PROPERTY_SCREEN_WIDTH,
                size[1].toString()
            )//屏幕宽度
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


    fun updateCommonProperties(key: String, value: String) {
        mCommonProperties?.put(key, value)
    }

    private fun getSdkType(): String {
        if (mCommonProperties?.contains(Constant.COMMON_PROPERTY_SDK_TYPE) == true) {
            return mCommonProperties?.get(Constant.COMMON_PROPERTY_SDK_TYPE).toString()
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
            if (eventName.startsWith(Constant.PRESET_EVENT_TAG)) {//预置事件
                realEventName = eventName.replace(Constant.PRESET_EVENT_TAG, "")
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
     * 获取当前时间的 ITime 实例
     */
    private fun getTime(): String {
        sCalibratedTimeLock.readLock().lock()
        val result: ITime = if (null != sCalibratedTime) {
            LogUtils.i("getTime", "RTimeCalibrated")
            RTimeCalibrated(sCalibratedTime)
        } else {
            LogUtils.i("getTime", "RTime")
            RTime(TimeUtils.getTrueTime())
        }
        sCalibratedTimeLock.readLock().unlock()
        return result.time
    }

    /**
     * 校准时间.
     * @param timestamp 当前时间戳
     */
    open fun calibrateTime(timestamp: Long) {
        if (!mIsCalibrateTime) {
            setCalibratedTime(RCalibratedTime(timestamp))
            mIsCalibrateTime = true
        }
    }

    /**
     * 使用自定义的 ICalibratedTime 校准时间
     * @param calibratedTime ICalibratedTime 实例
     */
    private fun setCalibratedTime(calibratedTime: ICalibratedTime) {
        sCalibratedTimeLock.writeLock().lock()
        sCalibratedTime = calibratedTime
        sCalibratedTimeLock.writeLock().unlock()
    }

    /**
     * 初始化时间服务器
     * @param enableLog 是否开启log
     */
    private fun initNTP(enableLog: Boolean) {
        if (mContext != null) {
            val list = mutableListOf<String>().apply {
                add("pool.ntp.org")
                add("0.pool.ntp.org")
                add("1.pool.ntp.org")
                add("time.google.com")
                add("time.asia.apple.com")
                add("time.windows.com")
                add("asia.pool.ntp.org")
                add("time.euro.apple.com")
                add("time.apple.com")
                add("time.cloudflare.com")
            }
            Thread {
                try {
                    TrueTime.build()
                        .withNtpHosts(list)
                        .withLoggingEnabled(enableLog)
                        .withSharedPreferencesCache(mContext.applicationContext)
                        .withConnectionTimeout(Constant.NTP_TIME_OUT_M)
                        .initialize()
                } catch (e: Exception) {
                    e.printStackTrace()
                    trackQualityEvent("initNTP&& ${e.message}")
                    LogUtils.i("something went wrong when trying to initialize TrueTime", e.message)
                }
            }.start()
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
                    updateEventInfo(Constant.EVENT_INFO_OAID, oaid)
                }

                override fun onOAIDGetError(exception: java.lang.Exception) {
                    // 获取OAID失败
                    LogUtils.printStackTrace(exception)

                    trackQualityEvent("getOAID&& ${exception.message}")

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
                    updateEventInfo(Constant.EVENT_INFO_GAID, info.adId)
                    //由于id 比较重要，所以在id回调之后再进行事件采集
                    trackPresetEvent()
                }

                override fun onException(exception: java.lang.Exception) {
                    trackPresetEvent()
                    LogUtils.d("getGAID", exception.message.toString())
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
                ProcessUtils.getCurrentProcessName(mContext) + "is not main process"
            )
            return
        }
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
            } catch (e: Exception) {
                LogUtils.printStackTrace(e)
                trackAppAttributeEvent(
                    ReferrerDetails(null),
                    "Exception: " + e.message.toString()
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
        track(
            Constant.PRESET_EVENT_APP_ATTRIBUTE,
            PropertyBuilder.newInstance()
                .append(
                    HashMap<String?, Any>().apply {
                        put(
                            Constant.ATTRIBUTE_PROPERTY_REFERRER_URL,
                            if (isOK) response.installReferrer else ""
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
                    }
                ).toJSONObject())
    }

    /**
     * 采集 app 活跃事件
     */
    private fun tackAppEngagementEvent() {
        if (mEngagemenExecutors != null && !mEngagemenExecutors?.isShutdown!!) {
            mEngagemenExecutors?.shutdown()
        }
        mEngagemenExecutors = ScheduledThreadPoolExecutor(1)
        mEngagemenExecutors?.scheduleAtFixedRate(
            {
                track(
                    Constant.PRESET_EVENT_APP_ENGAGEMENT,
                    PropertyBuilder.newInstance()
                        .append(
                            Constant.ENGAGEMENT_PROPERTY_IS_FOREGROUND,
                            mDataAdapter?.isAppForeground.toString()
                        )
                        .toJSONObject()
                )
            },
            0,
            Constant.APP_ENGAGEMENT_INTERVAL_TIME,
            TimeUnit.MILLISECONDS
        )

    }


}