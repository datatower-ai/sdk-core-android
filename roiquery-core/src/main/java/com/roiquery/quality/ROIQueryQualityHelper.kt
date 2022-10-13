package com.roiquery.quality

import com.roiquery.analytics.Constant
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.api.PropertyManager
import com.roiquery.analytics.network.HttpCallback
import com.roiquery.analytics.network.HttpMethod
import com.roiquery.analytics.network.RequestHelper
import com.roiquery.analytics.utils.EventUtils.getEventInfo
import com.roiquery.analytics.utils.LogUtils
import org.json.JSONObject

/**
 * author: xiaosailing
 * date: 2021-11-30
 * description:SDK 质量监控上报类
 * version：1.0
 */
internal class ROIQueryQualityHelper private constructor() {

    // 事件信息，包含事件的基本数据
    private var mEventInfo: MutableMap<String, Any?>? = null

    // 事件通用属性
    private var mCommonProperties: MutableMap<String, Any?>? = null

    fun reportQualityMessage(
        @ROIQueryErrorParams.ROIQueryErrorCode errorCode: Int,
        errorMsg: String?,
        @ROIQueryErrorParams.ROIQueryErrorMsg defaultErrorMsg: String? = null,
        @ROIQueryErrorParams.ROIQueryErrorLevel level: Int = ROIQueryErrorParams.TYPE_ERROR
    ) {
        try {
            RequestHelper.Builder(
                HttpMethod.POST_ASYNC,
                Constant.ERROR_REPORT_URL
            )
                .jsonData(getJsonData(errorCode, errorMsg, defaultErrorMsg, level))
                .retryCount(Constant.EVENT_REPORT_TRY_COUNT)
                .callback(object :
                    HttpCallback.JsonCallback() {
                    override fun onFailure(code: Int, errorMessage: String?) {
                        LogUtils.d("ROIQueryQuality onFailure", errorMessage)
                    }

                    override fun onResponse(response: JSONObject?) {
                        LogUtils.d("ROIQueryQuality onResponse", response.toString())
                    }

                    override fun onAfter() {
                    }
                }).execute()
        } catch (e: Exception) {
        }
    }

    private fun getJsonData(
        @ROIQueryErrorParams.ROIQueryErrorLevel errorType: Int,
        errorMsg: String?,
        defaultErrorMsg: String?,
        level: Int
    ): String {
        try {
            if (mEventInfo == null) {
                mEventInfo = PropertyManager.instance.getEventInfo()
            }
            if (mCommonProperties == null) {
                mCommonProperties = PropertyManager.instance.getCommonProperties()
            }
            val info = JSONObject(mEventInfo).apply {
                put(ERROR_CODE, errorType)
                put(ERROR_LEVEL, level)
                put(ERROR_MESSAGE, defaultErrorMsg.plus(errorMsg))
            }
            return info.toString()
        } catch (e: Exception) {
        }
        return JSONObject().toString()
    }


    companion object {
        private const val ERROR_CODE: String = "error_code"
        private const val ERROR_LEVEL = "error_level"
        private const val ERROR_MESSAGE: String = "error_message"
        val instance: ROIQueryQualityHelper by lazy {
            ROIQueryQualityHelper()
        }
    }
}