package com.nodetower.base.utils

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
     * 获取网络类型
     *
     * @param context Context
     * @return 网络类型
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun networkType(context: Context): String {
        return try {
            // 检测权限
            if (!DeviceUtils.checkHasPermission(
                    context,
                    Manifest.permission.ACCESS_NETWORK_STATE
                )
            ) {
                return "NULL"
            }
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager != null) {
                // 网络不可用返回 NULL
                if (!isNetworkAvailable(connectivityManager)) {
                    return "NULL"
                }
                // WIFI 网络
                if (isWiFiNetwork(connectivityManager)) {
                    return "WIFI"
                }
            }
            //读取移动网络类型
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            mobileNetworkType(context, telephonyManager, connectivityManager)
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
            "NULL"
        }
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

    /**
     * 判断指定网络类型是否可以上传数据
     *
     * @param networkType 网络类型
     * @param flushNetworkPolicy 上传策略
     * @return true：可以上传，false：不可以上传
     */
    fun isShouldFlush(networkType: String, flushNetworkPolicy: Int): Boolean {
        return toNetworkType(networkType) and flushNetworkPolicy != 0
    }

    private fun toNetworkType(networkType: String): Int {
        if ("NULL" == networkType) {
            return NetworkType.TYPE_ALL
        } else if ("WIFI" == networkType) {
            return NetworkType.TYPE_WIFI
        } else if ("2G" == networkType) {
            return NetworkType.TYPE_2G
        } else if ("3G" == networkType) {
            return NetworkType.TYPE_3G
        } else if ("4G" == networkType) {
            return NetworkType.TYPE_4G
        } else if ("5G" == networkType) {
            return NetworkType.TYPE_5G
        }
        return NetworkType.TYPE_ALL
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

    fun needRedirects(responseCode: Int): Boolean {
        return responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HTTP_307
    }

    @Throws(MalformedURLException::class)
    fun getLocation(connection: HttpURLConnection?, path: String?): String? {
        if (connection == null || TextUtils.isEmpty(path)) {
            return null
        }
        var location = connection.getHeaderField("Location")
        if (TextUtils.isEmpty(location)) {
            location = connection.getHeaderField("location")
        }
        if (TextUtils.isEmpty(location)) {
            return null
        }
        if (!(location.startsWith("http://") || location
                .startsWith("https://"))
        ) {
            //某些时候会省略host，只返回后面的path，所以需要补全url
            val originUrl = URL(path)
            location = (originUrl.protocol + "://"
                    + originUrl.host + location)
        }
        return location
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

    /**
     * 获取当前移动网络类型
     *
     * @param telephonyManager TelephonyManager
     * @param connectivityManager ConnectivityManager
     * @return 移动网络类型
     */
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    private fun mobileNetworkType(
        context: Context,
        telephonyManager: TelephonyManager?,
        connectivityManager: ConnectivityManager?
    ): String {
        // Mobile network
        var networkType = TelephonyManager.NETWORK_TYPE_UNKNOWN
        if (telephonyManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                && (DeviceUtils.checkHasPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) || telephonyManager.hasCarrierPrivileges())
            ) {
                networkType = telephonyManager.dataNetworkType
            } else {
                try {
                    networkType = telephonyManager.dataNetworkType
                } catch (ex: Exception) {
//                    LogUtils.printStackTrace(ex)
                }
            }
        }
        if (networkType == TelephonyManager.NETWORK_TYPE_UNKNOWN) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // 在 Android 11 平台上，没有 READ_PHONE_STATE 权限时
                return "NULL"
            }
            if (connectivityManager != null) {
                val networkInfo = connectivityManager.activeNetworkInfo
                if (networkInfo != null) {
                    networkType = networkInfo.subtype
                }
            }
        }
        when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> return "2G"
            TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD, TelephonyManager.NETWORK_TYPE_HSPAP -> return "3G"
            TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> return "4G"
            TelephonyManager.NETWORK_TYPE_NR -> return "5G"
        }
        return "NULL"
    }
}