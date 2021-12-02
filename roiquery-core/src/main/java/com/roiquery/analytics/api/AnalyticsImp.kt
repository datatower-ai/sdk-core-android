package com.roiquery.analytics.api

import android.annotation.SuppressLint
import android.content.Context
import com.roiquery.analytics.Constant
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.utils.LogUtils
import com.roiquery.quality.ROIQueryErrorParams
import com.roiquery.quality.ROIQueryQualityHelper

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
                updateEventInfo(Constant.EVENT_INFO_ACID, value)
            }
        }

    override var fiid: String?
        get() = EventDateAdapter.getInstance()?.fiid
        set(value) {
            if (value != null) {
                EventDateAdapter.getInstance()?.fiid = value
                updateCommonProperties(Constant.COMMON_PROPERTY_FIREBASE_IID, value)
            }
        }
    override var afid: String?
        get() = EventDateAdapter.getInstance()?.afid
        set(value) {
            if (value != null) {
                EventDateAdapter.getInstance()?.afid = value
                updateCommonProperties(Constant.COMMON_PROPERTY_APPSFLYER_ID, value)
            }
        }
    override var koid: String?
        get() = EventDateAdapter.getInstance()?.koid
        set(value) {
            if (value != null) {
                EventDateAdapter.getInstance()?.koid = value
                updateCommonProperties(Constant.COMMON_PROPERTY_KOCHAVA_ID, value)
            }
        }

    override var enableSDK: Boolean?
        get() = enableTrack == true && enableUpload == true
        set(value) {
            if (value == false && enableSDK == true) {
                LogUtils.e("Analytics SDK is disable")
                enableTrack = false
                enableUpload = false
                configLog(false)
            }
            if (value == true && enableSDK == false) {
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
//                mTrackTaskManager!!.setDataTrackEnable(it)
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
            val jsonObject = try {
                JSONObject(properties ?: mutableMapOf<String, Any>())
            } catch (e: Exception) {
                ROIQueryQualityHelper.instance.reportQualityMessage(
                    ROIQueryErrorParams.TRACK_PROPERTIES_KEY_NULL,
                    "event name: $eventName" + e.stackTraceToString()
                );
                return
            }
            track(eventName, jsonObject)
        } catch (e: Exception) {
        }

    }

    override fun track(eventName: String?, properties: JSONObject?) {
        if (!ROIQueryAnalytics.isSDKEnable()) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.SDK_INIT_ERROR,
                "SDK is unable, event name: $eventName "
            );
            return
        }
        mTrackTaskManager?.let {
            try {
                it.execute {
                    trackEvent(eventName, properties)
                }
            } catch (e: Exception) {
                LogUtils.printStackTrace(e)
                ROIQueryQualityHelper.instance.reportQualityMessage(
                    ROIQueryErrorParams.TRACK_TASK_MANAGER_ERROR,
                    "SDK is unable, event name: $eventName "
                );
            }

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
        setUserProperties(JSONObject(properties))
    }

    override fun setUserProperties(properties: JSONObject?) {
        if (properties != null) {
            val superPropertiesIterator: Iterator<String> = properties.keys()
            while (superPropertiesIterator.hasNext()) {
                val key = superPropertiesIterator.next()
                val value: Any = properties.get(key)
                val p = JSONObject().apply {
                    put(Constant.USER_PROPERTIES_PROPERTY_KEY, key)
                    put(Constant.USER_PROPERTIES_PROPERTY_VALUE, value)
                }
                track(Constant.PRESET_EVENT_USER_PROPERTIES, p)
            }
        } else {
            track(Constant.PRESET_EVENT_USER_PROPERTIES, JSONObject())
        }
    }


    override fun flush() {
        mAnalyticsManager?.flush()
    }


    override fun deleteAll() {
        mAnalyticsManager?.deleteAll()
    }


    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: AnalyticsImp? = null
        internal fun getInstance(context: Context?): AnalyticsImp {
            if (context == null || mConfigOptions == null) {
                throw IllegalStateException("call ROIQuerySDK.init() first")
            }
            return instance ?: synchronized(this) {
                instance ?: AnalyticsImp(context).also { instance = it }
            }

        }

        internal fun init(
            context: Context?,
            configOptions: AnalyticsConfig?
        ) {
            if (context == null || configOptions == null) {
                throw IllegalStateException("call ROIQuerySDK.init() first")
            }
            if (instance == null) {
                mConfigOptions = configOptions
                ROIQueryAnalytics.mContext = context.applicationContext
                instance = getInstance(ROIQueryAnalytics.mContext)
            }
        }

    }
}