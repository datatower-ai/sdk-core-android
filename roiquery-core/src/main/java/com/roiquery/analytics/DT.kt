package com.roiquery.analytics

import android.content.Context
import com.roiquery.analytics.api.AnalyticsImp
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.core.PropertyManager
import com.roiquery.analytics.utils.LogUtils
import com.roiquery.thirdparty.ThirdPartShareDataFactory
import com.roiquery.thirdparty.ThirdSDKShareType
import kotlinx.coroutines.delay
import org.json.JSONObject

class DT {
    companion object {

        /**
         * 初始化,唯一入口
         *
         * @param context 上下文
         * @param appId 应用id，后台分配
         * @param channel 渠道，可用 ROIQueryChannel.GP、ROIQueryChannel.APPSTORE
         * @param isDebug 是否打开调试，调试模式下将打印log,默认关闭
         * @param logLevel log 的级别，默认为 LogUtils.V，仅在 isDebug = true 有效
         * @param serverUrl: Int = DTServer.URL1,
         * @param sdkInitialSuccess 初始化成功回调
         * @param sdkInitialFail 初始化失败回调
         */
        @JvmStatic
        @JvmOverloads
        fun initSDK(
            context: Context,
            appId: String,
            channel: String = "",
            isDebug: Boolean = false,
            logLevel: Int = LogUtils.V,
            serverUrl: Int = DTServer.URL1,
            sdkInitialSuccess: () -> Unit = fun() {},
            sdkInitialFail: () -> Unit = fun() {},
            commonProperties: JSONObject = JSONObject(),
        ) {
            AnalyticsImp.init(
                context,
                AnalyticsConfig(appId)
                    .setServerUrl(serverUrl)
                    .setDebug(isDebug, logLevel)
                    .setChannel(channel)
                    .addCommonProperties(commonProperties),
                initSuccess = sdkInitialSuccess,
                initFail = sdkInitialFail
            )
        }


        /**
         * 初始化,唯一入口
         *
         * @param context 上下文
         * @param appId 应用id，后台分配
         * @param isDebug 是否打开调试，调试模式下将打印log,默认关闭
         * @param sdkInitialSuccess 初始化成功回调
         * @param sdkInitialFail 初始化失败回调
         */
        @JvmStatic
        fun initSDK(
            context: Context,
            appId: String,
            isDebug: Boolean = false,
            sdkInitialSuccess: () -> Unit = fun() {},
            sdkInitialFail: () -> Unit = fun() {}
        ) {
            this.initSDK(
                context,
                appId,
                channel = "",
                isDebug,
                LogUtils.V,
                serverUrl = DTServer.URL1,
                sdkInitialSuccess,
                sdkInitialFail,
                JSONObject()
            )
        }

        /**
         * 初始化,唯一入口
         *
         * @param context 上下文
         * @param appId 应用id，后台分配
         * @param isDebug 是否打开调试，调试模式下将打印log,默认关闭
         * @param logLevel log 的级别，默认为 LogUtils.V，仅在 isDebug = true 有效
         * @param sdkInitialSuccess 初始化成功回调
         * @param sdkInitialFail 初始化失败回调
         */
        @JvmStatic
        fun initSDK(
            context: Context,
            appId: String,
            isDebug: Boolean = false,
            logLevel: Int = LogUtils.V,
            sdkInitialSuccess: () -> Unit = fun() {},
            sdkInitialFail: () -> Unit = fun() {}
        ) {
            this.initSDK(
                context,
                appId,
                channel = "",
                isDebug,
                logLevel,
                serverUrl = DTServer.URL1,
                sdkInitialSuccess,
                sdkInitialFail,
                JSONObject()
            )
        }


        /**
         * 初始化,唯一入口
         *
         * @param context 上下文
         * @param appId 应用id，后台分配
         * @param isDebug 是否打开调试，调试模式下将打印log,默认关闭
         * @param logLevel log 的级别，默认为 LogUtils.V，仅在 isDebug = true 有效
         * @param serverUrl 服务器地址
         * @param sdkInitialSuccess 初始化成功回调
         * @param sdkInitialFail 初始化失败回调
         */
        @JvmStatic
        fun initSDK(
            context: Context,
            appId: String,
            isDebug: Boolean = false,
            logLevel: Int = LogUtils.V,
            serverUrl: Int = DTServer.URL1,
            sdkInitialSuccess: () -> Unit = fun() {},
            sdkInitialFail: () -> Unit = fun() {}
        ) {
            this.initSDK(
                context,
                appId,
                channel = "",
                isDebug,
                logLevel,
                serverUrl,
                sdkInitialSuccess,
                sdkInitialFail,
                JSONObject()
            )
        }

        @JvmStatic
        fun enableThirdShare(type: ThirdSDKShareType) {
            try {
                ThirdPartShareDataFactory.createThirdInstance(type)
                    .synThirdDTIdData(PropertyManager.instance.getDTID())
            } catch (error: Exception) {
                LogUtils.d("please impl ${type.name}")
            }

        }

    }
}