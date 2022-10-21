package com.roiquery.analytics.api

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import com.roiquery.analytics.Constant
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.core.EventTrackManager
import com.roiquery.analytics.core.EventUploadManager
import com.roiquery.analytics.core.PresetEventManager
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.utils.*
import com.roiquery.quality.ROIQueryErrorParams
import com.roiquery.quality.ROIQueryQualityHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext


abstract class AbstractAnalytics : IAnalytics, CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()

    //本地数据适配器，包括sp、db的操作
    private var mDataAdapter: EventDateAdapter? = null

    private val hasInit = AtomicBoolean(false)

    private val isInitRunning = AtomicBoolean(false)

    companion object {
        const val TAG = "AnalyticsApi"
        // 配置
        internal var mConfigOptions: AnalyticsConfig? = null
    }

    fun init(context: Context){
        if(isInitRunning.get() || hasInit.get()){
            return
        }
        isInitRunning.set(true)
        //real init
        internalInit(context)
    }


    fun internalInit(context: Context){
        try {
            initConfig(context)
            initLocalData(context)
            initTracker()
            registerAppLifecycleListener(context)
            initProperties(context, dataTowerIdHandler = {
                registerNetworkStatusChangedListener(context)
                trackPresetEvent(context)
                onInitSuccess()
            })
        } catch (e: Exception) {
            onInitFailed(e.message)
        }
    }

    /**
     * 初始化本成功
     */
    private fun onInitSuccess(){
        hasInit.set(true)
        isInitRunning.set(false)
        EventTrackManager.instance.trackNormalPreset(Constant.PRESET_EVENT_APP_INITIALIZE)
        LogUtils.d("ROIQuery", "init succeed")
    }

    /**
     * 初始化失败
     */
    private fun onInitFailed(errorMessage: String?){
        isInitRunning.set(false)
        ROIQueryQualityHelper.instance.reportQualityMessage(
            ROIQueryErrorParams.CODE_INIT_EXCEPTION,
            errorMessage,
            ROIQueryErrorParams.INIT_EXCEPTION,
            ROIQueryErrorParams.TYPE_ERROR
        )
    }

    fun isInitSuccess() = hasInit.get()

    /**
     * 初始化本地数据
     */
    private fun initLocalData(context: Context) {
        mDataAdapter = EventDateAdapter.getInstance(context)
        mDataAdapter?.enableUpload = true
    }

    /**
     * 初始化预置、通用属性, 并获取DT id
     */
    private fun initProperties(context: Context, dataTowerIdHandler: (dtid: String) -> Unit) {
        PropertyManager.instance.init(context, mConfigOptions, dataTowerIdHandler)
    }

    /**
     * 监听应用生命周期
     */
    private fun registerAppLifecycleListener(context: Context) {
        if (ProcessUtils.isInMainProcess(context)) {
            (context.applicationContext as Application).registerActivityLifecycleCallbacks(LifecycleObserverImpl())
        }
    }

    /**
     * 初始化数据采集
     */
    private fun initTracker() {
        EventTrackManager.instance.init()
    }

    /**
     * 采集预置事件
     */
    private fun trackPresetEvent(context: Context) {
        PresetEventManager.instance.trackPresetEvent(context)
    }

    /**
     * 初始化SDK传递进来的配置
     */
    private fun initConfig(context: Context) {
        var configBundle: Bundle? = null
        try {
            context.let {
                val appInfo = it.applicationContext.packageManager
                    .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
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

    /**
     * 网络状态监控
     */
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
                    EventUploadManager.getInstance()?.flush()
                }
            })
    }



}