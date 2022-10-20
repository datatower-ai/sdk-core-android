package com.roiquery.analytics.api

import android.content.Context
import com.roiquery.analytics.Constant
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.core.EventTrackManager
import com.roiquery.analytics.core.EventUploadManager
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.quality.ROIQueryErrorParams
import com.roiquery.quality.ROIQueryQualityHelper
import org.json.JSONException

import org.json.JSONObject

class AnalyticsImp internal constructor() : AbstractAnalytics() {


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
                PropertyManager.instance.updateACID(value)
            }
        }

    override var rqid: String?
        get() = EventDateAdapter.getInstance()?.rqid
        set(value) {

        }

    override var fiid: String?
        get() = EventDateAdapter.getInstance()?.fiid
        set(value) {
            if (value != null) {
                EventDateAdapter.getInstance()?.fiid = value
                PropertyManager.instance.updateFireBaseInstanceId(value)
            }
        }

    override var fcmToken: String?
        get() = EventDateAdapter.getInstance()?.fcmToken
        set(value) {
            if (value != null) {
                EventDateAdapter.getInstance()?.fcmToken = value
                PropertyManager.instance.updateFCMToken(value)
            }
        }
    override var afid: String?
        get() = EventDateAdapter.getInstance()?.afid
        set(value) {
            if (value != null) {
                EventDateAdapter.getInstance()?.afid = value
                PropertyManager.instance.updateAFID(value)
            }
        }
    override var koid: String?
        get() = EventDateAdapter.getInstance()?.koid
        set(value) {
            if (value != null) {
                EventDateAdapter.getInstance()?.koid = value
                PropertyManager.instance.updateKOID(value)
            }
        }

    override var appSetId: String?
        get() = EventDateAdapter.getInstance()?.appSetId
        set(value) {
            if (value != null) {
                EventDateAdapter.getInstance()?.appSetId = value
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

    override fun trackUser(eventName: String, properties: JSONObject?) {
        EventTrackManager.instance.trackUserWithPropertyCheck(eventName, properties)
    }

    override fun trackNormal(eventName: String?, isPreset: Boolean, properties: JSONObject?) {
        if (isPreset) EventTrackManager.instance.trackNormalPreset(eventName, properties)
        else EventTrackManager.instance.trackNormal(eventName, properties)
    }

    fun trackNormal(eventName: String?, isPreset: Boolean, properties: Map<String, Any>?) {
        try {
            trackNormal(eventName, isPreset, JSONObject(properties?.toMutableMap() ?: mutableMapOf<String, Any?>()))
        } catch (e: Exception) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.CODE_TRACK_ERROR,"event name: $eventName, properties map to json error" + e.stackTraceToString()
            )
            return
        }
    }

    fun userSet(properties: JSONObject?) {
        trackUser(Constant.PRESET_EVENT_USER_SET, properties)
    }

    fun userSetOnce(properties: JSONObject?) {
        trackUser(Constant.PRESET_EVENT_USER_SET_ONCE, properties)
    }

    fun userAdd(properties: JSONObject?) {
        trackUser(Constant.PRESET_EVENT_USER_ADD, properties)
    }

    fun userUnset(vararg properties: String?) {
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

    fun userDelete() {
        trackUser(Constant.PRESET_EVENT_USER_DEL, JSONObject())
    }

    fun userAppend(properties: JSONObject?) {
        trackUser(Constant.PRESET_EVENT_USER_APPEND, properties)
    }


    override fun flush() {
        EventUploadManager.getInstance()?.flush()
    }


    override fun deleteAll() {

    }

    companion object {

        @Volatile
        private var instance: AnalyticsImp? = null

        internal fun getInstance(): AnalyticsImp {
            if (mConfigOptions == null) {
                throw IllegalStateException("call ROIQuerySDK.init() first")
            }
            return instance ?: synchronized(this) {
                instance ?: AnalyticsImp().also { instance = it }
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
                instance = getInstance().apply {
                    internalInit(context)
                }
            }
        }

    }
}