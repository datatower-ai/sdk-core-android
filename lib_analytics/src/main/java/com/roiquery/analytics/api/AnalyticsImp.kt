package com.roiquery.analytics.api

import android.content.Context
import com.roiquery.analytics.Constant
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.utils.LogUtils

import org.json.JSONObject

class AnalyticsImp : AbstractAnalytics {

    internal constructor(
        context: Context?
    ) : super(context)


    override var maxCacheSize: Long?
        get() = mConfigOptions?.mMaxCacheSize
        set(value) {
            value?.let { mConfigOptions?.setMaxCacheSize(it) }
        }

    override var flushInterval: Int?
        get() = mConfigOptions?.mFlushInterval
        set(value) {
            value?.let { mConfigOptions?.setFlushInterval(it) }
        }

    override var flushBulkSize: Int?
        get() = mConfigOptions?.mFlushBulkSize
        set(value) {
            value?.let { mConfigOptions?.setFlushBulkSize(it) }
        }

    override var accountId: String?
        get() = EventDateAdapter.getInstance()?.accountId
        set(value) {
            if (value != null) {
                EventDateAdapter.getInstance()?.accountId = value
                updateEventInfo("#acid", value)
            }
        }
    override var enableSDK: Boolean?
        get() = enableTrack == true && enableUpload == true
        set(value) {
            if(value == false && enableSDK == true){
                LogUtils.e("Analytics SDK is disable")
                enableTrack = false
                enableUpload = false
                configLog(false)
            }
            if(value == true && enableSDK == false){
                enableTrack = true
                enableUpload = true
                configLog()
            }
        }


    override var enableTrack: Boolean?
        get() = EventDateAdapter.getInstance()?.enableTrack == true
        set(value) {
            value?.let {
                EventDateAdapter.getInstance()?.enableTrack = it
                mTrackTaskManager!!.setDataTrackEnable(it)
                mConfigOptions?.mEnableTrack = it
            }

        }


    override var enableUpload: Boolean?
        get() = EventDateAdapter.getInstance()?.enableUpload == true
        set(value) {
            value?.let {
                EventDateAdapter.getInstance()?.enableUpload = it
                mConfigOptions?.mEnableUpload = it
            }

        }

    override var flushNetworkPolicy: Int?
        get() = mConfigOptions?.mNetworkTypePolicy
        set(value) {
            value?.let {
                mConfigOptions?.setNetworkTypePolicy(it)
            }

        }

    fun track(eventName: String?, properties: Map<String, Any?>?) {
        try {
            if (!ROIQueryAnalytics.isSDKEnable()) return

            track(eventName, JSONObject(properties))
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
    }

    override fun track(eventName: String?, properties: JSONObject?) {
        if (!ROIQueryAnalytics.isSDKEnable()) return

        mTrackTaskManager?.let {
            it.addTrackEventTask(Runnable {
                try {
                    if (eventName != null) {
                        trackEvent(eventName, properties)
                    }
                } catch (e: Exception) {
                    LogUtils.printStackTrace(e)
                }
            })
        }
    }


    fun trackAppClose(properties: Map<String, Any?>?) {
        track(Constant.PRESET_EVENT_APP_CLOSE, properties)
    }

    override fun trackAppClose(properties: JSONObject?) {
        track(Constant.PRESET_EVENT_APP_CLOSE, properties)
    }

    fun trackPageOpen(properties: Map<String, Any?>?) {
        track(Constant.PRESET_EVENT_PAGE_OPEN, properties)
    }

    override fun trackPageOpen(properties: JSONObject?) {
        track(Constant.PRESET_EVENT_PAGE_OPEN, properties)
    }

    fun trackPageClose(properties: Map<String, Any?>?) {
        track(Constant.PRESET_EVENT_PAGE_CLOSE, properties)
    }

    override fun trackPageClose(properties: JSONObject?) {
        track(Constant.PRESET_EVENT_PAGE_CLOSE, properties)
    }

    fun setUserProperties(properties: Map<String, Any?>?) {
        track(Constant.PRESET_EVENT_USER_PROPERTIES, properties)
    }

    override fun setUserProperties(properties: JSONObject?) {
        track(Constant.PRESET_EVENT_USER_PROPERTIES, properties)
    }


    override fun flush() {
        mAnalyticsManager?.flush()
    }


    override fun deleteAll() {
        mAnalyticsManager?.deleteAll()
    }


    companion object {

        @Volatile
        private var instance: AnalyticsImp? = null
        internal fun getInstance(context: Context?): AnalyticsImp? {
            return try {
                if (context == null || mConfigOptions == null) {
                    throw IllegalStateException("call ROIQuerySDK.init() first")
                }
                instance ?: synchronized(this) {
                    instance ?: AnalyticsImp(context).also { instance = it }
                }
            } catch (e: Exception) {
                LogUtils.printStackTrace(e)
                null
            }

        }

        internal fun init(
            context: Context?,
            configOptions: AnalyticsConfig?
        ) {
            try {
                if (context == null || configOptions == null) {
                    throw IllegalStateException("call ROIQuerySDK.init() first")
                }
                if (instance == null) {
                    mConfigOptions = configOptions
                    ROIQueryAnalytics.mContext = context.applicationContext
                    instance = getInstance(ROIQueryAnalytics.mContext)
                }
            }catch (e:Exception){
                LogUtils.printStackTrace(e)
            }

        }

    }
}