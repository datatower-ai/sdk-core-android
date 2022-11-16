package com.roiquery.analytics.core

import android.app.Application.getProcessName
import android.content.Context
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.google.android.gms.common.util.ProcessUtils
import com.roiquery.analytics.Constant
import com.roiquery.analytics.api.AbstractAnalytics
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.utils.*
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean

class PresetEventManager {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            PresetEventManager()
        }
    }

    private var mDataAdapter: EventDateAdapter? = null

    private val isAppInstallTrackRunning = AtomicBoolean(false)

    /**
     * 采集app 预置事件
     */
    @Synchronized
    fun trackPresetEvent(context: Context) {
        //子进程不采集
        if (!ProcessUtil.isMainProcess(context)) {
            LogUtils.i(
                "trackPresetEvent",
                ProcessUtil.getCurrentProcessName(context) + "is not main process"
            )
            return
        }
        mDataAdapter = EventDateAdapter.getInstance()
        checkAppInstall(context)
        setLatestUserProperties(context)
        setActiveUserProperties(context)
    }

    private fun checkAppInstall(context: Context) {
        if (mDataAdapter?.isAppInstallInserted == false) {
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

        setActiveUserAgent(context)
    }

    //主线程获取ua 可能引发anr
    private fun setActiveUserAgent(context: Context) {
        Thread {
            val ua = DeviceUtils.getUserAgent(context)
            EventTrackManager.instance.trackUser(
                Constant.PRESET_EVENT_USER_SET_ONCE,
                JSONObject().apply {
                    put(
                        Constant.USER_PROPERTY_ACTIVE_USER_AGENT,
                        ua
                    )
                }
            )
        }.start()
    }

    private fun startAppAttribute(context: Context) {
        try {
            getAppAttribute(context)
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
            trackAppInstallEvent(
                ReferrerDetails(null),
                "Exception: " + e.message.toString()
            )
        }
    }

    /**
     * 获取 app 归因属性
     */
    private fun getAppAttribute(context: Context) {
        val referrerClient: InstallReferrerClient? = InstallReferrerClient.newBuilder(context).build()
        referrerClient?.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                try {
                    when (responseCode) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            // Connection established.
                            trackAppInstallEvent(referrerClient.installReferrer, "")
                        }
                        else -> trackAppInstallEvent(
                            ReferrerDetails(null),
                            "responseCode:$responseCode"
                        )

                    }
                    referrerClient.endConnection()
                } catch (e: Exception) {
                    trackAppInstallEvent(
                        ReferrerDetails(null),
                        "responseCode:$responseCode" + ",Exception: " + e.message.toString()
                    )
                }

            }

            override fun onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                try {
                    trackAppInstallEvent(
                        ReferrerDetails(null),
                        "onInstallReferrerServiceDisconnected"
                    )
                    referrerClient.endConnection()
                } catch (e: Exception) {
                    trackAppInstallEvent(
                        ReferrerDetails(null),
                        "onInstallReferrerServiceDisconnected,Exception: " + e.message.toString()
                    )
                }

            }
        })
    }

    /**
     * 采集 app_install 事件
     */
    private fun trackAppInstallEvent(response: ReferrerDetails, failedReason: String) {
        //如果 app_install 事件已插入或者正在插入，则不处理
        if (mDataAdapter?.isAppInstallInserted == true || isAppInstallTrackRunning.get()) {
            return
        }
        isAppInstallTrackRunning.set(true)
        val isOK = failedReason.isBlank()
        EventTrackManager.instance.trackNormalPreset(
            Constant.PRESET_EVENT_APP_INSTALL,
            JSONObject().apply {
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
            },
            insertHandler = { code: Int, _: String ->
                if (code == 0) {
                    EventDateAdapter.getInstance()?.isAppInstallInserted = true
                }
                isAppInstallTrackRunning.set(false)
            }
        )

    }

}