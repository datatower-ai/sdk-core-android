package ai.datatower.analytics.core

import ai.datatower.analytics.Constant
import ai.datatower.analytics.config.AnalyticsConfig
import ai.datatower.analytics.data.EventDataAdapter
import ai.datatower.analytics.taskqueue.MainQueue
import ai.datatower.analytics.utils.EventUtils
import ai.datatower.analytics.utils.LogUtils
import ai.datatower.analytics.utils.PresetEvent
import ai.datatower.analytics.utils.ProcessUtil
import android.content.Context
import android.os.SystemClock
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import org.json.JSONObject
import java.lang.StringBuilder
import java.util.concurrent.atomic.AtomicBoolean

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
        mDataAdapter = EventDataAdapter.getInstance()
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

        val eda = EventDataAdapter.getInstance()
        eda?.getLastUserSetProps()?.onSameQueueThen { lastUserSetPropsStr ->
            val savedUserSetProps = JSONObject(lastUserSetPropsStr)
            latestUserProps.keys().forEach {
                val current = latestUserProps.get(it)
                if (savedUserSetProps.has(it) && current == savedUserSetProps.get(it)) {
                    // 与之前相同
                    latestUserProps.remove(it)
                } else {
                    // 与之前不同，或之前没有
                    savedUserSetProps.put(it, current)
                }
            }
            eda.setLastUserSetProps(savedUserSetProps.toString())
        }

        if (latestUserProps.length() > 0) {
            EventTrackManager.instance.trackUser(
                Constant.PRESET_EVENT_USER_SET,
                happenTime,
                latestUserProps
            )
        }
    }

    private fun setActiveUserProperties(context: Context) {
        if (AnalyticsConfig.instance.isSdkDisable()) {
            return
        }
        val happenTime = SystemClock.elapsedRealtime()

        val activeUserProperties =
            JSONObject(PropertyManager.instance.getActiveProperties()).apply {
                PropertyManager.instance.updateSdkVersionProperty(
                    this,
                    Constant.USER_PROPERTY_ACTIVE_SDK_TYPE,
                    Constant.USER_PROPERTY_ACTIVE_SDK_VERSION
                )
            }

        val eda = EventDataAdapter.getInstance()
        eda?.getUserSetOnceProps()?.onSameQueueThen { userSetOnceProps ->
            userSetOnceProps.split(",").forEach {
                activeUserProperties.remove(it)
            }

            if (activeUserProperties.length() > 0) {
                val sb = StringBuilder(userSetOnceProps)
                activeUserProperties.keys().forEach {
                    if (sb.isNotEmpty()) {
                        sb.append(",")
                    }
                    sb.append(it)
                }
                eda.setUserSetOnceProps(sb.toString())

                EventTrackManager.instance.trackUser(
                    Constant.PRESET_EVENT_USER_SET_ONCE,
                    happenTime,
                    activeUserProperties
                )
            }
        } ?: run {
            // Failed to get previous set once of 'active_xxx', work as normal.
            EventTrackManager.instance.trackUser(
                Constant.PRESET_EVENT_USER_SET_ONCE,
                happenTime,
                activeUserProperties
            )
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
            JSONObject().apply {
                val cnl = AnalyticsConfig.instance.mChannel
                put(
                    Constant.ATTRIBUTE_PROPERTY_REFERRER_URL,
                    if (isOK) response.installReferrer + "&cnl=$cnl" else "cnl=$cnl"
                )
                put(
                    Constant.ATTRIBUTE_PROPERTY_REFERRER_CLICK_TIME,
                    if (isOK) response.referrerClickTimestampSeconds else 0
                )
                put(
                    Constant.ATTRIBUTE_PROPERTY_REFERRER_CLICK_TIME_SERVER,
                    if (isOK) response.referrerClickTimestampServerSeconds else 0
                )
                put(
                    Constant.ATTRIBUTE_PROPERTY_APP_INSTALL_TIME,
                    if (isOK) response.installBeginTimestampSeconds else 0
                )
                put(
                    Constant.ATTRIBUTE_PROPERTY_APP_INSTALL_TIME_SERVER,
                    if (isOK) response.installBeginTimestampServerSeconds else 0
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
//                put(Constant.ATTRIBUTE_PROPERTY_USER_AGENT, EventUtils.ua)
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
