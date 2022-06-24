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
import org.json.JSONException

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
                userSetOnce(JSONObject().apply {
                    put(Constant.EVENT_INFO_ACID.replace("#",""), value)
                })
            }
        }

    override var rqid: String?
        get() = EventDateAdapter.getInstance()?.rqid
        set(value) {}

    override var fiid: String?
        get() = EventDateAdapter.getInstance()?.fiid
        set(value) {
            if (value != null) {
                EventDateAdapter.getInstance()?.fiid = value
                updateCommonProperties(Constant.COMMON_PROPERTY_FIREBASE_IID, value)
                userSetOnce(JSONObject().apply {
                    put(Constant.COMMON_PROPERTY_FIREBASE_IID.replace("#",""), value)
                })
            }
        }

    override var fcmToken: String?
        get() = EventDateAdapter.getInstance()?.fcmToken
        set(value) {
            if (value != null) {
                EventDateAdapter.getInstance()?.fcmToken = value
                updateCommonProperties(Constant.COMMON_PROPERTY_FCM_TOKEN, value)
                userSetOnce(JSONObject().apply {
                    put(Constant.COMMON_PROPERTY_FCM_TOKEN.replace("#",""), value)
                })
            }
        }
    override var afid: String?
        get() = EventDateAdapter.getInstance()?.afid
        set(value) {
            if (value != null) {
                EventDateAdapter.getInstance()?.afid = value
                updateCommonProperties(Constant.COMMON_PROPERTY_APPSFLYER_ID, value)
                userSetOnce(JSONObject().apply {
                    put(Constant.COMMON_PROPERTY_APPSFLYER_ID.replace("#",""), value)
                })
            }
        }
    override var koid: String?
        get() = EventDateAdapter.getInstance()?.koid
        set(value) {
            if (value != null) {
                EventDateAdapter.getInstance()?.koid = value
                updateCommonProperties(Constant.COMMON_PROPERTY_KOCHAVA_ID, value)
                userSetOnce(JSONObject().apply {
                    put(Constant.COMMON_PROPERTY_KOCHAVA_ID.replace("#",""), value)
                })
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

    private fun trackInternal(eventName: String?, eventType: String, properties: Map<String, Any?>?) {
        try {
            trackInternal(eventName, eventType,JSONObject(properties ?: mutableMapOf<String,Any>()))
        } catch (e: Exception) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.TRACK_PROPERTIES_KEY_NULL,
                "event name: $eventName" + e.stackTraceToString()
            );
            return
        }

    }

     private fun trackInternal(eventName: String?, eventType: String, properties: JSONObject?) {
         if (!ROIQueryAnalytics.isSDKEnable()) {
             ROIQueryQualityHelper.instance.reportQualityMessage(
                 ROIQueryErrorParams.SDK_INIT_ERROR,
                 "SDK is unable, event name: $eventName "
             )
             return
         }

         mTrackTaskManager?.let {
             try {
                 it.execute {
                     trackEvent(eventName,eventType, properties)
                 }
             } catch (e: Exception) {
                 LogUtils.printStackTrace(e)
                 ROIQueryQualityHelper.instance.reportQualityMessage(
                     ROIQueryErrorParams.TRACK_TASK_MANAGER_ERROR,
                     "event name: $eventName "
                 );
             }
         }
    }


    override fun trackUser(eventType: String,properties: JSONObject?) {
        trackInternal(eventType, eventType, properties)
    }

   override fun trackNormal(eventName: String?, properties: JSONObject?){
       trackInternal(eventName, Constant.EVENT_TYPE_TRACK, properties)
    }

    fun trackNormal(eventName: String?, properties: Map<String, Any?>?){
        trackInternal(eventName, Constant.EVENT_TYPE_TRACK,properties)
    }

    fun trackAppClose(properties: Map<String, Any?>?) {
        trackNormal(Constant.PRESET_EVENT_APP_CLOSE, properties)
    }

    override fun trackAppClose(properties: JSONObject?) {
        trackNormal(Constant.PRESET_EVENT_APP_CLOSE, properties)
    }

    fun trackPageOpen(properties: Map<String, Any?>?) {
        trackNormal(Constant.PRESET_EVENT_PAGE_OPEN,  properties)
    }

    override fun trackPageOpen(properties: JSONObject?) {
        trackNormal(Constant.PRESET_EVENT_PAGE_OPEN, properties)
    }

    fun trackPageClose(properties: Map<String, Any?>?) {
        trackNormal(Constant.PRESET_EVENT_PAGE_CLOSE, properties)
    }

    override fun trackPageClose(properties: JSONObject?) {
        trackNormal(Constant.PRESET_EVENT_PAGE_CLOSE, properties)
    }

    fun userSet(properties: JSONObject?){
        trackUser(Constant.EVENT_TYPE_USER_SET, properties)
    }

    fun userSetOnce(properties: JSONObject?){
        trackUser(Constant.EVENT_TYPE_USER_SET_ONCE, properties)
    }

    fun userAdd(properties: JSONObject?){
        trackUser(Constant.EVENT_TYPE_USER_ADD, properties)
    }

    fun trackAppStateChanged(){
        trackNormal(Constant.PRESET_EVENT_APP_STATE_CHANGED)
    }

    fun userUnset(vararg properties: String?){
        val props = JSONObject()
        for (s in properties) {
            try {
                props.put(s, 0)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        if (props.length() > 0) {
            trackUser(Constant.EVENT_TYPE_USER_UNSET, props)
        }
    }

    fun userDelete(){
        trackUser(Constant.EVENT_TYPE_USER_DEL, JSONObject())
    }

    fun userAppend(properties: JSONObject?){
        trackUser(Constant.EVENT_TYPE_USER_APPEND, properties)
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