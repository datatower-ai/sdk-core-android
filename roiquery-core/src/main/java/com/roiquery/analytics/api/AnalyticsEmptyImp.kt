package com.roiquery.analytics.api

import com.roiquery.analytics.OnDataTowerIdListener
import org.json.JSONObject

class AnalyticsEmptyImp internal constructor() : AbstractAnalytics() {

    override var accountId: String?
        get() = ""
        set(value) {

        }

    override fun getDTId(onDataTowerIDListener: OnDataTowerIdListener) {
    }


    override fun setFirebaseInstanceId(id: String?) {
    }

    override fun setAppsFlyersId(id: String?) {
    }

    override fun setKochavaId(id: String?) {
    }

    override fun setAdjustId(id: String?) {
    }

    override var enableUpload: Boolean?
        get() = true
        set(value) {

        }


    override fun trackUser(eventName: String, properties: JSONObject?) {
    }

    override fun trackNormal(eventName: String?, isPreset: Boolean, properties: JSONObject?) {

    }

    override fun trackNormal(eventName: String?, isPreset: Boolean, properties: Map<String, Any>?) {

    }

    override fun userSet(properties: JSONObject?) {
    }

    override fun userSetOnce(properties: JSONObject?) {
    }

    override fun userAdd(properties: JSONObject?) {
    }

    override fun userUnset(vararg properties: String?) {

    }

    override fun userDelete() {
    }

    override fun userAppend(properties: JSONObject?) {

    }

    override fun trackTimerStart(eventName: String) {

    }

    override fun trackTimerPause(eventName: String) {

    }

    override fun trackTimerResume(eventName: String) {

    }

    override fun trackTimerEnd(eventName: String, properties: JSONObject) {

    }

    override fun removeTimer(eventName: String) {

    }

    override fun clearTrackTimer() {

    }


}