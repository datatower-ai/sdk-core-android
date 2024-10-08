package ai.datatower.analytics.core

import ai.datatower.analytics.Constant
import ai.datatower.analytics.config.AnalyticsConfig
import ai.datatower.analytics.data.EventDataAdapter
import ai.datatower.analytics.taskqueue.MainQueue
import ai.datatower.analytics.utils.EventUtils
import ai.datatower.analytics.utils.LogUtils
import ai.datatower.analytics.utils.PresetEvent
import ai.datatower.analytics.utils.PresetPropManager
import ai.datatower.analytics.utils.ProcessUtil
import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import org.json.JSONObject
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.text.StringBuilder

class PresetEventManager {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            PresetEventManager()
        }
    }

    private var mDataAdapter: EventDataAdapter? = null

    private val isAppInstallTrackRunning = AtomicBoolean(false)

    /**
     * 采集app 预置事件
     */
    @Synchronized
    fun trackPresetEvent(context: Context) {
        //子进程不采集
        if (!ProcessUtil.isMainProcess(context)) {
            return
        }
        mDataAdapter = EventDataAdapter.getInstance(context)
        MainQueue.get().postTask {
            checkAppInstall(context)
            setLatestUserProperties(context)
            setActiveUserProperties(context)
        }
    }

    private fun checkAppInstall(context: Context) {
        if (AnalyticsConfig.instance.isSdkDisable()) {
            return
        }

        mDataAdapter?.isAppInstallInserted()?.onSameQueueThen {
            MainQueue.get().postTask {
                if (!it) startAppAttribute(context)
            }
        }
    }

    private fun setLatestUserProperties(context: Context) {
        if (AnalyticsConfig.instance.isSdkDisable()) {
            return
        }
        val happenTime = SystemClock.elapsedRealtime()

        val latestUserProps = JSONObject(EventUtils.getLatestUserProperties(context))

        val eda = EventDataAdapter.getInstance(context)
        eda?.getLastUserSetProps()?.onSameQueueThen { lastUserSetPropsStr ->
            if (lastUserSetPropsStr.isEmpty()) {
                eda.setLastUserSetProps(latestUserProps.toString())

                if (latestUserProps.length() > 0) {
                    EventTrackManager.instance.trackUser(
                        Constant.PRESET_EVENT_USER_SET,
                        happenTime,
                        latestUserProps
                    )
                }
            } else {
                val savedUserSetProps = JSONObject(lastUserSetPropsStr)
                val propsToTrack = JSONObject()
                latestUserProps.keys().forEach {
                    val current = latestUserProps.get(it)
                    if (!savedUserSetProps.has(it) || current != savedUserSetProps.get(it)) {
                        // 与之前不同，或之前没有
                        savedUserSetProps.put(it, current)
                        propsToTrack.put(it, current)
                    }
                }
                eda.setLastUserSetProps(savedUserSetProps.toString())

                if (propsToTrack.length() > 0) {
                    EventTrackManager.instance.trackUser(
                        Constant.PRESET_EVENT_USER_SET,
                        happenTime,
                        propsToTrack
                    )
                }
            }
        } ?: run {
            eda?.setLastUserSetProps(latestUserProps.toString())

            if (latestUserProps.length() > 0) {
                EventTrackManager.instance.trackUser(
                    Constant.PRESET_EVENT_USER_SET,
                    happenTime,
                    latestUserProps
                )
            }
        }
    }

    private fun setActiveUserProperties(context: Context) {
        if (AnalyticsConfig.instance.isSdkDisable()) {
            return
        }
        val happenTime = SystemClock.elapsedRealtime()

        PropertyManager.instance.updateSdkVersionProperty(
            PresetPropManager.get(context).userActive
        )
        val activeUserProperties = JSONObject(PropertyManager.instance.getActiveProperties())

        val eda = EventDataAdapter.getInstance(context)
        eda?.getUserSetOnceProps()?.onSameQueueThen { userSetOnceProps ->
            userSetOnceProps.split(",").forEach {
                activeUserProperties.remove(it)
            }

            if (activeUserProperties.length() > 0) {
                val sb = StringBuilder(userSetOnceProps).apply {
                    activeUserProperties.keys().forEach {
                        if (isNotEmpty()) {
                            append(",")
                        }
                        append(it)
                    }
                }.toString()
                eda.setUserSetOnceProps(sb)

                EventTrackManager.instance.trackUser(
                    Constant.PRESET_EVENT_USER_SET_ONCE,
                    happenTime,
                    activeUserProperties
                )
            }
        } ?: run {
            eda?.setUserSetOnceProps(StringBuilder().apply {
                activeUserProperties.keys().forEach {
                    if (isNotEmpty()) {
                        append(",")
                    }
                    append(it)
                }
            }.toString())

            if (activeUserProperties.length() > 0) {
                EventTrackManager.instance.trackUser(
                    Constant.PRESET_EVENT_USER_SET_ONCE,
                    happenTime,
                    activeUserProperties
                )
            }
        }
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
        val referrerClient: InstallReferrerClient? =
            InstallReferrerClient.newBuilder(context).build()
        referrerClient?.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                MainQueue.get().postTask {
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
            }

            override fun onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                MainQueue.get().postTask {
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
            }
        })
    }

    /**
     * 采集 app_install 事件
     */
    private fun trackAppInstallEvent(response: ReferrerDetails, failedReason: String) {
        // preset event disabled
        if (!PresetEvent.Install.isOn()) return

        //如果 app_install 事件已插入或者正在插入，则不处理
        if (isAppInstallTrackRunning.get()) {
            return
        }
        isAppInstallTrackRunning.set(true)

        val isOK = failedReason.isBlank()
        val happenTime = SystemClock.elapsedRealtime()

        EventTrackManager.instance.trackNormalPreset(
            Constant.PRESET_EVENT_APP_INSTALL,
            happenTime,
            JSONObject().also {
                val cnl = AnalyticsConfig.instance.mChannel
                PresetPropManager.get()?.run {
                    checkNSet(it,
                        Constant.ATTRIBUTE_PROPERTY_REFERRER_URL,
                        if (isOK) response.installReferrer + "&cnl=$cnl" else "cnl=$cnl"
                    )
                    checkNSet(it,
                        Constant.ATTRIBUTE_PROPERTY_REFERRER_CLICK_TIME,
                        if (isOK) response.referrerClickTimestampSeconds else 0
                    )
                    checkNSet(it,
                        Constant.ATTRIBUTE_PROPERTY_REFERRER_CLICK_TIME_SERVER,
                        if (isOK) response.referrerClickTimestampServerSeconds else 0
                    )
                    checkNSet(it,
                        Constant.ATTRIBUTE_PROPERTY_APP_INSTALL_TIME,
                        if (isOK) response.installBeginTimestampSeconds else 0
                    )
                    checkNSet(it,
                        Constant.ATTRIBUTE_PROPERTY_APP_INSTALL_TIME_SERVER,
                        if (isOK) response.installBeginTimestampServerSeconds else 0
                    )
                    checkNSet(it,
                        Constant.ATTRIBUTE_PROPERTY_INSTANT_EXPERIENCE_LAUNCHED,
                        if (isOK) response.googlePlayInstantParam else false
                    )
                    checkNSet(it,
                        Constant.ATTRIBUTE_PROPERTY_CNL,
                        cnl
                    )
                    if (!isOK) {
                        checkNSet(it,
                            Constant.ATTRIBUTE_PROPERTY_FAILED_REASON,
                            failedReason
                        )
                    }
//                    checkNSet(it, Constant.ATTRIBUTE_PROPERTY_USER_AGENT, EventUtils.ua)
                }
            },
            insertHandler = { code: Int, _: String ->
                if (code == 0) {
                    EventDataAdapter.getInstance()?.setIsAppInstallInserted(true)
                }
                isAppInstallTrackRunning.set(false)
            }
        )
    }

}
