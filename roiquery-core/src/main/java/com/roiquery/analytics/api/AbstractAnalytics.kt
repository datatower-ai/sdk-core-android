package com.roiquery.analytics.api

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.github.gzuliyujiang.oaid.DeviceID
import com.github.gzuliyujiang.oaid.IGetter
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.roiquery.analytics.Constant
import com.roiquery.analytics.Constant.EVENT_INFO_SYN
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.core.AnalyticsManager
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.network.HttpPOSTResourceRemoteRepository
import com.roiquery.analytics.taskscheduler.SchedulerTask
import com.roiquery.analytics.taskscheduler.TaskScheduler
import com.roiquery.analytics.utils.*
import com.roiquery.cloudconfig.ROIQueryCloudConfig
import com.roiquery.quality.ROIQueryErrorParams
import com.roiquery.quality.ROIQueryQualityHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext


abstract class AbstractAnalytics(context: Context?) : IAnalytics , CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()

    private var mContext: Context? = null

    //事件采集管理线程池
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
            trackPresetEvent()
            mSDKConfigInit = true
            LogUtils.d("ROIQuery","init succeed")
        } catch (e: Exception) {
            if (!ROIQueryAnalytics.isSDKInitSuccess()) {
                ROIQueryQualityHelper.instance.reportQualityMessage(
                    ROIQueryErrorParams.CODE_INIT_EXCEPTION,e.message,ROIQueryErrorParams.INIT_EXCEPTION,ROIQueryErrorParams.TYPE_ERROR
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
        mDataAdapter?.enableUpload = true
    }

    /**
     * 初始化预置、通用属性
     */
    private fun initProperties() {
        PropertyManager.instance.init(mContext, mDataAdapter, mConfigOptions)
    }

    /**
     * 监听应用生命周期
     */
    private fun initAppLifecycleListener() {
        if (ProcessUtils.isMainProcess(ROIQueryAnalytics.mContext as Application?)) {
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

            if (!isPreset && !assertEvent(eventName, properties)) return

            var isTimeVerify: Boolean

            launch(Dispatchers.Default) {
                //设置事件的基本信息
                val eventInfo = JSONObject(PropertyManager.instance.getEventInfo()).apply {
                    TimeCalibration.instance.getVerifyTimeAsync().apply {
                        isTimeVerify = this!=TimeCalibration.TIME_NOT_VERIFY_VALUE
                        // 如果时间已校准，则 保存当前时间，否则保存当前时间的系统休眠时间差用做上报时时间校准依据
                        put(Constant.EVENT_INFO_TIME, if (isTimeVerify) this else TimeCalibration.instance.getSystemHibernateTimeGap())
                        put(Constant.EVENT_INFO_NAME, eventName)
                        put(Constant.EVENT_INFO_TYPE, eventType)
                        put(EVENT_INFO_SYN, DataUtils.getUUID())
                    }
                }

                //事件属性, 常规事件与用户属性类型区分
                val eventProperties = if (eventType == Constant.EVENT_TYPE_TRACK) {
                    JSONObject(PropertyManager.instance.getCommonProperties()).apply {
                        //应用是否在前台, 需要动态添加
                        put(
                            Constant.COMMON_PROPERTY_IS_FOREGROUND,
                            mDataAdapter?.isAppForeground
                        )
                        //硬盘使用率
                        put(
                            Constant.COMMON_PROPERTY_STORAGE_USED,
                            MemoryUtils.getStorageUsed(mContext)
                        )
                        //内存使用率
                        put(
                            Constant.COMMON_PROPERTY_MEMORY_USED,
                            MemoryUtils.getMemoryUsed(mContext)
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
                    eventName, data, eventInfo.optString(
                        EVENT_INFO_SYN
                    )
                )
                //如果有插入失败的数据，则一起插入
                mAnalyticsManager?.enqueueErrorInsertEventMessage()
            }
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
            trackQualityEvent("trackEvent&&$eventName&& ${e.message}")
        }
    }

    private fun trackQualityEvent(qualityInfo: String) {
        ROIQueryQualityHelper.instance.reportQualityMessage(
            ROIQueryErrorParams.CODE_TRACK_ERROR,
            qualityInfo,ROIQueryErrorParams.TRACK_GENERATE_EVENT_ERROR
        )
    }


    /**
     * 事件校验
     */
    private fun assertEvent(
        eventName: String,
        properties: JSONObject? = null
    ) = EventUtils.isValidEventName(eventName) && EventUtils.isValidProperty(properties)

    /**
     * 初始化配置
     */
    private fun initConfig(packageName: String) {
        var configBundle: Bundle? = null
        try {
            mContext?.let {
                val appInfo = it.applicationContext.packageManager
                    .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                configBundle = appInfo.metaData
            }

        } catch (e: PackageManager.NameNotFoundException) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.CODE_INIT_CONFIG_ERROR,e.message,ROIQueryErrorParams.INIT_CONFIG_ERROR,ROIQueryErrorParams.TYPE_ERROR
            )
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
    private fun configLog(
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
                override fun onDisconnected() {
                    PropertyManager.instance.updateNetworkType(NetworkUtil.NetworkType.NETWORK_NO)
                }
                override fun onConnected(networkType: NetworkUtil.NetworkType?) {
                    LogUtils.i("onNetConnChanged", networkType)
                    PropertyManager.instance.updateNetworkType(networkType)
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
        checkFirstOpen()
        trackAppEngagementEvent()
        setLatestUserProperties()
        setActiveUserProperties()
    }

    private fun checkFirstOpen(){
        val isFirstOpen = mDataAdapter?.isFirstOpen
        if (isFirstOpen == true) {
            mDataAdapter?.isFirstOpen = false
            startAppAttribute()
        }
        trackAppOpenEvent(isFirstOpen)
    }

    /**
     * 采集app 启动事件
     */
    private fun trackAppOpenEvent(isFirstOpen :Boolean?) {
        trackNormal(if (isFirstOpen == true) Constant.PRESET_EVENT_APP_FIRST_OPEN else Constant.PRESET_EVENT_APP_OPEN,true)
    }

    /**
     * 采集 app 活跃事件
     */
    private fun trackAppEngagementEvent() {
        TaskScheduler.scheduleTask(object : SchedulerTask(Constant.APP_ENGAGEMENT_INTERVAL_TIME_LONG, false) {
            override fun onSchedule() {
                trackNormal(
                    Constant.PRESET_EVENT_APP_ENGAGEMENT,
                    true
                )
            }
        })
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
               PropertyManager.instance.updateSdkVersionProperty(this)
            }
        trackUser(
            Constant.PRESET_EVENT_USER_SET_ONCE,
            activeUserProperties
        )
    }

    private fun startAppAttribute() {
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
            true,
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
                            cnl
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



}