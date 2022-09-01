package com.roiquery.quality

import com.roiquery.analytics.Constant
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.network.HttpCallback
import com.roiquery.analytics.network.HttpMethod
import com.roiquery.analytics.network.RequestHelper
import com.roiquery.analytics.utils.LogUtils
import org.json.JSONObject

/**
 * author: xiaosailing
 * date: 2021-11-30
 * description:SDK 质量监控上报类
 * version：1.0
 */
internal class ROIQueryQualityHelper private constructor() {
    private val ERROR_TYPE: String = "message_type"
    private val ERROR_MESSAGE: String = "message_content"

    // 事件信息，包含事件的基本数据
    private var mEventInfo: MutableMap<String, Any?>? = null
    // 事件通用属性
    private var mCommonProperties: MutableMap<String, Any?>? = null

    fun reportQualityMessage(
        @ROIQueryErrorParams.ROIQueryErrorType errorType: String,
        errorMsg: String?
    ) {
        try {
            RequestHelper.Builder(
                HttpMethod.POST_ASYNC,
                Constant.ERROR_REPORT_URL
            )
                .jsonData(getJsonData(errorType, errorMsg))
                .retryCount(Constant.EVENT_REPORT_TRY_COUNT)
                .callback(object :
                    HttpCallback.JsonCallback() {
                    override fun onFailure(code: Int, errorMessage: String?) {
                        LogUtils.d("ROIQueryQuality onFailure",errorMessage)
                    }

                    override fun onResponse(response: JSONObject?) {
                        LogUtils.d("ROIQueryQuality onResponse",response.toString())
                    }

                    override fun onAfter() {
                    }
                }).execute()
        } catch (e: Exception) {
        }
    }
    
    private fun getJsonData(@ROIQueryErrorParams.ROIQueryErrorType errorType: String, errorMsg: String?): String{
        try {
            if (mEventInfo == null) {
                mEventInfo = ROIQueryAnalytics.getEventInfo()
            }
            if (mCommonProperties == null) {
                mCommonProperties = ROIQueryAnalytics.getCommonProperties()
            }
            val info = JSONObject(mEventInfo).apply {
                put(ERROR_TYPE, errorType)
                put("properties",JSONObject(mCommonProperties).apply {
                    put(ERROR_MESSAGE, errorMsg)
                })
            }
            return info.toString()
        } catch (e: Exception) {
        }
        return JSONObject().toString()
    }


    companion object {
        val instance: ROIQueryQualityHelper by lazy {
            ROIQueryQualityHelper()
        }
    }
}