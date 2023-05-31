package com.roiquery.analytics.core

import android.os.SystemClock
import com.roiquery.analytics.Constant
import com.roiquery.analytics.utils.TimeCalibration
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.math.abs

/**
 * author: xiaosailing
 * date: 2022-07-13
 * description:
 * version：1.0
 */
class EventInfoCheckHelper private constructor() {
    companion object {
        val instance: EventInfoCheckHelper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            EventInfoCheckHelper()
        }
    }
    //事件头部属性（dt_id、gaid、android_id）、事件时间校准
    fun  correctEventInfo(data: String, correctFinish: (info: String) -> Unit){
        try {
            val jsonArray = JSONArray(data)
            val length = jsonArray.length()
            if (length == 0) {
                correctFinish.invoke("")
                return
            }
            val correctedEventInfo = JSONArray()

            for (index in 0 until length) {
                jsonArray.getJSONObject(index)?.let { it ->
                 correctEventIdInfo(it)?.let {
                     correctEventTime(it)?.let { correctData->
                         correctedEventInfo.put(correctData)
                     }
                 }
                }
            }
            correctFinish.invoke(correctedEventInfo.toString())

        } catch (e: JSONException) {
            correctFinish.invoke("")
        }
    }

    private fun correctEventTime(data: JSONObject): JSONObject? {
        return try {
            if(isFormatAfterVer200(data)) {
                correctDataVerAfter200(data)
            } else if (isFormatAfterVer100(data)) {
                correctDataVerUnder200AndAfter100(data)
            } else {
                data
            }
        } catch (e: JSONException) {
            null
        }
    }

    private fun correctEventIdInfo(data: JSONObject):JSONObject? {
        try {
            val dtId = PropertyManager.instance.getDTID()
            if (dtId.isEmpty()) {
                return null
            }
            (if (isFormatAfterVer100(data)) data.optJSONObject(Constant.EVENT_BODY) else data)?.let { eventInfo ->
                if (eventInfo.optString(Constant.EVENT_INFO_DT_ID).isEmpty()) {
                    eventInfo.put(Constant.EVENT_INFO_DT_ID, dtId)
                }
                return data
            }

        } catch (e: JSONException) {
           return null
        }
        return null
    }

    private fun correctDataVerUnder200AndAfter100(
        jsonEventBody: JSONObject
    ):JSONObject? {
        jsonEventBody.optJSONObject(Constant.EVENT_BODY)?.let { eventInfo ->
            val infoTime = eventInfo.optLong(Constant.EVENT_INFO_TIME, SystemClock.elapsedRealtime())
            //判定时间是否校准，未校准在已获得网络时间的情况下校准则校准
            if (!jsonEventBody.optBoolean(Constant.EVENT_TIME_CALIBRATED)) {
                //服务器时间
                val serverTime = TimeCalibration.instance.getServerTime()
                //更新服务器时开机时间
                val updateSystemUpTime = TimeCalibration.instance.getUpdateSystemUpTime()

                if (serverTime != TimeCalibration.TIME_NOT_VERIFY_VALUE
                    && updateSystemUpTime != TimeCalibration.TIME_NOT_VERIFY_VALUE) {
                    val realTime = infoTime - updateSystemUpTime + serverTime
                    eventInfo.put(
                        Constant.EVENT_INFO_TIME,
                        realTime
                    )
                    return eventInfo
                }
            } else {
                eventInfo.put(Constant.EVENT_INFO_TIME, infoTime)
                return eventInfo
            }
        }
        return null
    }

    private fun correctDataVerAfter200(
    jsonEventBody: JSONObject
    ):JSONObject? {
        val dHistoryTime = jsonEventBody.optLong(Constant.EVENT_TIME_DEVICE, 0)
        val sessionId = jsonEventBody.optString(Constant.EVENT_TIME_SESSION_ID)

        if ( dHistoryTime == 0L || sessionId == null || sessionId.isEmpty()) {
            // toto lilinli report
            return null
        }

        jsonEventBody.optJSONObject(Constant.EVENT_BODY)?.let { eventInfo ->
            val infoTime = eventInfo.optLong(Constant.EVENT_INFO_TIME, 0)

            //判定时间是否校准，未校准在已获得网络时间的情况下校准则校准
            if (!jsonEventBody.optBoolean(Constant.EVENT_TIME_CALIBRATED)) {
                //服务器时间
                val serverTime = TimeCalibration.instance.getServerTime()
                //更新服务器时开机时间
                val updateSystemUpTime = TimeCalibration.instance.getUpdateSystemUpTime()

                val sInterval = updateSystemUpTime - infoTime

                if (serverTime != TimeCalibration.TIME_NOT_VERIFY_VALUE
                    && updateSystemUpTime != TimeCalibration.TIME_NOT_VERIFY_VALUE) {

                    if (sessionId == TimeCalibration.instance.sessionId) {
//                        当前进程启动产生的数据
                        val realTime = infoTime - updateSystemUpTime + serverTime
                        eventInfo.put(
                            Constant.EVENT_INFO_TIME,
                            realTime
                        )
                        return eventInfo
                    } else {
                        val deviceTime = TimeCalibration.instance.getDeviceTime()
                        val dInterval = deviceTime - dHistoryTime
                        var realTime = 0L

                        //  历史数据，时间戳不可行
                        if (TimeCalibration.instance.isDeviceTimeCorrect()) {
                        //  设备时间是对的
                            realTime = if ((sInterval * dInterval) > 0L && abs(sInterval - dInterval) < 5 * 60 * 1000L) {
                        // 没有调时，也没有启动过
                                infoTime - updateSystemUpTime + serverTime
                            } else {
                        // 重启过
                                dHistoryTime
                            }

                        } else {
                            realTime = if ((sInterval * dInterval) > 0L && abs(sInterval - dInterval) < 5 * 60 * 1000L) {
//                                没重启过，系统时间与实际时间的差是固定的
                                infoTime - updateSystemUpTime + serverTime
                            } else {
                                eventInfo.optJSONObject(Constant.EVENT_INFO_PROPERTIES)?.put(
                                    Constant.EVENT_TIME_CAN_TRUSTED,
                                    false
                                )
                                dHistoryTime
                            }
                        }

                        eventInfo.put(
                            Constant.EVENT_INFO_TIME,
                            realTime
                        )

                        return  eventInfo
                    }
                }
            } else {
                eventInfo.put(Constant.EVENT_INFO_TIME, infoTime)
                return eventInfo
            }
        }
        return null
    }

    private fun isFormatAfterVer200(eventInfo: JSONObject) =
        eventInfo.has(Constant.EVENT_TIME_CALIBRATED) && eventInfo.has(Constant.EVENT_BODY) &&
                eventInfo.has(Constant.EVENT_TIME_DEVICE) && eventInfo.has(Constant.EVENT_TIME_SESSION_ID)

    private fun isFormatAfterVer100(eventInfo: JSONObject) =
        eventInfo.has(Constant.EVENT_TIME_CALIBRATED) && eventInfo.has(Constant.EVENT_BODY)

}
