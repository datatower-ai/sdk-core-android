package com.roiquery.analytics

import android.content.Context
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
         */
        @JvmStatic
        @JvmOverloads
        fun initSDK(
            context: Context?,
            appId: String?,
            channel: String = "",
            isDebug: Boolean = false,
            logLevel: Int = LogUtils.V,
            commonProperties: JSONObject = JSONObject()
        ) {
            ROIQueryAnalytics.init(context,
                AnalyticsConfig(appId)
                    .setDebug(isDebug, logLevel)
                    .setChannel(channel)
                    .addCommonProperties(commonProperties)
            )
        }


        /**
         * 初始化,唯一入口
         *
         * @param context 上下文
         * @param appId 应用id，后台分配
         * @param isDebug 是否打开调试，调试模式下将打印log,默认关闭
         */
        @JvmStatic
        fun initSDK(
            context: Context?,
            appId: String?,
            isDebug: Boolean = false,
        ){
            this.initSDK(
                context,
                appId,
                channel = "",
                isDebug,
                LogUtils.V,
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
         */
        @JvmStatic
        fun initSDK(
            context: Context?,
            appId: String?,
            isDebug: Boolean = false,
            logLevel: Int = LogUtils.V,
        ){
            this.initSDK(
                context,
                appId,
                channel = "",
                isDebug,
                logLevel,
                JSONObject()
            )
        }

        fun enableThirdShare(type: ThirdSDKShareType) {
            try {
            ThirdPartShareDataFactory.createThirdInstance(type).
                    synThirdDTIdData(PropertyManager.instance.getDTID())
            }catch (error:Exception){
                LogUtils.d("please impl ${type.name}")
            }

        }

    }
}