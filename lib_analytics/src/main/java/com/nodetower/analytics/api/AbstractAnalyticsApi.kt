package com.nodetower.analytics.api

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import com.nodetower.analytics.BuildConfig
import com.nodetower.analytics.Constant
import com.nodetower.analytics.config.AnalyticsConfigOptions
import com.nodetower.analytics.core.AnalyticsManager
import com.nodetower.analytics.core.TrackTaskManager
import com.nodetower.analytics.core.TrackTaskManagerThread
import com.nodetower.analytics.data.DbAdapter
import com.nodetower.analytics.utils.DataHelper.assertKey
import com.nodetower.analytics.utils.DataHelper.assertPropertyTypes
import com.nodetower.analytics.utils.DataUtils
import com.nodetower.analytics.utils.GaidHelper
import com.nodetower.analytics.utils.OaidHelper
import com.nodetower.base.utils.AppInfoUtils
import com.nodetower.base.utils.DeviceUtils
import com.nodetower.base.utils.LogUtils
import org.json.JSONException
import org.json.JSONObject
import java.security.SecureRandom
import java.util.*


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
    private var mEventInfo: Map<String, Any?>? = null

    // 事件通用属性
    private var mCommonProperties: Map<String, Any?>? = null

    private var mDisableTrackDeviceId = false

    protected var mTrackTaskManager: TrackTaskManager? = null

    private var mTrackTaskManagerThread: TrackTaskManagerThread? = null

    protected var mAnalyticsManager: AnalyticsManager? = null

    private var mDbAdapter: DbAdapter? = null

    companion object {
        const val TAG = "NT.AnalyticsApi"
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
        mDbAdapter = DbAdapter.getInstance(mContext!!, mContext.packageName)
        getGaid()
        getOaid()

        mEventInfo = setupEventInfo()
        mCommonProperties = setupCommonProperties()
        initConfig(serverUrl, mContext.packageName)
        mTrackTaskManager = TrackTaskManager.instance
        mTrackTaskManager?.let {
            mConfigOptions.isDataCollectEnable.let { it1 -> it.setDataCollectEnable(it1) }
        }
        mTrackTaskManagerThread = TrackTaskManagerThread()
        Thread(mTrackTaskManagerThread, "TaskQueueThread").start()

        mAnalyticsManager = AnalyticsManager.getInstance(mContext, this as RoiqueryAnalyticsAPI)

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
            assertKey(eventName)
            //检查事件属性
            assertPropertyTypes(properties)

            try {
                //设置事件的基本信息
                val eventInfo = JSONObject(mEventInfo).apply {
                    put("#event_time", System.currentTimeMillis().toString())
                    put("#event_name", eventName)
                    put("#event_syn", SecureRandom().nextInt().toString())
                }
                //设置事件属性
                val eventProperties = JSONObject(mCommonProperties).apply {
                    // 屏幕方向
                    val screenOrientation: String? = getScreenOrientation()
                    if (!screenOrientation.isNullOrEmpty()) {
                        put("#screen_orientation", screenOrientation)
                    }
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
    protected open fun setupEventInfo(): Map<String, Any?>? =
        Collections.unmodifiableMap(HashMap<String, String?>().apply {
            put("#did", DeviceUtils.getAndroidID(mContext!!))//设备 ID。即唯一ID，区分设备的最小ID
            put("#acid", getAccountId())//登录账号id
            put("#gaid", mDbAdapter?.gaid.toString())//谷歌广告标识id,不同app在同一个设备上gdid一样
            put("#oaid", mDbAdapter?.oaid.toString())//华为广告标识id,不同app在同一个设备上oaid一样
            put("#app_id", getAppId())//应用唯一标识,后台分配
            put("#pkg", mContext.packageName)//包名
        })

    /**
     * 获取并配置 事件通用属性
     *
     * @return
     */
    protected open fun setupCommonProperties(): Map<String, Any>? =
        Collections.unmodifiableMap(HashMap<String, String>().apply {
            put("#mcc", DeviceUtils.getMcc(mContext!!).toString())//移动信号国家码
            put("#mnc", DeviceUtils.getMnc(mContext).toString())//移动信号网络码
            put("#os_country", DeviceUtils.getLocalCountry(mContext))//系统国家
            put("#os_lang", DeviceUtils.getLocaleLanguage())//系统语言
            put("#app_version_code", AppInfoUtils.getAppVersionCode(mContext).toString())//应用版本号
            put("#sdk_type", "Android")//接入 SDK 的类型，如 Android，iOS,Unity ,Flutter
            put("#sdk_version", BuildConfig.VERSION_NAME)//SDK 版本,如 1.1.2
            put("#os", "Android")//如 Android、iOS 等
            put("#os_version", DeviceUtils.oS)//操作系统版本,iOS 11.2.2、Android 8.0.0 等
            put("#browser_version", "")//浏览器版本,用户使用的浏览器的版本，如 Chrome 61.0，Firefox 57.0 等
            put("#device_manufacturer", DeviceUtils.manufacturer)//用户设备的制造商，如 Apple，vivo 等
            put("#device_brand", DeviceUtils.brand)//设备品牌,如 Galaxy、Pixel
            put("#device_model", DeviceUtils.model)//设备型号,用户设备的型号，如 iPhone 8 等
            val size = DeviceUtils.getDeviceSize(mContext)
            put("#screen_height", size[0].toString())//屏幕高度
            put("#screen_width", size[1].toString())//屏幕宽度
        })


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

            initLog(configOptions.mEnabledDebug,configOptions.mLogLevel)
            setServerUrl(serverURL)

            if (configOptions.mFlushInterval == 0) {
                configOptions.setFlushInterval(
                    15000
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
                mDbAdapter?.let {
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
                initLog(it.mEnabledDebug,it.mLogLevel)
            }
        }
    }

    private fun initLog(enable :Boolean, logLevel:Int){
        LogUtils.getConfig().apply {
            isLogSwitch = enable
            globalTag = Constant.LOG_TAG
            setConsoleSwitch(enable)
            setConsoleFilter(logLevel)
        }
    }


    fun getOaid() {
        Thread {
            DbAdapter.getInstance()?.commitOaid(
                mContext?.let {
                    OaidHelper.getOAID(it) }
            )
        }.start()
    }

    fun getGaid() {
        GaidHelper.getAdInfo(mContext?.applicationContext, object : GaidHelper.GaidListener {
            override fun onSuccess(info: GaidHelper.AdIdInfo) {
                DbAdapter.getInstance()?.commitGaid(info.adId)
            }
            override fun onException(exception: java.lang.Exception) {
                LogUtils.printStackTrace(exception)
            }
        })
    }


}