package com.roiquery.quality

import com.roiquery.analytics.Constant
import com.roiquery.analytics.api.PropertyManager
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

    private var mJsonParams: MutableMap<String, Any?>? = null

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
            val info = generateJsonParams()?.let {
                JSONObject(it).apply {
                    put(ERROR_CODE, errorType)
                    put(ERROR_LEVEL, level)
                    put(ERROR_MESSAGE, defaultErrorMsg.plus(errorMsg))
                }
            }
            return info.toString()
        } catch (e: Exception) {
        }
        return JSONObject().toString()
    }

    private fun generateJsonParams(): MutableMap<String, Any?>? {
        if (mJsonParams == null) {
            mJsonParams = mutableMapOf<String, Any?>().apply {
                PropertyManager.instance.getEventInfo().let {
                    put(APP_ID, it[Constant.EVENT_INFO_APP_ID])
                    put(INSTANCE_ID, it[Constant.EVENT_INFO_DT_ID])
                }
                PropertyManager.instance.getCommonProperties().let {
                    put(SDK_TYPE, it[Constant.COMMON_PROPERTY_SDK_TYPE])
                    put(SDK_VERSION_NAME, it[Constant.COMMON_PROPERTY_SDK_VERSION])
                    put(APP_VERSION_NAME, it[Constant.COMMON_PROPERTY_APP_VERSION_NAME])
                    put(OS_VERSION_NAME, it[Constant.COMMON_PROPERTY_OS_VERSION_NAME])
                    put(DEVICE_MODEL, it[Constant.COMMON_PROPERTY_DEVICE_MODEL])
                }
            }
        }
        return mJsonParams
    }

    companion object {
        private const val APP_ID            = "app_id"
        private const val INSTANCE_ID       = "instance_id"
        private const val SDK_TYPE          = "sdk_type"
        private const val SDK_VERSION_NAME  = "sdk_version_name"
        private const val APP_VERSION_NAME  = "app_version_name"
        private const val OS_VERSION_NAME   = "os_version_name"
        private const val DEVICE_MODEL      = "device_model"
        private const val ERROR_CODE        = "error_code"
        private const val ERROR_LEVEL       = "error_level"
        private const val ERROR_MESSAGE     = "error_message"
        val instance: ROIQueryQualityHelper by lazy {
            ROIQueryQualityHelper()
        }
    }
}