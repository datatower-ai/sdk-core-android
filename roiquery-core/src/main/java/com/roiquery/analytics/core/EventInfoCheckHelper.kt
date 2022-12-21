package com.roiquery.analytics.core

import android.os.SystemClock
import com.roiquery.analytics.Constant
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.utils.TimeCalibration
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

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
            if (isNewFormatData(data)) {
                correctNewFormatData(data)
            } else {
                data
            }
        } catch (e: JSONException) {
            null
        }
    }

    private fun correctEventIdInfo(data: JSONObject):JSONObject? {
        try {
            val androidId = PropertyManager.instance.getAndroidId()
            val dtId = PropertyManager.instance.getDTID()
            val gaid = PropertyManager.instance.getGAID()
            if ((androidId.isEmpty()&& gaid.isEmpty()) || dtId.isEmpty() ) {
                return null
            }

            (if (isNewFormatData(data)) data.optJSONObject(Constant.EVENT_BODY) else data)?.let { eventInfo ->
                if (eventInfo.optString(Constant.EVENT_INFO_DT_ID).isEmpty()) {
                    eventInfo.put(Constant.EVENT_INFO_DT_ID, dtId)
                }
                if (eventInfo.optString(Constant.EVENT_INFO_GAID).isEmpty()&&gaid.isNotEmpty()) {
                    eventInfo.put(Constant.EVENT_INFO_GAID, gaid)
                }
                if (eventInfo.optString(Constant.EVENT_INFO_ANDROID_ID).isEmpty()&&eventInfo.optString(Constant.EVENT_INFO_GAID).isEmpty()) {
                    eventInfo.put(Constant.EVENT_INFO_ANDROID_ID, androidId)
                }
                if (eventInfo.optString(Constant.EVENT_INFO_DT_ID)
                        .isNotEmpty() && (eventInfo.optString(
                        Constant.EVENT_INFO_ANDROID_ID
                    ).isNotEmpty() || eventInfo.optString(Constant.EVENT_INFO_GAID)
                        .isNotEmpty())
                ) {
                        return data
                }
            }

        } catch (e: JSONException) {
           return null
        }
        return null
    }

    private fun correctNewFormatData(
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

    private fun correctAppStartTime(
        eventInfo: JSONObject,
        infoTime: Long
    ): JSONObject? {
            if (EventDateAdapter.getInstance()?.isFirstOpenTimeVerified == true) {
                eventInfo.put(
                    Constant.EVENT_INFO_TIME,
                    infoTime
                )
                return eventInfo
            } else {
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
            }
        return null
    }

    private fun eventNameForPreset(eventInfo: JSONObject) =
        if (eventInfo.optString(Constant.PRE_EVENT_INFO_NAME).isNotEmpty()) eventInfo.optString(
            Constant.PRE_EVENT_INFO_NAME
        ) else eventInfo.getString(Constant.EVENT_INFO_NAME)




    private fun isNewFormatData(eventInfo: JSONObject) =
        eventInfo.has(Constant.EVENT_TIME_CALIBRATED) && eventInfo.has(Constant.EVENT_BODY)



}