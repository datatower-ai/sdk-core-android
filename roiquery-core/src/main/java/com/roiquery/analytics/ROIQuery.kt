package com.roiquery.analytics

import android.content.Context
import android.util.Log
import com.roiquery.analytics.api.AnalyticsImp
import com.roiquery.analytics.config.AnalyticsConfig
import org.json.JSONObject

@Deprecated("this class will be remove, Replace With \'DT\'", ReplaceWith("DT"))
class ROIQuery {
    companion object {

        /**
         * 初始化,唯一入口
         *
         * @param context 上下文
         * @param appId 应用id，后台分配
         * @param serverUrl 服务器地址,后台分配
         * @param channel 渠道，默认为空字符串，可用 ROIQueryChannel.GP，具体联系商务
         * @param isDebug 是否打开调试，调试模式下将打印log,默认关闭
         * @param logLevel log 的级别，默认为 Log.VERBOSE，仅在 isDebug = true 有效
         * @param serverUrl 服务器地址, 默认DTServer.URL1
         * @param initializeListener 初始化回调
         */
        @JvmStatic
        @JvmOverloads
        fun initSDK(
            context: Context,
            appId: String,
            serverUrl: String,
            channel: String = "",
            isDebug: Boolean = false,
            logLevel: Int = Log.VERBOSE,
            initializeListener: InitCallback? = null,
            commonProperties: JSONObject = JSONObject(),
        ) {
            AnalyticsImp.init(
                context,
                AnalyticsConfig(appId)
                    .setServerUrl(serverUrl)
                    .setDebug(isDebug, logLevel)
                    .setChannel(channel)
                    .addCommonProperties(commonProperties),
                initializeListener
            )
        }
    }
}