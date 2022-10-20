package com.roiquery.analytics.core

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.roiquery.analytics.Constant
import com.roiquery.analytics.api.AbstractAnalytics
import com.roiquery.analytics.api.PropertyBuilder
import com.roiquery.analytics.api.PropertyManager
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.utils.EventUtils
import com.roiquery.analytics.utils.LogUtils
import com.roiquery.analytics.utils.ProcessUtils
import org.json.JSONObject

class PresetEventManager {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            PresetEventManager()
        }
    }

    private var mDataAdapter: EventDateAdapter? = null

    /**
     * 采集app 预置事件
     */
    fun trackPresetEvent(context: Context) {
        //子进程不采集
        if (!ProcessUtils.isInMainProcess(context)) {
            LogUtils.i(
                "trackPresetEvent",
                ProcessUtils.getProcessName(context) + "is not main process"
            )
            return
        }
        mDataAdapter = EventDateAdapter.getInstance()
        checkFirstOpen(context)
        setLatestUserProperties(context)
        setActiveUserProperties(context)
    }

    private fun checkFirstOpen(context: Context) {
        val isFirstOpen = mDataAdapter?.isFirstOpen
        if (isFirstOpen == true) {
            mDataAdapter?.isFirstOpen = false
            startAppAttribute(context)
        }
    }

    private fun setLatestUserProperties(context: Context) {
        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET,
            JSONObject(EventUtils.getLatestUserProperties(context, mDataAdapter))
        )
    }

    private fun setActiveUserProperties(context: Context) {
        val activeUserProperties =
            JSONObject(EventUtils.getActiveUserProperties(context, mDataAdapter)).apply {
                PropertyManager.instance.updateSdkVersionProperty(
                    this,
                    Constant.USER_PROPERTY_ACTIVE_SDK_TYPE,
                    Constant.USER_PROPERTY_ACTIVE_SDK_VERSION
                )
            }
        EventTrackManager.instance.trackUser(
            Constant.PRESET_EVENT_USER_SET_ONCE,
            activeUserProperties
        )
    }

    private fun startAppAttribute(context: Context) {
        try {
            getAppAttribute(context)
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
            trackAppAttributeEvent(
                ReferrerDetails(null),
                "Exception: " + e.message.toString()
            )
        }
    }

    /**
     * 获取 app 归因属性
     */
    private fun getAppAttribute(context: Context) {
        val referrerClient: InstallReferrerClient? =
            InstallReferrerClient.newBuilder(context).build()
        referrerClient?.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                try {
                    when (responseCode) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            // Connection established.
                            trackAppAttributeEvent(referrerClient.installReferrer, "")
                        }
                        else -> trackAppAttributeEvent(
                            ReferrerDetails(null),
                            "responseCode:$responseCode"
                        )

                    }
                    referrerClient.endConnection()
                } catch (e: Exception) {
                    trackAppAttributeEvent(
                        ReferrerDetails(null),
                        "responseCode:$responseCode" + ",Exception: " + e.message.toString()
                    )
                }

            }

            override fun onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                try {
                    trackAppAttributeEvent(
                        ReferrerDetails(null),
                        "onInstallReferrerServiceDisconnected"
                    )
                    referrerClient.endConnection()
                } catch (e: Exception) {
                    trackAppAttributeEvent(
                        ReferrerDetails(null),
                        "onInstallReferrerServiceDisconnected,Exception: " + e.message.toString()
                    )
                }

            }
        })
    }

    /**
     * 采集 app 归因属性事件
     */
    private fun trackAppAttributeEvent(response: ReferrerDetails, failedReason: String) {
        val isOK = failedReason.isBlank()
        EventTrackManager.instance.trackNormalPreset(
            Constant.PRESET_EVENT_APP_INSTALL,
            PropertyBuilder.newInstance()
                .append(
                    HashMap<String?, Any>().apply {

                        val cnl = AbstractAnalytics.mConfigOptions?.mChannel ?: ""
                        put(
                            Constant.ATTRIBUTE_PROPERTY_REFERRER_URL,
                            if (isOK) response.installReferrer + "&cnl=$cnl" else "cnl=$cnl"
                        )
                        put(
                            Constant.ATTRIBUTE_PROPERTY_REFERRER_CLICK_TIME,
                            if (isOK) response.referrerClickTimestampSeconds else 0
                        )
                        put(
                            Constant.ATTRIBUTE_PROPERTY_APP_INSTALL_TIME,
                            if (isOK) response.installBeginTimestampSeconds else 0
                        )
                        put(
                            Constant.ATTRIBUTE_PROPERTY_INSTANT_EXPERIENCE_LAUNCHED,
                            if (isOK) response.googlePlayInstantParam else false
                        )
                        put(
                            Constant.ATTRIBUTE_PROPERTY_CNL,
                            cnl
                        )
                        if (!isOK) {
                            put(
                                Constant.ATTRIBUTE_PROPERTY_FAILED_REASON,
                                failedReason
                            )
                        }

                    }
                ).toJSONObject())
    }

}