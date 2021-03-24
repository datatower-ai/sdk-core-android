//package com.roiquery.analytics.network
//
//
//import com.roiquery.analytics.utils.LogUtils
//import com.roiquery.cloudconfig.core.ResourceRemoteRepository
//import com.roiquery.cloudconfig.exceptions.HttpException
//import org.json.JSONObject
//
//class HttpPOSTResourceRemoteRepository(
//    private val url: String,
//    private val params: MutableMap<String, Any?>?
//) : ResourceRemoteRepository {
//
//    override fun fetch(
//        success: (String) -> Unit,
//        fail: (Exception) -> Unit
//    ) {
//        //http 请求
//        val call =
//            RequestHelper.Builder(
//                HttpMethod.POST,
//                url
//            )
//                .jsonData(JSONObject(params).toString())
//                .retryCount(3)
//        invokeInternal(call, success, fail)
//    }
//
//    private fun invokeInternal(
//        call: RequestHelper.Builder,
//        success: (String) -> Unit,
//        fail: (Exception) -> Unit
//    ) {
//
//        call.callback(object :HttpCallback.JsonCallback(){
//            override fun onFailure(code: Int, errorMessage: String?) {
//                fail.invoke(HttpException(
//                    code,
//                    errorMessage ?: ""
//                ))
//            }
//
//            override fun onResponse(response: JSONObject?) {
//                try {
//                    LogUtils.json("CloudConfig onResponse",response.toString())
//                    if (response?.get("code") == 0) {
//                        success.invoke(response.get("data").toString())
//                    } else {
//                        fail.invoke(HttpException(
//                            response?.get("code") as Int,
//                            response.get("msg") as String
//                        ))
//                    }
//                } catch (e:Exception){
//                    fail.invoke(HttpException(
//                        -1,
//                        e.message.toString()
//                    ))
//                }
//
//            }
//
//        }).execute()
//    }
//
//
//    companion object {
//        fun create(url: String,params: MutableMap<String, Any?>?): ResourceRemoteRepository = HttpPOSTResourceRemoteRepository(url,params)
//    }
//}
