package ai.datatower.analytics.core

import ai.datatower.analytics.Constant
import ai.datatower.analytics.api.AnalyticsImp
import ai.datatower.analytics.config.AnalyticsConfig
import ai.datatower.analytics.data.EventDataAdapter
import ai.datatower.analytics.taskqueue.MainQueue
import ai.datatower.analytics.utils.DataUtils
import ai.datatower.analytics.utils.EventUtils
import ai.datatower.analytics.utils.LogUtils
import ai.datatower.analytics.utils.MemoryUtils
import ai.datatower.analytics.utils.CommonPropsUtil
import ai.datatower.analytics.utils.PresetPropManager
import ai.datatower.analytics.utils.TimeCalibration
import ai.datatower.quality.DTErrorParams
import ai.datatower.quality.DTQualityHelper
import org.json.JSONObject
import java.util.concurrent.ThreadFactory


class EventTrackManager {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            EventTrackManager()
        }
    }

    //事件采集管理线程池
//    private var mTrackTaskManager: TrackTaskManager? = null

    //采集 、上报管理
    private var mAnalyticsManager: EventUploadManager? = null
//    private var mTrackTaskManagerThread: TrackTaskManagerThread? = null

    fun init() {
//        mTrackTaskManager = TrackTaskManager.getInstance()
//        mTrackTaskManagerThread = TrackTaskManagerThread()
//        Thread(mTrackTaskManagerThread, "DT.TaskQueueThread").start()
        mAnalyticsManager = EventUploadManager.getInstance()
        initTime()
    }

    /**
     * Init time
     * 初始化网络时间，保存至内存中
     */
    private fun initTime() {
        TimeCalibration.instance.getReferenceTime()
        EventDataAdapter.getInstance()?.setIsUploadEnabled(true)
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
    fun trackNormal(eventName: String?, eventTime: Long, properties: JSONObject? = JSONObject()) {
        trackInternal(eventName, eventTime, Constant.EVENT_TYPE_TRACK, false, properties)
    }

    /**
     * 用于 track 预置事件
     *
     * */
    fun trackNormalPreset(
        eventName: String?,
        eventTime: Long,
        properties: JSONObject? = JSONObject(),
        insertHandler: ((code: Int, msg: String) -> Unit)? = null
    ) {
        trackInternal(eventName, eventTime, Constant.EVENT_TYPE_TRACK, true, properties, insertHandler)
    }

    fun trackUser(eventName: String?, eventTime: Long, properties: JSONObject? = JSONObject()) {
        trackInternal(eventName, eventTime, Constant.EVENT_TYPE_USER, true, properties)
    }

    fun trackUserWithPropertyCheck(eventName: String?, eventTime: Long, properties: JSONObject? = JSONObject()) {
        if (!EventUtils.isValidProperty(properties)) return
        trackInternal(eventName, eventTime, Constant.EVENT_TYPE_USER, true, properties)
    }

    private fun trackInternal(
        eventName: String?,
        eventTime: Long,
        eventType: String,
        isPreset: Boolean,
        properties: JSONObject?,
        insertHandler: ((code: Int, msg: String) -> Unit)? = null
    ) {
        if (AnalyticsConfig.instance.isSdkDisable()) {
            insertHandler?.invoke(
                DTErrorParams.CODE_TRACK_EVENT_ILLEGAL,
                "sdk is disable"
            )
            return
        }
        trackEvent(eventName, eventTime, eventType, isPreset, properties, insertHandler)
    }

    private fun trackEvent(
        eventName: String?,
        eventTime: Long,
        eventType: String,
        isPreset: Boolean,
        properties: JSONObject?,
        insertHandler: ((code: Int, msg: String) -> Unit)? = null
    ) {
//        if (mTrackTaskManager != null) {
            try {
                //事件名判空
                if (eventName.isNullOrEmpty()) {
                    insertHandler?.invoke(
                        DTErrorParams.CODE_TRACK_EVENT_NAME_EMPTY,
                        "event name isNullOrEmpty"
                    )
                    return
                }
                //事件时间，开机时长
//                val eventTime = SystemClock.elapsedRealtime()

                //加入线程池
                MainQueue.get().postTask {
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
                DTQualityHelper.instance.reportQualityMessage(
                    DTErrorParams.CODE_TRACK_ERROR,
                    "event name: $eventName "
                )
                insertHandler?.invoke(DTErrorParams.CODE_TRACK_ERROR, "trackEvent Exception")
            }
//        }
//        else {
//            insertHandler?.invoke(DTErrorParams.CODE_TRACK_ERROR, "TrackTaskManager is null")
//        }
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
            // TODO: Optimization: Validate event before its being `addEventTask`ed.
            if (!isPreset && !assertEvent(eventName, properties)) {
                insertHandler?.invoke(DTErrorParams.CODE_TRACK_EVENT_ILLEGAL, "event illegal")
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

            val props = eventProperties ?: JSONObject()
            var delayedInsertCommon = false
            // 接入方设置的动态/静态通用属性，event_type 为 track 时添加
            // 最低优先级, 且 dynamic > static
            if (AnalyticsConfig.instance.mManualUploadSwitch.get()) {
                if (eventType == Constant.EVENT_TYPE_TRACK) {
                    CommonPropsUtil.applyCommonPropertiesToEvent(props)
                }
            } else {
                // Switch of enableTrack is off yet.
                delayedInsertCommon = true
            }

            //设置事件属性
            eventInfo.put(Constant.EVENT_INFO_PROPERTIES, props)

            //将事件时间是否校准的结果保存至事件信息中，以供上报时校准时间使用
            val data = JSONObject().apply {
                put(Constant.EVENT_BODY, eventInfo)
                put(
                    Constant.EVENT_TIME_CALIBRATED,
                    isTimeVerify
                )
                put(
                    Constant.EVENT_TIME_DEVICE,
                    System.currentTimeMillis(),
                )
                put(
                    Constant.EVENT_TIME_SESSION_ID,
                    TimeCalibration.instance.sessionId
                )
                put(
                    Constant.EVENT_TEMP_EXTRA_DELAY_INSERT_COMMON,
                    delayedInsertCommon
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
                DTErrorParams.CODE_TRACK_ERROR,
                DTErrorParams.TRACK_GENERATE_EVENT_ERROR
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
            PresetPropManager.get()?.checkNSet(
                properties,
                Constant.COMMON_PROPERTY_FPS,
                MemoryUtils.getFPS()
            )

            //硬盘使用率
            PresetPropManager.get()?.checkNSet(
                properties,
                Constant.COMMON_PROPERTY_STORAGE_USED,
                MemoryUtils.getDisk(AnalyticsConfig.instance.mContext, false)
            )

            //内存使用率
            PresetPropManager.get()?.checkNSet(
                properties,
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


//    fun addTask(task: Runnable) {
//        mTrackTaskManager?.let {
//            try {
//                it.addTrackEventTask(task)
//            } catch (e: Exception) {
//                trackQualityEvent("addTrackEventTask Exception")
//            }
//        }
//    }


    private fun trackQualityEvent(qualityInfo: String) {
        DTQualityHelper.instance.reportQualityMessage(
            DTErrorParams.CODE_TRACK_ERROR,
            qualityInfo, DTErrorParams.TRACK_GENERATE_EVENT_ERROR
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
