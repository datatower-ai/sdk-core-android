package com.roiquery.cloudconfig.remote


import com.roiquery.analytics.network.HttpCallback
import com.roiquery.analytics.network.HttpMethod
import com.roiquery.analytics.network.RequestHelper
import com.roiquery.cloudconfig.core.ResourceRemoteRepository
import com.roiquery.cloudconfig.exceptions.HttpException
import org.json.JSONObject

class HttpPOSTResourceRemoteRepository(
    private val url: String
) : ResourceRemoteRepository {

    override fun fetch(
        success: (String) -> Unit,
        fail: (Exception) -> Unit
    ) {
        //http 请求
        val call =
            RequestHelper.Builder(
                HttpMethod.POST,
                url
            )
                .jsonData(getParameters())
                .retryCount(3)
        invokeInternal(call, success, fail)
    }

    private fun invokeInternal(
        call: RequestHelper.Builder,
        success: (String) -> Unit,
        fail: (Exception) -> Unit
    ) {

        call.callback(object :HttpCallback.JsonCallback(){
            override fun onFailure(code: Int, errorMessage: String?) {
                fail.invoke(HttpException(
                    code,
                    errorMessage ?: ""
                ))
            }

            override fun onResponse(response: JSONObject?) {
                if (response?.get("code") == 0) {
                    success.invoke(response.get("config").toString())
                } else {
                    fail.invoke(HttpException(
                        response?.get("code") as Int,
                        response.get("msg") as String
                    ))
                }
            }

        }).execute()
    }

    fun getParameters():String{
        return ""
    }

    companion object {
        fun create(url: String): ResourceRemoteRepository = HttpPOSTResourceRemoteRepository(url)
    }
}
