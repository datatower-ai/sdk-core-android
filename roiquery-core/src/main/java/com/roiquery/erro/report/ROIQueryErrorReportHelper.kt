package com.roiquery.erro.report

import com.roiquery.analytics.Constant
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
class ROIQueryErrorReportHelper private constructor() {
    private val ERROR_TYPE: String = "error_type"
    private val ERROR_MESSAGE: String = "message"

    fun reportDebugErrorReport(
        @ROIQueryErrorParams.ROIQueryErrorType errorType: String,
        errorMsg: String?,
        jsonObject: JSONObject?
    ) {

        val body = jsonObject?.optJSONObject(Constant.EVENT_BODY) ?: JSONObject()
        val propertiesObj = body.optJSONObject(Constant.EVENT_INFO_PROPERTIES) ?: jsonObject
        propertiesObj?.apply {
            put(ERROR_TYPE, errorType)
            put(ERROR_MESSAGE, errorMsg)
        }
        body.apply {
            remove(Constant.EVENT_INFO_PROPERTIES)
            put(Constant.EVENT_INFO_PROPERTIES, propertiesObj)
        }


        RequestHelper.Builder(
            HttpMethod.POST_ASYNC,
            Constant.ERROR_REPORT_URL
        )
            .jsonData(body.toString())
            .retryCount(Constant.EVENT_REPORT_TRY_COUNT)
            .callback(object :
                HttpCallback.JsonCallback() {
                override fun onFailure(code: Int, errorMessage: String?) {
                    LogUtils.w(errorMessage)
                }

                override fun onResponse(response: JSONObject?) {
                    LogUtils.w(response.toString())
                }

                override fun onAfter() {
                }
            }).execute()
    }

    fun reportDebugErrorReport(
        @ROIQueryErrorParams.ROIQueryErrorType errorType: String,
        errorMsg: String,
        map: Map<String, Any?>?
    ) {
        val jsonObject = JSONObject()
        map?.entries?.forEach { it ->
            jsonObject.apply {
                put(it.key ?: "", it.value)
            }
        }
        reportDebugErrorReport(errorType, errorMsg, jsonObject)
    }


    companion object {
        private var roiQueryErrorReportHelper: ROIQueryErrorReportHelper? = null
        fun instance(): ROIQueryErrorReportHelper {
            roiQueryErrorReportHelper = ROIQueryErrorReportHelper()
            return roiQueryErrorReportHelper as ROIQueryErrorReportHelper
        }
    }
}