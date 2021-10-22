package com.roiquery.analytics.network


import com.roiquery.analytics.utils.LogUtils
import com.roiquery.cloudconfig.core.ResourceRemoteRepository
import com.roiquery.cloudconfig.exceptions.HttpException
import org.json.JSONObject

class HttpPOSTResourceRemoteRepository(
    private val url: String,
    private val params: () -> Map<String, String>
) : ResourceRemoteRepository {

    override fun fetch(
        success: (String) -> Unit,
        fail: (Exception) -> Unit
    ) {
        //http 请求
        val call =
            RequestHelper.Builder(
                HttpMethod.GET,
                url
            )
                .params(params.invoke())
                .retryCount(3)
        LogUtils.json("CloudConfig Reques：$url", params.invoke().toString())
        invokeInternal(call, success, fail)
    }

    private fun invokeInternal(
        call: RequestHelper.Builder,
        success: (String) -> Unit,
        fail: (Exception) -> Unit
    ) {

        call.callback(object : HttpCallback.JsonCallback() {
            override fun onFailure(code: Int, errorMessage: String?) {
                fail.invoke(
                    HttpException(
                        code,
                        errorMessage ?: ""
                    )
                )
            }

            override fun onResponse(response: JSONObject?) {
                try {
                    LogUtils.json("CloudConfig onResponse", response.toString())
                    if (response?.get(ResponseDataKey.KEY_CODE) == 0) {
                        success.invoke(response.getJSONObject(ResponseDataKey.KEY_DATA).toString())
                    } else {
                        fail.invoke(
                            HttpException(
                                response?.getInt(ResponseDataKey.KEY_CODE) ?: -1,
                                response?.getString(ResponseDataKey.KEY_MSG) ?: ""
                            )
                        )
                    }
                } catch (e: Exception) {
                    fail.invoke(
                        HttpException(
                            -1,
                            e.message.toString()
                        )
                    )
                }

            }

        }).execute()
    }


    companion object {
        fun create(url: String, params: () -> Map<String, String>): ResourceRemoteRepository =
            HttpPOSTResourceRemoteRepository(url, params)
    }
}
