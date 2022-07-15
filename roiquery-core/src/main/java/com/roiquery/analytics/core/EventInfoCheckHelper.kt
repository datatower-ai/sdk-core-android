package com.roiquery.analytics.core

import com.roiquery.analytics.Constant
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.utils.TimeCalibration
import com.roiquery.analytics.utils.transToLong
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

    fun correctEventTime(data: String,correctFinish:(info:String,reInsertData:JSONArray)->Unit) {

        val reInsertData = JSONArray()
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
                        correctNewFormatData(it, reInsertData, correctedEventInfo)
                    } else {
                        correctedEventInfo.put(it)
                    }
                }
            }

            correctFinish.invoke(correctedEventInfo.toString(),reInsertData)

        } catch (e: JSONException) {
            correctFinish.invoke("",reInsertData)
        }
    }

    private fun correctNewFormatData(
        jsonEventBody: JSONObject,
        reInsertData: JSONArray,
        correctedEventInfo: JSONArray
    ) {
        jsonEventBody.optJSONObject(Constant.EVENT_BODY)?.let { eventInfo ->
            val presetEventName = eventNameForPreset(eventInfo)

            if (!correctAttributeFirstOpenTimeInfo(presetEventName, eventInfo, reInsertData,jsonEventBody)) return

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
                        verifyTimeAsyncByGapTime.toString()
                    )

                    if (presetEventName == Constant.PRESET_EVENT_APP_FIRST_OPEN) saveFirstOpenTime(verifyTimeAsyncByGapTime)

                    correctedEventInfo.put(eventInfo)

                } else {
                    reInsertData.put(jsonEventBody)
                }
            } else {
                eventInfo.put(Constant.EVENT_INFO_TIME,infoTime.toString())

                if (presetEventName == Constant.PRESET_EVENT_APP_FIRST_OPEN) saveFirstOpenTime(infoTime)

                correctedEventInfo.put(eventInfo)
            }
        }
    }

    private fun eventNameForPreset(eventInfo: JSONObject) =
        "${Constant.PRESET_EVENT_TAG}${eventInfo.getString(Constant.EVENT_INFO_NAME)}"


    private fun correctAttributeFirstOpenTimeInfo(
        eventName: String,
        eventInfo: JSONObject,
        reInsertData: JSONArray,
        eventBodyInfo: JSONObject
    ):Boolean {
        if (eventName == Constant.PRESET_EVENT_APP_ATTRIBUTE && eventInfo.optLong(
                Constant.ATTRIBUTE_PROPERTY_FIRST_OPEN_TIME
            ) == 0L
        ) {
            if (EventDateAdapter.getInstance()?.firstOpenTime == 0L) {
                reInsertData.put(eventBodyInfo)
                return false
            } else {
                eventInfo.put(
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
    }

}