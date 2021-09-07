package com.roiquery.analytics.utils

/*
 * Created by dengshiwei on 2019/06/03.
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

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.annotation.RequiresApi

import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


object NetworkUtils {
    /**
     * HTTP 状态码 307
     */
    private const val HTTP_307 = 307

    /**
     * 获取userAgent
     */
     fun getUserAgent(context: Context): String {
        var ua: String = ""
        try {
            if (Build.VERSION.SDK_INT < 19) {
                val web = WebView(context)
                ua = web.settings.userAgentString
                web.destroy()
            } else {
                ua = WebSettings.getDefaultUserAgent(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ua
    }

    /**
     * 是否有可用网络
     *
     * @param context Context
     * @return true：网络可用，false：网络不可用
     */
    @SuppressLint("WrongConstant")
    fun isNetworkAvailable(context: Context): Boolean {
        // 检测权限
        return if (!DeviceUtils.checkHasPermission(
                context,
                Manifest.permission.ACCESS_NETWORK_STATE
            )
        ) {
            false
        } else try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            isNetworkAvailable(cm)
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
            false
        }
    }

    @SuppressLint("NewApi", "WrongConstant")
    fun isNetworkValid(capabilities: NetworkCapabilities?): Boolean {
        return if (capabilities != null) {
            (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    || capabilities.hasTransport(7) //目前已知在车联网行业使用该标记作为网络类型（TBOX 网络类型）
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                    || capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
        } else false
    }

    /**
     * 判断网络是否可用
     *
     * @param connectivityManager ConnectivityManager
     * @return true：可用；false：不可用
     */
    private fun isNetworkAvailable(connectivityManager: ConnectivityManager?): Boolean {
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork
                if (network != null) {
                    val capabilities = connectivityManager.getNetworkCapabilities(network)
                    if (capabilities != null) {
                        return isNetworkValid(capabilities)
                    }
                }
            } else {
                val networkInfo = connectivityManager.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnected
            }
        }
        return false
    }

    /**
     * 判断当前网络是否是 wifi
     *
     * @param connectivityManager ConnectivityManager
     * @return true：是 wifi；false：不是 wifi
     */
    private fun isWiFiNetwork(connectivityManager: ConnectivityManager): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            if (network != null) {
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                if (capabilities != null) {
                    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                }
            }
        } else {
            val networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            return networkInfo != null && networkInfo.isConnectedOrConnecting
        }
        return false
    }

}