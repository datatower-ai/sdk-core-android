/*
 * Created by chenru on 2020/06/22.
 * Copyright 2015－2020 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.roiquery.analytics.network

import android.os.Handler
import com.roiquery.analytics.network.RealResponse
import android.text.TextUtils
import com.roiquery.analytics.network.HttpCallback
import org.json.JSONObject
import org.json.JSONException
import android.os.Looper
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.api.AnalyticsImp
import com.roiquery.analytics.utils.LogUtils

abstract class HttpCallback<T> {
    fun onError(response: RealResponse) {
        val errorMessage: String = if (!TextUtils.isEmpty(response.result)) {
            response.result
        } else if (!TextUtils.isEmpty(response.errorMsg)) {
            response.errorMsg
        } else if (response.exception != null) {
            response.exception.toString()
        } else {
            "unknown error"
        }
        sMainHandler.post {
            onFailure(response.code, errorMessage)
            onAfter()
        }
    }

    fun onSuccess(response: RealResponse) {
        AnalyticsImp.getInstance(ROIQueryAnalytics.mContext).calibrateTime(response.date)
        val obj: T = onParseResponse(response.result)
        sMainHandler.post {
            onResponse(obj)
            onAfter()
        }
    }

    /**
     * 解析 Response，执行在子线程
     *
     * @param result 网络请求返回信息
     * @return T
     */
    abstract fun onParseResponse(result: String): T

    /**
     * 访问网络失败后被调用，执行在 UI 线程
     *
     * @param code 请求返回的错误 code
     * @param errorMessage 错误信息
     */
    abstract fun onFailure(code: Int, errorMessage: String?)

    /**
     * 访问网络成功后被调用，执行在 UI 线程
     *
     * @param response 处理后的对象
     */
    abstract fun onResponse(response: T)

    /**
     * 访问网络成功或失败后调用
     */
    abstract fun onAfter()
    abstract class StringCallback : HttpCallback<String>() {
        override fun onParseResponse(result: String): String {
            return result
        }
    }

    abstract class JsonCallback : HttpCallback<JSONObject?>() {
        override fun onParseResponse(result: String): JSONObject? {
            try {
                if (!TextUtils.isEmpty(result)) {
                    return JSONObject(result)
                }
            } catch (e: JSONException) {
                LogUtils.printStackTrace(e)
            }
            return null
        }

        override fun onAfter() {}
    }

    interface ResponseDataKey {
        companion object {
            const val KEY_CODE = "code"
            const val KEY_MSG = "msg"
            const val KEY_DATA = "data"
        }
    }

    companion object {
        var sMainHandler = Handler(Looper.getMainLooper())
    }
}