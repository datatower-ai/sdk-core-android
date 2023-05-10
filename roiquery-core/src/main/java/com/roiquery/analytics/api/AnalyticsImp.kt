package com.roiquery.analytics.api

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.roiquery.analytics.Constant
import com.roiquery.analytics.OnDataTowerIdListener
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.core.EventTimer
import com.roiquery.analytics.core.EventTimerManager
import com.roiquery.analytics.core.EventTrackManager
import com.roiquery.analytics.core.PropertyManager
import com.roiquery.analytics.data.EventDataAdapter
import com.roiquery.analytics.taskqueue.MainQueue
import com.roiquery.analytics.utils.EventUtils
import com.roiquery.analytics.utils.LogUtils
import com.roiquery.quality.PerfAction
import com.roiquery.quality.PerfLogger
import com.roiquery.quality.ROIQueryErrorParams
import com.roiquery.quality.ROIQueryQualityHelper
import org.json.JSONException
import org.json.JSONObject

class AnalyticsImp internal constructor() : AbstractAnalytics() {

    override var accountId: String?
        get() = PropertyManager.instance.getACID()
        set(value) {
            if (value != null) {
                PropertyManager.instance.updateACID(value)
            }
        }

    override fun getDTId(onDataTowerIDListener: OnDataTowerIdListener) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        PropertyManager.instance.getDataTowerId(onDataTowerIDListener)
    }

    override fun setFirebaseInstanceId(id: String?) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        PropertyManager.instance.updateFireBaseInstanceId(id)
    }

    override fun setAppsFlyersId(id: String?) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        PropertyManager.instance.updateAFID(id)
    }

    override fun setKochavaId(id: String?) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        PropertyManager.instance.updateKOID(id)
    }

    override fun setAdjustId(id: String?) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        PropertyManager.instance.updateAdjustId(id)
    }

    override var enableUpload: Boolean?
        get() = EventDataAdapter.getInstance()?.isUploadEnabled == true
        set(value) {
            EventDataAdapter.getInstance()?.isUploadEnabled
            value?.let {
                configOptions?.mEnableUpload = it
            }
        }


    override fun trackUser(eventName: String, properties: JSONObject?) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        val happenTime = SystemClock.elapsedRealtime()
        EventTrackManager.instance.trackUserWithPropertyCheck(eventName, happenTime, properties)
    }

    override fun trackNormal(eventName: String?, isPreset: Boolean, properties: JSONObject?) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        val happenTime = SystemClock.elapsedRealtime()

        if (isPreset) EventTrackManager.instance.trackNormalPreset(eventName, happenTime, properties)
        else EventTrackManager.instance.trackNormal(eventName, happenTime, properties)
    }

    override fun trackNormal(eventName: String?, isPreset: Boolean, properties: Map<String, Any>?) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
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

    override fun userSet(properties: JSONObject?) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        trackUser(Constant.PRESET_EVENT_USER_SET, properties)
    }

    override fun userSetOnce(properties: JSONObject?) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        trackUser(Constant.PRESET_EVENT_USER_SET_ONCE, properties)
    }

    override fun userAdd(properties: JSONObject?) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        trackUser(Constant.PRESET_EVENT_USER_ADD, properties)
    }

    override fun userUnset(vararg properties: String?) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
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

    override fun userDelete() {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        trackUser(Constant.PRESET_EVENT_USER_DEL, JSONObject())
    }

    override fun userAppend(properties: JSONObject?) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        trackUser(Constant.PRESET_EVENT_USER_APPEND, properties)
    }

    override fun userUniqAppend(properties: JSONObject?) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        trackUser(Constant.PRESET_EVENT_USER_UNIQ_APPEND, properties)
    }

    override fun trackTimerStart(eventName: String) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        val startTime = SystemClock.elapsedRealtime()
        MainQueue.get().postTask {
            try {
                if (!EventUtils.isValidEventName(eventName)) return@postTask
                EventTimerManager.instance.addEventTimer(eventName, EventTimer(startTime))
            } catch (e: Exception) {
                LogUtils.e(e)
            }
        }
    }

    override fun trackTimerPause(eventName: String) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        val startTime = SystemClock.elapsedRealtime()
        MainQueue.get().postTask {
            try {
                if (!EventUtils.isValidEventName(eventName)) return@postTask
                EventTimerManager.instance.updateTimerState(eventName, startTime, true)
            } catch (e: Exception) {
                LogUtils.e(e)
            }
        }
    }

    override fun trackTimerResume(eventName: String) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        val startTime = SystemClock.elapsedRealtime()
        MainQueue.get().postTask {
            try {
                if (!EventUtils.isValidEventName(eventName)) return@postTask
                EventTimerManager.instance.updateTimerState(eventName, startTime, false)
            } catch (e: Exception) {
                LogUtils.e(e)
            }
        }
    }

    override fun trackTimerEnd(eventName: String, properties: JSONObject) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        val endTime = SystemClock.elapsedRealtime()
        MainQueue.get().postTask {
            try {
                if (!EventUtils.isValidEventName(eventName)) return@postTask
                EventTimerManager.instance.updateEndTime(eventName, endTime)
                trackNormal(eventName,false, properties)
            } catch (e: Exception) {
                LogUtils.e(e)
            }
        }
    }

    override fun removeTimer(eventName: String) {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        MainQueue.get().postTask {
            try {
                if (!EventUtils.isValidEventName(eventName)) return@postTask
                EventTimerManager.instance.removeTimer(eventName)
            } catch (e: java.lang.Exception) {
                LogUtils.e(e)
            }
        }
    }

    override fun clearTrackTimer() {
        if (configOptions?.isSdkDisable() == true) {
            return
        }
        MainQueue.get().postTask { try {
                EventTimerManager.instance.clearTimers()
            } catch (e: java.lang.Exception) {
                LogUtils.e(e)
            }
        }
    }

    companion object {

        @Volatile
        private var instance: AnalyticsImp? = null

        internal fun getInstance(): AbstractAnalytics {
            if (!mHasInit.get()) {
                Log.e(Constant.LOG_TAG,"Call DT.init() first")
                return AnalyticsEmptyImp()
            }
            if (AnalyticsConfig.instance.isSdkDisable()) {
//                Log.e(Constant.LOG_TAG,"sdk is disable")
                return AnalyticsEmptyImp()
            }
            return instance ?: synchronized(this) {
                instance ?: AnalyticsImp().also { instance = it }
            }
        }

        internal fun init(
            context: Context?,
            configOptions: AnalyticsConfig?,
        ) {
            if (context == null || configOptions == null) {
                throw IllegalStateException("Context and configOptions can not be null")
            }

            PerfLogger.doPerfLog(PerfAction.SDKINITBEGIN, System.currentTimeMillis())
            MainQueue.get().postTask {
                if (instance == null) {
                    instance = AnalyticsImp()
                }

                instance?.init(context)
                PerfLogger.doPerfLog(PerfAction.SDKINITEND, System.currentTimeMillis())
            }
        }

    }
}
