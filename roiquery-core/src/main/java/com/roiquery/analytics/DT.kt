package com.roiquery.analytics

import android.content.Context
import com.roiquery.analytics.api.AnalyticsImp
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.utils.LogUtils
import org.json.JSONObject

class DT {
    companion object {

        /**
         * 初始化,唯一入口
         *
         * @param context 上下文
         * @param appId 应用id，后台分配
         * @param serverUrl 服务器地址,后台分配
         * @param channel 渠道，可用 ROIQueryChannel.GP、ROIQueryChannel.APPSTORE
         * @param isDebug 是否打开调试，调试模式下将打印log,默认关闭
         * @param logLevel log 的级别，默认为 LogUtils.V，仅在 isDebug = true 有效
         * @param initCallback 初始化回调
         */
        @JvmStatic
        @JvmOverloads
        fun initSDK(
            context: Context,
            appId: String,
            serverUrl: String,
            channel: String = "",
            isDebug: Boolean = false,
            logLevel: Int = LogUtils.V,
            initCallback: InitCallback? = null,
            commonProperties: JSONObject = JSONObject(),
        ) {
            AnalyticsImp.init(
                context,
                AnalyticsConfig(appId)
                    .setServerUrl(serverUrl)
                    .setDebug(isDebug, logLevel)
                    .setChannel(channel)
                    .addCommonProperties(commonProperties),
                initCallback
            )
        }

    }
}