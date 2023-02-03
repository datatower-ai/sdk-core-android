package com.roiquery.analytics.core

import android.os.SystemClock
import com.roiquery.analytics.Constant
import com.roiquery.analytics.api.AnalyticsImp
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.utils.*
import com.roiquery.quality.ROIQueryErrorParams
import com.roiquery.quality.ROIQueryQualityHelper
import org.json.JSONObject
import java.util.concurrent.ThreadFactory


class EventTrackManager {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            EventTrackManager()
        }
    }

    //事件采集管理线程池
    private var mTrackTaskManager: TrackTaskManager? = null

    //采集 、上报管理
    private var mAnalyticsManager: EventUploadManager? = null
    private var mTrackTaskManagerThread: TrackTaskManagerThread? = null

    fun init() {
        mTrackTaskManager = TrackTaskManager.getInstance()
        mTrackTaskManagerThread = TrackTaskManagerThread()
        Thread(mTrackTaskManagerThread, "DT.TaskQueueThread").start()
        mAnalyticsManager = EventUploadManager.getInstance()
        initTime()
    }

    /**
     * Init time
     * 初始化网络时间，保存至内存中
     */
    private fun initTime() {
        Thread {
            TimeCalibration.instance.getReferenceTime()
            EventDateAdapter.getInstance()?.enableUpload = true
        }.start()
    }


    /**
     * 事件校验
     */
    private fun assertEvent(
        eventName: String,
        properties: JSONObject? = null
    ) = EventUtils.isValidEventName(eventName) && EventUtils.isValidProperty(properties)


    /**
     * 用于 track 非预置事件
     *
     * */
    fun trackNormal(eventName: String?, properties: JSONObject? = JSONObject()) {
        trackInternal(eventName, Constant.EVENT_TYPE_TRACK, false, properties)
    }

    /**
     * 用于 track 预置事件
     *
     * */
    fun trackNormalPreset(
        eventName: String?,
        properties: JSONObject? = JSONObject(),
        insertHandler: ((code: Int, msg: String) -> Unit)? = null
    ) {
        trackInternal(eventName, Constant.EVENT_TYPE_TRACK, true, properties, insertHandler)
    }

    fun trackUser(eventName: String?, properties: JSONObject? = JSONObject()) {
        trackInternal(eventName, Constant.EVENT_TYPE_USER, true, properties)
    }

    fun trackUserWithPropertyCheck(eventName: String?, properties: JSONObject? = JSONObject()) {
        if (!EventUtils.isValidProperty(properties)) return
        trackInternal(eventName, Constant.EVENT_TYPE_USER, true, properties)
    }

    private fun trackInternal(
        eventName: String?,
        eventType: String,
        isPreset: Boolean,
        properties: JSONObject?,
        insertHandler: ((code: Int, msg: String) -> Unit)? = null
    ) {
        trackEvent(eventName, eventType, isPreset, properties, insertHandler)
    }

    private fun trackEvent(
        eventName: String?,
        eventType: String,
        isPreset: Boolean,
        properties: JSONObject?,
        insertHandler: ((code: Int, msg: String) -> Unit)? = null
    ) {
        if (mTrackTaskManager != null) {
            try {
                //事件名判空
                if (eventName.isNullOrEmpty()) {
                    insertHandler?.invoke(
                        ROIQueryErrorParams.CODE_TRACK_EVENT_NAME_EMPTY,
                        "event name isNullOrEmpty"
                    )
                    return
                }
                //事件时间，开机时长
                val eventTime = SystemClock.elapsedRealtime()

                //加入线程池
                mTrackTaskManager?.addTrackEventTask {
                    addEventTask(
                        eventName,
                        eventTime,
                        eventType,
                        isPreset,
                        properties,
                        insertHandler)
                }
            } catch (e: Exception) {
                LogUtils.printStackTrace(e)
                ROIQueryQualityHelper.instance.reportQualityMessage(
                    ROIQueryErrorParams.CODE_TRACK_ERROR,
                    "event name: $eventName "
                )
                insertHandler?.invoke(ROIQueryErrorParams.CODE_TRACK_ERROR, "trackEvent Exception")
            }
        } else {
            insertHandler?.invoke(ROIQueryErrorParams.CODE_TRACK_ERROR, "TrackTaskManager is null")
        }
    }

    private fun addEventTask(
        eventName: String,
        eventTimeUpTime: Long,
        eventType: String,
        isPreset: Boolean,
        properties: JSONObject? = null,
        insertHandler: ((code: Int, msg: String) -> Unit)? = null
    ) {
        try {
            //事件名、属性名规则校验
            if (!isPreset && !assertEvent(eventName, properties)) {
                insertHandler?.invoke(ROIQueryErrorParams.CODE_TRACK_EVENT_ILLEGAL, "event illegal")
                return
            }
            // 事件时间
            val (eventTime,isTimeVerify) = getEventTime(eventName, eventTimeUpTime)
            //设置事件的基本信息
            val eventInfo = JSONObject(PropertyManager.instance.getEventInfo()).apply {
                put(Constant.EVENT_INFO_TIME, eventTime)
                put(Constant.EVENT_INFO_NAME, eventName)
                put(Constant.EVENT_INFO_TYPE, eventType)
                put(Constant.EVENT_INFO_SYN, DataUtils.getUUID())
            }

            //事件属性, 常规事件与用户属性类型区分
            val eventProperties = if (eventType == Constant.EVENT_TYPE_TRACK) {
                JSONObject(PropertyManager.instance.getCommonProperties()).apply {
                    //添加动态属性
                    appendDynamicProperties(eventName, this)
                    //合并用户自定义属性和通用属性
                    DataUtils.mergeJSONObject(properties, this, null)
                }
            } else {
                properties
            }

            //设置事件属性
            eventInfo.put(Constant.EVENT_INFO_PROPERTIES, eventProperties)

            //将事件时间是否校准的结果保存至事件信息中，以供上报时校准时间使用
            val data = JSONObject().apply {
                put(Constant.EVENT_BODY, eventInfo)
                put(
                    Constant.EVENT_TIME_CALIBRATED,
                    isTimeVerify
                )
            }

            mAnalyticsManager?.enqueueEventMessage(
                eventName,
                data,
                eventInfo.optString(
                    Constant.EVENT_INFO_SYN
                ),
                insertHandler
            )
            //如果有插入失败的数据，则一起插入
            mAnalyticsManager?.enqueueErrorInsertEventMessage()

        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
            trackQualityEvent("trackEvent&&$eventName&& ${e.message}")
            insertHandler?.invoke(
                ROIQueryErrorParams.CODE_TRACK_ERROR,
                ROIQueryErrorParams.TRACK_GENERATE_EVENT_ERROR
            )
        }
    }

    private fun getEventTime(eventName: String, eventSystemUpTime: Long): Pair<Long, Boolean> {
        //服务器时间
        val serverTime = TimeCalibration.instance.getServerTime()
        //更新服务器时开机时间
        val updateSystemUpTime = TimeCalibration.instance.getUpdateSystemUpTime()
        //是否更新到了服务器时间
        var isTimeVerify = (serverTime != TimeCalibration.TIME_NOT_VERIFY_VALUE
                && updateSystemUpTime != TimeCalibration.TIME_NOT_VERIFY_VALUE)

        val time = if (eventName == Constant.PRESET_EVENT_APP_INSTALL) {
            // app_install 的时间因为一开始初始就记录，没有校准
            isTimeVerify = false
            AnalyticsImp.getInstance().firstOpenTime ?: (eventSystemUpTime - 1000)
        } else {
            if (isTimeVerify) (eventSystemUpTime - updateSystemUpTime + serverTime) else eventSystemUpTime
        }
        return Pair(time, isTimeVerify)
    }

    private fun appendDynamicProperties(eventName: String, properties: JSONObject) {
        properties.apply {
            //fps
            put(
                Constant.COMMON_PROPERTY_FPS,
                MemoryUtils.getFPS()
            )
            //硬盘使用率
            put(
                Constant.COMMON_PROPERTY_STORAGE_USED,
                MemoryUtils.getDisk(AnalyticsConfig.instance.mContext, false)
            )
            //内存使用率
            put(
                Constant.COMMON_PROPERTY_MEMORY_USED,
                MemoryUtils.getRAM(AnalyticsConfig.instance.mContext)
            )
            //事件时长
            EventTimerManager.instance.getEventTimer(eventName)?.duration()?.let {
                if (it > 0) {
                    this.put(
                        Constant.COMMON_PROPERTY_EVENT_DURATION,
                        it
                    )
                }
            }
        }
    }


    fun addTask(task: Runnable) {
        mTrackTaskManager?.let {
            try {
                it.addTrackEventTask(task)
            } catch (e: Exception) {
                trackQualityEvent("addTrackEventTask Exception")
            }
        }
    }


    private fun trackQualityEvent(qualityInfo: String) {
        ROIQueryQualityHelper.instance.reportQualityMessage(
            ROIQueryErrorParams.CODE_TRACK_ERROR,
            qualityInfo, ROIQueryErrorParams.TRACK_GENERATE_EVENT_ERROR
        )
    }

    internal class TrackThreadFactory : ThreadFactory {
        override fun newThread(r: Runnable): Thread {
            return Thread(r, "DT.TrackTaskManager").apply {
                this.uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { t: Thread?, e: Throwable? ->
                    LogUtils.e(e?.message)
                }
            }
        }
    }
}