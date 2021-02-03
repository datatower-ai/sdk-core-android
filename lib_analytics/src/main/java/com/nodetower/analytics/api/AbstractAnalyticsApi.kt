package com.nodetower.analytics.api

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.github.gzuliyujiang.oaid.DeviceID
import com.github.gzuliyujiang.oaid.IOAIDGetter
import com.nodetower.analytics.BuildConfig
import com.nodetower.analytics.Constant
import com.nodetower.analytics.config.AnalyticsConfigOptions
import com.nodetower.analytics.core.AnalyticsManager
import com.nodetower.analytics.core.TrackTaskManager
import com.nodetower.analytics.core.TrackTaskManagerThread
import com.nodetower.analytics.data.DataParams
import com.nodetower.analytics.data.DateAdapter
import com.nodetower.analytics.data.persistent.PersistentAppFirstOpen
import com.nodetower.analytics.data.persistent.PersistentLoader
import com.nodetower.analytics.utils.DataHelper.assertPropertyTypes
import com.nodetower.analytics.utils.DataUtils
import com.nodetower.analytics.utils.GaidHelper
import com.nodetower.base.utils.AppInfoUtils
import com.nodetower.base.utils.DeviceUtils
import com.nodetower.base.utils.LogUtils
import org.json.JSONException
import org.json.JSONObject
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


abstract class AbstractAnalyticsApi : IAnalyticsApi {

    protected val mContext: Context?

    // 数据上报 地址
    protected var mServerUrl: String? = null

    // SDK 配置是否初始化
    protected var mSDKConfigInit = false

    // 是否为主进程
    var mIsMainProcess = false

    // 是否请求网络
    protected var mEnableNetworkRequest = true

    // 主进程名称
    private var mMainProcessName: String? = null

    // Session 时长,设定app进入后台超过30s为应用退出
    protected var mSessionTime = 30 * 1000

    // 事件信息，包含事件的基本数据
    private var mEventInfo: MutableMap<String, Any?>? = null

    // 事件通用属性
    private var mCommonProperties: MutableMap<String, Any?>? = null

    //是否允许采集did
    private var mDisableTrackDeviceId = false

    //事件采集管理
    protected var mTrackTaskManager: TrackTaskManager? = null

    //事件采集线程
    var mTrackTaskManagerThread: TrackTaskManagerThread? = null

    //采集 app 活跃事件线程池
    private var mTrackEngagementEventExecutors: ScheduledThreadPoolExecutor? = null


    //采集 、上报管理
    protected var mAnalyticsManager: AnalyticsManager? = null

    //本地数据适配器，包括sp、db的操作
    private var mDataAdapter: DateAdapter? = null

    //是否为第一打开app标记
    var mAppFirstOpen: PersistentAppFirstOpen? = null


    companion object {
        const val TAG = "AnalyticsApi"

        // 适配多进程场景
        val S_INSTANCE_MAP: MutableMap<Context, RoiqueryAnalyticsAPI> =
            HashMap<Context, RoiqueryAnalyticsAPI>()

        // 配置
        lateinit var mConfigOptions: AnalyticsConfigOptions
    }


    constructor(
        context: Context?,
        serverUrl: String = ""
    ) {
        mContext = context
        mServerUrl = serverUrl
        initConfig(serverUrl, context!!.packageName)
        initLocalData(context)
        initProperties()
        initTrack(context)
    }

    /**
     * 初始化本地数据
     */
    private fun initLocalData(context: Context) {
        PersistentLoader.initLoader(context)
        mAppFirstOpen =
            PersistentLoader.loadPersistent(DataParams.TABLE_APP_FIRST_OPEN) as PersistentAppFirstOpen?

        mDataAdapter = DateAdapter.getInstance(mContext!!, mContext.packageName)
    }

    /**
     * 初始化预置、通用属性
     */
    private fun initProperties() {
        getGAID()
        getOAID()
        initEventInfo()
        initCommonProperties()
    }

    /**
     * 初始化数据采集
     */
    private fun initTrack(context: Context) {
        mTrackTaskManager = TrackTaskManager.instance
        mTrackTaskManager?.let {
            mConfigOptions.isDataCollectEnable.let { it1 -> it.setDataCollectEnable(it1) }
        }
        mTrackTaskManagerThread = TrackTaskManagerThread()
        //开启事件收集线程
        Thread(mTrackTaskManagerThread, "TaskQueueThread").start()

        mAnalyticsManager = AnalyticsManager.getInstance(context, this as RoiqueryAnalyticsAPI)

    }

    constructor() {
        mContext = null
    }

    override fun setServerUrl(serverUrl: String?) {
        mServerUrl = serverUrl
    }

    protected open fun addTimeProperty(jsonObject: JSONObject) {
        if (!jsonObject.has("#time")) {
            try {
                jsonObject.put("#time", Date(System.currentTimeMillis()))
            } catch (e: JSONException) {
                LogUtils.printStackTrace(e)
            }
        }
    }


    /**
     * 处理渠道相关的事件
     *
     * @param runnable 任务
     */
    protected open fun transformInstallationTaskQueue(runnable: Runnable?) {
        // 禁用采集事件时，先计算基本信息存储到缓存中
        if (!mConfigOptions.isDataCollectEnable) {
            mTrackTaskManager!!.addTrackEventTask { mTrackTaskManager!!.transformTaskQueue(runnable!!) }
            return
        }
        mTrackTaskManager!!.addTrackEventTask(runnable!!)
    }

    protected fun trackEvent(
        eventName: String,
        properties: JSONObject? = null
    ) {
        try {
            //检查事件名
//            assertKey(eventName)
            //检查事件属性
            assertPropertyTypes(properties)

            try {
                //设置事件的基本信息
                val eventInfo = JSONObject(mEventInfo).apply {
                    put("#event_time", System.currentTimeMillis().toString())
                    put("#event_name", eventName)
                    put("#event_syn", DataUtils.getUUID())
                }
                //设置事件属性
                val eventProperties = JSONObject(mCommonProperties).apply {
                    // 屏幕方向
//                    val screenOrientation: String? = getScreenOrientation()
//                    if (!screenOrientation.isNullOrEmpty()) {
//                        put("#screen_orientation", screenOrientation)
//                    }
                    //合并用户自定义属性和通用属性
                    DataUtils.mergeJSONObject(properties, this)
                }
                //设置事件属性
                eventInfo.put("properties", eventProperties)

                mAnalyticsManager?.enqueueEventMessage(eventName, eventInfo)

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
            put("#acid", accountId)//登录账号id
            put("#gaid", mDataAdapter?.gaid.toString())//谷歌广告标识id,不同app在同一个设备上gdid一样
            put("#oaid", mDataAdapter?.oaid.toString())//华为广告标识id,不同app在同一个设备上oaid一样
            put("#app_id", getAppId())//应用唯一标识,后台分配
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
            put("#sdk_type", "Android")//接入 SDK 的类型，如 Android，iOS,Unity ,Flutter
            put("#sdk_version", BuildConfig.VERSION_NAME)//SDK 版本,如 1.1.2
            put("#os", "Android")//如 Android、iOS 等
            put("#os_version", DeviceUtils.oS)//操作系统版本,iOS 11.2.2、Android 8.0.0 等
            put(
                "#browser_version",
                DeviceUtils.getBrowserOS(mContext)
            )//浏览器版本,用户使用的浏览器的版本，如 Chrome 61.0，Firefox 57.0 等
            put("#device_manufacturer", DeviceUtils.manufacturer)//用户设备的制造商，如 Apple，vivo 等
            put("#device_brand", DeviceUtils.brand)//设备品牌,如 Galaxy、Pixel
            put("#device_model", DeviceUtils.model)//设备型号,用户设备的型号，如 iPhone 8 等
            val size = DeviceUtils.getDeviceSize(mContext)
            put("#screen_height", size[0].toString())//屏幕高度
            put("#screen_width", size[1].toString())//屏幕宽度
        }
    }


    /**
     * 初始化配置
     */
    protected open fun initConfig(serverURL: String, packageName: String) {
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

        mConfigOptions.let { configOptions ->

            initLog(configOptions.mEnabledDebug, configOptions.mLogLevel)
            setServerUrl(serverURL)

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
            if (configOptions.isSubProcessFlushData) {
                mDataAdapter?.let {
                    if (it.isFirstProcess) {
                        //如果是首个进程
                        it.commitFirstProcessState(false)
                        it.commitSubProcessFlushState(false)
                    }
                }
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


    protected open fun applySAConfigOptions() {
        mConfigOptions.let {
            if (it.mEnabledDebug) {
                initLog(it.mEnabledDebug, it.mLogLevel)
            }
        }
    }

    private fun initLog(enable: Boolean, logLevel: Int) {
        LogUtils.getConfig().apply {
            isLogSwitch = enable
            globalTag = Constant.LOG_TAG
            setConsoleSwitch(enable)
            setConsoleFilter(logLevel)
        }
    }

    /**
     * oaid 获取，异步
     */
    private fun getOAID() {
        val deviceId = DeviceID.with(mContext)
        if (!deviceId.supportOAID()) {
            // 不支持OAID，须自行生成GUID，然后存到`SharedPreferences`及`ExternalStorage`甚至`CloudStorage`。
            return
        }
        deviceId.doGet(object : IOAIDGetter {
            override fun onOAIDGetComplete(oaid: String) {
                // 不同厂商的OAID格式是不一样的，可进行MD5、SHA1之类的哈希运算统一
                //存入sp
                mDataAdapter?.commitOaid(oaid)
                updateEventInfo("#oaid", oaid)
            }

            override fun onOAIDGetError(exception: java.lang.Exception) {
                // 获取OAID失败

            }
        })
    }

    /**
     * gaid 获取，异步
     */
    private fun getGAID() {
        GaidHelper.getAdInfo(
            mContext?.applicationContext,
            object : GaidHelper.GaidListener {
                override fun onSuccess(info: GaidHelper.AdIdInfo) {
                    //存入sp
                    mDataAdapter?.commitGaid(info.adId)
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
        trackAppOpenEvent()
        tackAppEngagementEvent()
    }

    /**
     * 采集app 启动事件
     */
    private fun trackAppOpenEvent() {
        if (mAppFirstOpen?.get() != null) {
            if (mAppFirstOpen?.get()!!) {
                track(Constant.PRESET_EVENT_APP_FIRST_OPEN)
                mAppFirstOpen?.commit(false)
                getAppAttribute()
            } else {
                track(Constant.PRESET_EVENT_APP_OPEN)
            }
        } else {
            track(Constant.PRESET_EVENT_APP_FIRST_OPEN)
            mAppFirstOpen?.commit(false)
            getAppAttribute()
        }
    }

    /**
     * 获取 app 归因属性
     */
    private fun getAppAttribute() {
        var referrerClient: InstallReferrerClient =
            InstallReferrerClient.newBuilder(mContext).build()
        referrerClient.startConnection(object : InstallReferrerStateListener {

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
            PropertyBuilder.newInstance()
                .append(
                    HashMap<String?, Any>().apply {
                        put("referrerUrl", response.installReferrer)
                        put("referrerClickTime", response.referrerClickTimestampSeconds)
                        put("appInstallTime", response.installBeginTimestampSeconds)
                        put("instantExperienceLaunched", response.googlePlayInstantParam)
                    }
                ).toJSONObject()

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
                track(Constant.PRESET_EVENT_APP_ENGAGEMENT)
            },
            0,
            Constant.APP_ENGAGEMENT_INTERVAL_TIME,
            TimeUnit.MILLISECONDS
        )

    }
}