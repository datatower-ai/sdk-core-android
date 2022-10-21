package com.roiquery.analytics.core

import com.roiquery.analytics.Constant
import com.roiquery.analytics.utils.*
import com.roiquery.quality.ROIQueryErrorParams
import com.roiquery.quality.ROIQueryQualityHelper
import org.json.JSONObject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class EventTrackManager {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            EventTrackManager()
        }
    }

    //事件采集管理线程池
    private var mTrackTaskManager: ExecutorService? = null

    //采集 、上报管理
    private var mAnalyticsManager: EventUploadManager? = null

    fun init(){
        mTrackTaskManager = Executors.newSingleThreadExecutor()
        mAnalyticsManager = EventUploadManager.getInstance()
        initTime()
    }

    /**
     * Init time
     * 初始化网络时间，保存至内存中
     */
    private fun initTime() {
        TimeCalibration.instance.getReferenceTime()
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
    fun trackNormal(eventName: String?, properties: JSONObject? = JSONObject()){
        trackInternal(eventName, Constant.EVENT_TYPE_TRACK, false, properties)
    }

    /**
     * 用于 track 预置事件
     *
     * */
    fun trackNormalPreset(eventName: String?, properties: JSONObject? = JSONObject(), insertHandler: ((code: Int, msg: String) -> Unit)? = null){
        trackInternal(eventName, Constant.EVENT_TYPE_TRACK, true, properties, insertHandler)
    }

    fun trackUser(eventName: String?, properties: JSONObject? = JSONObject()){
        trackInternal(eventName, Constant.EVENT_TYPE_USER, true, properties)
    }

    fun trackUserWithPropertyCheck(eventName: String?, properties: JSONObject? = JSONObject()){
        if(!EventUtils.isValidProperty(properties)) return
        trackInternal(eventName, Constant.EVENT_TYPE_USER, true, properties)
    }

    private fun trackInternal(eventName: String?, eventType: String, isPreset: Boolean, properties: JSONObject?, insertHandler: ((code: Int, msg: String) -> Unit)? = null) {
        trackEvent(eventName, eventType, isPreset, properties, insertHandler)
    }

    private fun trackEvent(eventName: String?, eventType: String, isPreset: Boolean, properties: JSONObject?, insertHandler: ((code: Int, msg: String) -> Unit)? = null){
        mTrackTaskManager?.let {
            try {
                it.execute {
                    addEventTask(eventName, eventType, isPreset, properties, insertHandler)
                }
            } catch (e: Exception) {
                LogUtils.printStackTrace(e)
                ROIQueryQualityHelper.instance.reportQualityMessage(
                    ROIQueryErrorParams.CODE_TRACK_ERROR,
                    "event name: $eventName "
                )
                insertHandler?.invoke(ROIQueryErrorParams.CODE_TRACK_ERROR,"trackEvent Exception")
            }
        }
    }

    private fun addEventTask(
        eventName: String?,
        eventType: String,
        isPreset: Boolean,
        properties: JSONObject? = null,
        insertHandler: ((code: Int, msg: String) -> Unit)? = null
    ) {
        try {
            if (eventName.isNullOrEmpty()){
                insertHandler?.invoke(ROIQueryErrorParams.CODE_TRACK_EVENT_NAME_EMPTY,"event name isNullOrEmpty")
                return
            }

            if (!isPreset && !assertEvent(eventName, properties)){
                insertHandler?.invoke(ROIQueryErrorParams.CODE_TRACK_EVENT_ILLEGAL,"event illegal")
                return
            }

            var isTimeVerify: Boolean

            //设置事件的基本信息
            val eventInfo = JSONObject(PropertyManager.instance.getEventInfo()).apply {
                TimeCalibration.instance.getVerifyTimeAsync().apply {
                    isTimeVerify = this != TimeCalibration.TIME_NOT_VERIFY_VALUE
                    // 如果时间已校准，则 保存当前时间，否则保存当前时间的系统休眠时间差用做上报时时间校准依据
                    put(
                        Constant.EVENT_INFO_TIME,
                        if (isTimeVerify) this else TimeCalibration.instance.getSystemHibernateTimeGap()
                    )
                    put(Constant.EVENT_INFO_NAME, eventName)
                    put(Constant.EVENT_INFO_TYPE, eventType)
                    put(Constant.EVENT_INFO_SYN, DataUtils.getUUID())
                }
            }

            //事件属性, 常规事件与用户属性类型区分
            val eventProperties = if (eventType == Constant.EVENT_TYPE_TRACK) {
                JSONObject(PropertyManager.instance.getCommonProperties()).apply {
                    //应用是否在前台, 需要动态添加
//                    put(
//                        Constant.COMMON_PROPERTY_IS_FOREGROUND,
//                        mDataAdapter?.isAppForeground
//                    )
                    //硬盘使用率
                    put(
                        Constant.COMMON_PROPERTY_STORAGE_USED,
                        MemoryUtils.getDisk(AdtUtil.getInstance().applicationContext,false)
                    )
                    //内存使用率
                    put(
                        Constant.COMMON_PROPERTY_MEMORY_USED,
                        MemoryUtils.getRAM(AdtUtil.getInstance().applicationContext)
                    )
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
                put(Constant.EVENT_TIME_CALIBRATED, isTimeVerify)
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
            insertHandler?.invoke(ROIQueryErrorParams.CODE_TRACK_ERROR, ROIQueryErrorParams.TRACK_GENERATE_EVENT_ERROR)
        }
    }



    private fun trackQualityEvent(qualityInfo: String) {
        ROIQueryQualityHelper.instance.reportQualityMessage(
            ROIQueryErrorParams.CODE_TRACK_ERROR,
            qualityInfo, ROIQueryErrorParams.TRACK_GENERATE_EVENT_ERROR
        )
    }


}