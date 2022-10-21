package com.roiquery.analytics.api

import android.content.Context
import com.roiquery.analytics.Constant
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.core.EventTrackManager
import com.roiquery.analytics.core.EventUploadManager
import com.roiquery.analytics.core.PropertyManager
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.quality.ROIQueryErrorParams
import com.roiquery.quality.ROIQueryQualityHelper
import org.json.JSONException

import org.json.JSONObject

class AnalyticsImp internal constructor() : AbstractAnalytics() {

    override var accountId: String?
        get() = EventDateAdapter.getInstance()?.accountId
        set(value) {
            if (value != null) {
                PropertyManager.instance.updateACID(value)
            }
        }

    override fun getDTId(): String? = EventDateAdapter.getInstance()?.dtId

    override fun setFirebaseInstanceId(id: String?) {
        PropertyManager.instance.updateFireBaseInstanceId(id)
    }

    override fun setAppsFlyersId(id: String?) {
        PropertyManager.instance.updateAFID(id)
    }

    override fun setKochavaId(id: String?) {
        PropertyManager.instance.updateKOID(id)
    }

    override var enableUpload: Boolean?
        get() = EventDateAdapter.getInstance()?.enableUpload == true
        set(value) {
            value?.let {
                EventDateAdapter.getInstance()?.enableUpload = it
                mConfigOptions?.mEnableUpload = it
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
            trackNormal(
                eventName,
                isPreset,
                JSONObject(properties?.toMutableMap() ?: mutableMapOf<String, Any?>())
            )
        } catch (e: Exception) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.CODE_TRACK_ERROR,
                "event name: $eventName, properties map to json error" + e.stackTraceToString()
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