package com.nodetower.analytics.api

import com.nodetower.base.utils.NetworkType
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AnalyticsAPI : AbstractAnalyticsApi() {
    override fun enableLog(enable: Boolean) {
        TODO("Not yet implemented")
    }

    override var maxCacheSize: Long
        get() = TODO("Not yet implemented")
        set(value) {}
    override val isDebugMode: Boolean
        get() = TODO("Not yet implemented")
    override val isNetworkRequestEnable: Boolean
        get() = TODO("Not yet implemented")

    override fun enableNetworkRequest(isRequest: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setFlushNetworkPolicy(networkType: Int) {
        TODO("Not yet implemented")
    }

    override var flushInterval: Int
        get() = TODO("Not yet implemented")
        set(value) {}
    override var flushBulkSize: Int
        get() = TODO("Not yet implemented")
        set(value) {}
    override var sessionIntervalTime: Int
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun getAccountId(): String? {
        TODO("Not yet implemented")
    }

    override fun getAppId(): String? {
        TODO("Not yet implemented")
    }

    override fun trackAppInstall(properties: JSONObject?) {
        TODO("Not yet implemented")
    }

    override fun trackAppInstall() {
        TODO("Not yet implemented")
    }

    override fun track(eventName: String?, properties: JSONObject?) {
        TODO("Not yet implemented")
    }

    override fun track(eventName: String?) {
        TODO("Not yet implemented")
    }

    override fun removeTimer(eventName: String?) {
        TODO("Not yet implemented")
    }

    override fun trackTimerBegin(eventName: String?) {
        TODO("Not yet implemented")
    }

    override fun trackTimerBegin(eventName: String?, timeUnit: TimeUnit?) {
        TODO("Not yet implemented")
    }

    override fun trackTimerEnd(eventName: String?, properties: JSONObject?) {
        TODO("Not yet implemented")
    }

    override fun trackTimerEnd(eventName: String?) {
        TODO("Not yet implemented")
    }

    override fun clearTrackTimer() {
        TODO("Not yet implemented")
    }

    override fun clearReferrerWhenAppEnd() {
        TODO("Not yet implemented")
    }

    override val mainProcessName: String?
        get() = TODO("Not yet implemented")

    override fun flush() {
        TODO("Not yet implemented")
    }

    override fun flushSync() {
        TODO("Not yet implemented")
    }

    override val superProperties: JSONObject?
        get() = TODO("Not yet implemented")

    override fun registerSuperProperties(superProperties: JSONObject?) {
        TODO("Not yet implemented")
    }

    override fun unregisterSuperProperty(superPropertyName: String?) {
        TODO("Not yet implemented")
    }

    override fun clearSuperProperties() {
        TODO("Not yet implemented")
    }

    override fun trackTimerStart(eventName: String?): String? {
        TODO("Not yet implemented")
    }

    override fun trackTimerPause(eventName: String?) {
        TODO("Not yet implemented")
    }

    override fun trackTimerResume(eventName: String?) {
        TODO("Not yet implemented")
    }

    override fun setCookie(cookie: String?, encode: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getCookie(decode: Boolean): String? {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun stopTrackThread() {
        TODO("Not yet implemented")
    }

    override fun startTrackThread() {
        TODO("Not yet implemented")
    }

    override fun enableDataCollect() {
        TODO("Not yet implemented")
    }

    override fun getScreenOrientation(): String? {
        TODO("Not yet implemented")
    }


    fun getServerUrl() = mServerUrl

    fun getFlushNetworkPolicy() = if (mConfigOptions != null ) mConfigOptions!!.mNetworkTypePolicy else NetworkType.TYPE_NONE

    fun isMultiProcessFlushData() = if (mConfigOptions != null) mConfigOptions!!.isSubProcessFlushData else false
}