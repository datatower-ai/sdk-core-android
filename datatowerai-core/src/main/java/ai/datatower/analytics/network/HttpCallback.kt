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
package ai.datatower.analytics.network

import ai.datatower.analytics.utils.LogUtils
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import org.json.JSONException
import org.json.JSONObject

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
        val obj: T = onParseResponse(response,response.result)
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
    abstract fun onParseResponse(response: RealResponse,result: String): T

    /**
     * 访问网络失败后被调用，执行在 子 线程
     *
     * @param code 请求返回的错误 code
     * @param errorMessage 错误信息
     */
    abstract fun onFailure(code: Int, errorMessage: String?)

    /**
     * 访问网络成功后被调用，执行在 子 线程
     *
     * @param response 处理后的对象
     */
    abstract fun onResponse(response: T)

    /**
     * 访问网络成功或失败后调用
     */
    abstract fun onAfter()

    abstract class TimeCallback : HttpCallback<Long>() {
        override fun onParseResponse(response: RealResponse, result: String): Long {
            return response.date
        }
        override fun onAfter() {}
    }
    abstract class StringCallback : HttpCallback<String>() {
        override fun onParseResponse(response: RealResponse,result: String): String {
            return result
        }
    }

    abstract class JsonCallback : HttpCallback<JSONObject?>() {
        override fun onParseResponse(response: RealResponse,result: String): JSONObject? {
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