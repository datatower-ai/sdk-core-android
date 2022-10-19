package com.roiquery.analytics.api

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.lifecycle.ProcessLifecycleOwner
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.roiquery.analytics.Constant
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.core.AnalyticsManager
import com.roiquery.analytics.core.EventTrackManager
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
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext


abstract class AbstractAnalytics : IAnalytics, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()


    //本地数据适配器，包括sp、db的操作
    private var mDataAdapter: EventDateAdapter? = null


    companion object {
        const val TAG = "AnalyticsApi"

        // 配置
        internal var mConfigOptions: AnalyticsConfig? = null

        // SDK 配置是否初始化
        var mSDKConfigInit = false

    }


    fun internalInit(context: Context){
        try {
            initConfig(context.packageName)
            initLocalData(context)
            initTracker()
            initProperties(context)
            initCloudConfig()
            registerNetworkStatusChangedListener()
            registerAppLifecycleListener(context)
            trackPresetEvent()
            mSDKConfigInit = true
            LogUtils.d("ROIQuery", "init succeed")
        } catch (e: Exception) {
            if (!ROIQueryAnalytics.isSDKInitSuccess()) {
                ROIQueryQualityHelper.instance.reportQualityMessage(
                    ROIQueryErrorParams.CODE_INIT_EXCEPTION,
                    e.message,
                    ROIQueryErrorParams.INIT_EXCEPTION,
                    ROIQueryErrorParams.TYPE_ERROR
                )
            }
            LogUtils.printStackTrace(e)
        }
    }


    /**
     * 初始化本地数据
     */
    private fun initLocalData(context: Context) {
        mDataAdapter = EventDateAdapter.getInstance(context)
        mDataAdapter?.enableUpload = true
    }

    /**
     * 初始化预置、通用属性
     */
    private fun initProperties(context: Context) {
        PropertyManager.instance.init(context, mDataAdapter, mConfigOptions)
    }

    /**
     * 监听应用生命周期
     */
    private fun registerAppLifecycleListener(context: Context) {
        if (ProcessUtils.isInMainProcess(context)) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleObserverImpl())
        }
    }

    /**
     * 初始化数据采集
     */
    private fun initTracker() {
        EventTrackManager.instance.init()
    }


    /**
     * 初始化配置
     */
    private fun initConfig(context: Context,packageName: String) {
        var configBundle: Bundle? = null
        try {
            context.let {
                val appInfo = it.applicationContext.packageManager
                    .getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                configBundle = appInfo.metaData
            }

        } catch (e: PackageManager.NameNotFoundException) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.CODE_INIT_CONFIG_ERROR,
                e.message,
                ROIQueryErrorParams.INIT_CONFIG_ERROR,
                ROIQueryErrorParams.TYPE_ERROR
            )
            LogUtils.printStackTrace(e)
        }
        if (null == configBundle) {
            configBundle = Bundle()
        }

        mConfigOptions?.let { configOptions ->
            configLog(configOptions.mEnabledDebug, configOptions.mLogLevel)
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
    private fun initCloudConfig(context: Context) {
        ROIQueryCloudConfig.init(
            context,
            HttpPOSTResourceRemoteRepository.create(
                Constant.CLOUD_CONFIG_URL//拉取配置地址
                //拉取参数
            ) {
                mutableMapOf<String, String>().apply {
                    put("app_id", mConfigOptions?.mAppId ?: "")
                    put("did", DeviceUtils.getAndroidID(context) ?: "")
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

    private fun registerNetworkStatusChangedListener(context: Context) {
        NetworkUtil.registerNetworkStatusChangedListener(
            context,
            object : NetworkUtil.OnNetworkStatusChangedListener {
                override fun onDisconnected() {
                    PropertyManager.instance.updateNetworkType(NetworkUtil.NetworkType.NETWORK_NO)
                }

                override fun onConnected(networkType: NetworkUtil.NetworkType?) {
                    LogUtils.i("onNetConnChanged", networkType)
                    PropertyManager.instance.updateNetworkType(networkType)
                    AnalyticsManager.getInstance()?.flush()
                }
            })
    }


    /**
     * 采集app 预置事件
     */
    private fun trackPresetEvent() {
        //子进程不采集
        if (!ProcessUtils.isInMainProcess(mContext!!)) {
            LogUtils.i(
                "trackPresetEvent",
                ProcessUtils.getProcessName(mContext!!) + "is not main process"
            )
            return
        }
        checkFirstOpen()
        trackAppEngagementEvent()
        setLatestUserProperties()
        setActiveUserProperties()
    }

    private fun checkFirstOpen() {
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
    private fun trackAppOpenEvent(isFirstOpen: Boolean?) {
        trackNormal(
            if (isFirstOpen == true) Constant.PRESET_EVENT_APP_FIRST_OPEN else Constant.PRESET_EVENT_APP_OPEN,
            true
        )
    }

    /**
     * 采集 app 活跃事件
     */
    private fun trackAppEngagementEvent() {
        TaskScheduler.scheduleTask(object :
            SchedulerTask(Constant.APP_ENGAGEMENT_INTERVAL_TIME_LONG, false) {
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
                PropertyManager.instance.updateSdkVersionProperty(
                    this,
                    Constant.USER_PROPERTY_ACTIVE_SDK_TYPE,
                    Constant.USER_PROPERTY_ACTIVE_SDK_VERSION
                )
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
        val referrerClient: InstallReferrerClient? =
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