package com.roiquery.analytics.core

import com.roiquery.analytics.Constant
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.utils.LogUtils
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

    fun correctEventTime(data: String, correctFinish: (info: String) -> Unit) {
        try {

            val jsonArray = JSONArray(data)
            val length = jsonArray.length()
            if (length == 0) {
                return
            }
            val correctedEventInfo = JSONArray()

            for (index in 0 until length) {
                jsonArray.getJSONObject(index)?.let { it ->
                    if (isNewFormatData(it)) {
                        correctNewFormatData(it, correctedEventInfo)
                    } else {
                        correctedEventInfo.put(it)
                    }
                }
            }

            correctFinish.invoke(correctedEventInfo.toString())

        } catch (e: JSONException) {
            correctFinish.invoke("")
        }
    }

    private fun correctNewFormatData(
        jsonEventBody: JSONObject,
        correctedEventInfo: JSONArray
    ) {
        jsonEventBody.optJSONObject(Constant.EVENT_BODY)?.let { eventInfo ->
            val presetEventName = eventNameForPreset(eventInfo)

            if (!correctAttributeFirstOpenTimeInfo(presetEventName, eventInfo)) return

            val infoTime = eventInfo.optLong(Constant.EVENT_INFO_TIME)

            //判定时间是否校准，未校准在已获得网络时间的情况下校准则校准
            if (!jsonEventBody.optBoolean(Constant.EVENT_TIME_CALIBRATED)) {
                val verifyTime =
                    TimeCalibration.instance.getVerifyTimeAsync()
                if (verifyTime != TimeCalibration.TIME_NOT_VERIFY_VALUE) {
                    val verifyTimeAsyncByGapTime =
                        TimeCalibration.instance.getVerifyTimeAsyncByGapTime(infoTime)
                    eventInfo.put(
                        Constant.EVENT_INFO_TIME,
                        verifyTimeAsyncByGapTime
                    )

                    if (presetEventName == Constant.PRESET_EVENT_APP_FIRST_OPEN) saveFirstOpenTime(
                        verifyTimeAsyncByGapTime
                    )

                    correctedEventInfo.put(eventInfo)

                } else {
                }
            } else {
                eventInfo.put(Constant.EVENT_INFO_TIME, infoTime)

                if (presetEventName == Constant.PRESET_EVENT_APP_FIRST_OPEN) saveFirstOpenTime(
                    infoTime
                )

                correctedEventInfo.put(eventInfo)
            }
        }
    }

    private fun eventNameForPreset(eventInfo: JSONObject) =
        if (eventInfo.optString(Constant.PRE_EVENT_INFO_NAME).isNotEmpty()) eventInfo.optString(
            Constant.PRE_EVENT_INFO_NAME
        ) else eventInfo.getString(Constant.EVENT_INFO_NAME)


    private fun correctAttributeFirstOpenTimeInfo(
        eventName: String,
        eventInfo: JSONObject
    ): Boolean {
        if (eventName == Constant.PRESET_EVENT_APP_ATTRIBUTE && eventInfo.optLong(Constant.ATTRIBUTE_PROPERTY_FIRST_OPEN_TIME) == 0L) {
            if (EventDateAdapter.getInstance()?.firstOpenTime == 0L) {
                return false
            } else {
                eventInfo.getJSONObject(Constant.EVENT_INFO_PROPERTIES).put(
                    Constant.ATTRIBUTE_PROPERTY_FIRST_OPEN_TIME,
                    EventDateAdapter.getInstance()?.firstOpenTime.toString()
                )
            }
        }
        return true
    }

    private fun isNewFormatData(eventInfo: JSONObject) =
        eventInfo.has(Constant.EVENT_TIME_CALIBRATED) && eventInfo.has(Constant.EVENT_BODY)

    private fun saveFirstOpenTime(firstOpenTime: Long) {
        EventDateAdapter.getInstance()?.firstOpenTime = firstOpenTime
        ROIQueryAnalytics.userSetOnce(JSONObject().apply {
            put(Constant.USER_PROPERTY_ACTIVE_EVENT_TIME, firstOpenTime.toString())
        })
    }

}