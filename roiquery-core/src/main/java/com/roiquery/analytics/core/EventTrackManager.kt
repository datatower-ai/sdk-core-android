package com.roiquery.analytics.core

import android.content.Context
import com.roiquery.analytics.Constant
import com.roiquery.analytics.ROIQueryAnalytics.Companion.mContext
import com.roiquery.analytics.api.PropertyManager
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
    protected var mTrackTaskManager: ExecutorService? = null

    //采集 、上报管理
    protected var mAnalyticsManager: AnalyticsManager? = null

    fun init(){
        mTrackTaskManager = Executors.newSingleThreadExecutor()
        mAnalyticsManager = AnalyticsManager.getInstance()
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


    fun trackEvent(eventName: String?, eventType: String, isPreset: Boolean, properties: JSONObject?){
        mTrackTaskManager?.let {
            try {
                it.execute {
                    trackEventTask(eventName, eventType, isPreset, properties)
                }
            } catch (e: Exception) {
                LogUtils.printStackTrace(e)
                ROIQueryQualityHelper.instance.reportQualityMessage(
                    ROIQueryErrorParams.CODE_TRACK_ERROR,
                    "event name: $eventName "
                )
            }
        }
    }

    private fun trackEventTask(
        eventName: String?,
        eventType: String,
        isPreset: Boolean,
        properties: JSONObject? = null
    ) {
        try {
            if (eventName.isNullOrEmpty()) return

            if (!isPreset && !assertEvent(eventName, properties)) return

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
                        MemoryUtils.getStorageUsed(mContext)
                    )
                    //内存使用率
                    put(
                        Constant.COMMON_PROPERTY_MEMORY_USED,
                        MemoryUtils.getMemoryUsed(mContext)
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
                eventName, data, eventInfo.optString(
                    Constant.EVENT_INFO_SYN
                )
            )
            //如果有插入失败的数据，则一起插入
            mAnalyticsManager?.enqueueErrorInsertEventMessage()

        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
            trackQualityEvent("trackEvent&&$eventName&& ${e.message}")
        }
    }



    private fun trackQualityEvent(qualityInfo: String) {
        ROIQueryQualityHelper.instance.reportQualityMessage(
            ROIQueryErrorParams.CODE_TRACK_ERROR,
            qualityInfo, ROIQueryErrorParams.TRACK_GENERATE_EVENT_ERROR
        )
    }


}