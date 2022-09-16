package com.roiquery.analytics.api

import android.annotation.SuppressLint
import android.content.Context
import com.roiquery.analytics.Constant
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.network.HttpCallback
import com.roiquery.analytics.network.HttpMethod
import com.roiquery.analytics.network.RequestHelper
import com.roiquery.analytics.utils.EventUtils
import com.roiquery.analytics.utils.LogUtils
import com.roiquery.quality.ROIQueryErrorParams
import com.roiquery.quality.ROIQueryQualityHelper
import org.json.JSONException

import org.json.JSONObject

class AnalyticsImp internal constructor(context: Context?) : AbstractAnalytics(context) {


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

    override var rqid: String?
        get() = EventDateAdapter.getInstance()?.rqid
        set(value) {}

    override var fiid: String?
        get() = EventDateAdapter.getInstance()?.fiid
        set(value) {
            if (value != null) {
                EventDateAdapter.getInstance()?.fiid = value
                updateCommonProperties(Constant.COMMON_PROPERTY_FIREBASE_IID, value)
                userSet(JSONObject().apply {
                    put(Constant.USER_PROPERTY_LATEST_FIREBASE_IID, value)
                })
            }
        }

    override var fcmToken: String?
        get() = EventDateAdapter.getInstance()?.fcmToken
        set(value) {
            if (value != null) {
                EventDateAdapter.getInstance()?.fcmToken = value
                updateCommonProperties(Constant.COMMON_PROPERTY_FCM_TOKEN, value)
                userSet(JSONObject().apply {
                    put(Constant.USER_PROPERTY_LATEST_FCM_TOKEN, value)
                })
            }
        }
    override var afid: String?
        get() = EventDateAdapter.getInstance()?.afid
        set(value) {
            if (value != null) {
                EventDateAdapter.getInstance()?.afid = value
                updateCommonProperties(Constant.COMMON_PROPERTY_APPSFLYER_ID, value)
                userSet(JSONObject().apply {
                    put(Constant.USER_PROPERTY_LATEST_APPSFLYER_ID, value)
                })
            }
        }
    override var koid: String?
        get() = EventDateAdapter.getInstance()?.koid
        set(value) {
            if (value != null) {
                EventDateAdapter.getInstance()?.koid = value
                updateCommonProperties(Constant.COMMON_PROPERTY_KOCHAVA_ID, value)
                userSet(JSONObject().apply {
                    put(Constant.USER_PROPERTY_LATEST_KOCHAVA_ID, value)
                })
            }
        }

    override var appSetId: String?
        get() = EventDateAdapter.getInstance()?.appSetId
        set(value) {
            if (value != null) {
                EventDateAdapter.getInstance()?.appSetId = value
                updateCommonProperties(Constant.COMMON_PROPERTY_APP_SET_ID, value)
                userSet(JSONObject().apply {
                    put(Constant.USER_PROPERTY_LATEST_APP_SET_ID, value)
                })
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

    private fun trackInternal(eventName: String?, eventType: String, isPreset: Boolean,properties: Map<String, Any?>?) {
        try {
            trackInternal(eventName, eventType, isPreset, JSONObject(properties ?: mutableMapOf<String,Any>()))
        } catch (e: Exception) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.TRACK_PROPERTIES_KEY_NULL,
                "event name: $eventName" + e.stackTraceToString()
            )
            return
        }
    }

    private fun trackInternal(eventName: String?, eventType: String, isPreset: Boolean, properties: JSONObject?) {
         mTrackTaskManager?.let {
             try {
                 it.execute {
                     trackEvent(eventName, eventType, isPreset, properties)
                 }
             } catch (e: Exception) {
                 LogUtils.printStackTrace(e)
                 ROIQueryQualityHelper.instance.reportQualityMessage(
                     ROIQueryErrorParams.TRACK_TASK_MANAGER_ERROR,
                     "event name: $eventName "
                 )
             }
         } }

    fun getServerTimeAsync(serverTimeListener: ServerTimeListener?){
        RequestHelper.Builder(
            HttpMethod.POST_ASYNC,
            Constant.EVENT_REPORT_URL
        )
            .jsonData("[{}]")
            .retryCount(Constant.EVENT_REPORT_TRY_COUNT)
            .callback(object : HttpCallback.TimeCallback() {
                override fun onFailure(code: Int, errorMessage: String?) {
                    serverTimeListener?.onFinished(0L,errorMessage ?: "onFailure")
                }

                override fun onResponse(response: Long) {
                    serverTimeListener?.onFinished(response,"ok")
                }
            })
            .execute()
    }

    fun getServerTimeSync():Long {
        RequestHelper.Builder(
            HttpMethod.POST_SYNC,
            Constant.EVENT_REPORT_URL
        )
            .jsonData("[{}]")
            .retryCount(Constant.EVENT_REPORT_TRY_COUNT)
            .executeSync()?.let {
                return it.date
            }
        return 0L
    }

    override fun trackUser(eventName: String, properties: JSONObject?) {
        trackInternal(eventName, Constant.EVENT_TYPE_USER,true, properties)
    }

   override fun trackNormal(eventName: String?, isPreset: Boolean, properties: JSONObject?){
       trackInternal(eventName, Constant.EVENT_TYPE_TRACK, isPreset, properties)
    }

    fun trackNormal(eventName: String?, isPreset: Boolean, properties: Map<String, Any?>?){
        trackNormal(eventName, isPreset, JSONObject(properties ?: mutableMapOf<String,Any>()))
    }

    private fun trackNormalInternal(eventName: String?, properties: Map<String, Any?>?){
        trackNormalInternal(eventName, JSONObject(properties ?: mutableMapOf<String,Any>()))
    }

    /**
     * 用于 track 预置事件，会对传入的属性进行校验（事件名不需要）
     *
     * */
    private fun trackNormalInternal(eventName: String?, properties: JSONObject? = JSONObject()){
        if (!EventUtils.isValidProperty(properties)) return
        trackNormal(eventName,true, properties)
    }


    fun userSet(properties: JSONObject?){
        trackUser(Constant.PRESET_EVENT_USER_SET, properties)
    }

    fun userSetOnce(properties: JSONObject?){
        trackUser(Constant.PRESET_EVENT_USER_SET_ONCE, properties)
    }

    fun userAdd(properties: JSONObject?){
        trackUser(Constant.PRESET_EVENT_USER_ADD, properties)
    }

    fun trackAppStateChanged(){
        trackNormalInternal(Constant.PRESET_EVENT_APP_STATE_CHANGED)
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
            trackUser(Constant.PRESET_EVENT_USER_UNSET, props)
        }
    }

    fun userDelete(){
        trackUser(Constant.PRESET_EVENT_USER_DEL, JSONObject())
    }

    fun userAppend(properties: JSONObject?){
        trackUser(Constant.PRESET_EVENT_USER_APPEND, properties)
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