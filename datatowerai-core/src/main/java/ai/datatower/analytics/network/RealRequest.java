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

package ai.datatower.analytics.network;

import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import ai.datatower.analytics.utils.LogUtils;


class RealRequest {

    private static final String TAG = "HttpRequest";
    private static final int CONNECT_TIMEOUT = 30000;
    private static final int READ_TIMEOUT = 30000;
    private static String sRequestURL;

    /**
     * GET 请求
     *
     * @param requestURL 请求 url
     * @param headerMap 请求头键值对
     * @return RealResponse 返回的 Response 信息
     */
    RealResponse getData(String requestURL, Map<String, String> headerMap) {
        try {
//            LogUtils.i(TAG,String.format("url:%s\nmethod:GET", requestURL));
            sRequestURL = requestURL;
            HttpURLConnection conn;
            conn = getHttpURLConnection(requestURL, "GET");
            if (headerMap != null) {
                setHeader(conn, headerMap);
            }
            conn.connect();
            return getRealResponse(conn);
        } catch (Exception e) {
            return getExceptionResponse(e);
        }
    }

    /**
     * POST 请求
     *
     * @param requestURL 请求 url
     * @param body 请求 body 信息
     * @param bodyType 请求的 Content-Type
     * @param headerMap 请求头键值对
     * @return RealResponse 返回的 Response 信息
     */
    RealResponse postData(String requestURL, String body, String bodyType, Map<String, String> headerMap) {
        BufferedWriter writer = null;
        try {
            HttpURLConnection conn;
            sRequestURL = requestURL;
//            LogUtils.i(TAG, String.format("url:%s\nparams:%s\nmethod:POST", requestURL, body));
            conn = getHttpURLConnection(requestURL, "POST");
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            if (!TextUtils.isEmpty(bodyType)) {
                conn.setRequestProperty("Content-Type", bodyType);
            }
            if (headerMap != null) {
                setHeader(conn, headerMap);
            }
            conn.connect();
            if (!TextUtils.isEmpty(body)) {
                writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8));
                writer.write(body);
                writer.flush();
            }
            return getRealResponse(conn);
        } catch (Exception e) {
            return getExceptionResponse(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    LogUtils.printStackTrace(e);
                }
            }
        }
    }

    /**
     * 得到 HttpURLConnection 对象，并进行一些设置
     *
     * @param requestURL 请求的 url
     * @param requestMethod 请求方法: POST,GET
     * @return HttpURLConnection
     * @throws IOException IOException
     */
    private HttpURLConnection getHttpURLConnection(String requestURL, String requestMethod) throws IOException {
        URL url = new URL(requestURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(requestMethod);
        //不使用缓存
        conn.setUseCaches(false);
        //设置超时时间
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        //设置读取超时时间
        conn.setReadTimeout(READ_TIMEOUT);
        if (requestMethod.equals("POST")) {
            //设置为 true 后才能写入参数
            conn.setDoOutput(true);
        }

        return conn;
    }

    /**
     * 设置请求头
     *
     * @param conn HttpURLConnection
     * @param headerMap 请求头键值对
     */
    private void setHeader(HttpURLConnection conn, Map<String, String> headerMap) {
        if (headerMap != null) {
            for (String key : headerMap.keySet()) {
                conn.setRequestProperty(key, headerMap.get(key));
            }
        }
    }

    /**
     * 当正常返回时，返回正常信息的 RealResponse 对象
     *
     * @param conn HttpURLConnection
     * @return RealResponse 网络请求返回信息
     */
    private RealResponse getRealResponse(HttpURLConnection conn) {
        RealResponse response = new RealResponse();
        try {
            response.code = conn.getResponseCode();
            if (HttpUtils.needRedirects(response.code)) {
                response.location = HttpUtils.getLocation(conn, sRequestURL);
            }
            response.contentLength = conn.getContentLength();
            response.date = conn.getDate();
            // 当 ResponseCode 小于 HTTP_BAD_REQUEST（400）时，获取返回信息
            if (response.code < HttpURLConnection.HTTP_BAD_REQUEST) {
                response.result = HttpUtils.getRetString(conn.getInputStream());
            } else {
                response.errorMsg = HttpUtils.getRetString(conn.getErrorStream());
            }
        } catch (IOException e) {
            return getExceptionResponse(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
//        LogUtils.i(TAG, response.toString());
        return response;
    }

    /**
     * 发生异常时，返回包含异常信息的 RealResponse 对象
     *
     * @return RealResponse 异常信息
     */
    private RealResponse getExceptionResponse(Exception e) {
        RealResponse response = new RealResponse();
        response.exception = e;
        response.errorMsg = e.getMessage();
        return response;
    }
}