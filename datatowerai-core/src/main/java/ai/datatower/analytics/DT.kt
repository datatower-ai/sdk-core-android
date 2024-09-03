package ai.datatower.analytics

import ai.datatower.analytics.api.AnalyticsImp
import ai.datatower.analytics.config.AnalyticsConfig
import ai.datatower.analytics.utils.PresetEvent
import android.content.Context
import android.util.Log
import org.json.JSONObject

class DT {
    companion object {

        /**
         * 初始化,唯一入口
         *
         * @param context 上下文
         * @param appId 应用id，后台分配
         * @param serverUrl 服务器地址,后台分配
         * @param channel 渠道，默认为空字符串，可用 DTChannel.GP，具体联系商务
         * @param isDebug 是否打开调试，调试模式下将打印log,默认关闭
         * @param logLevel log 的级别，默认为 Log.VERBOSE，仅在 isDebug = true 有效
         * @param manualEnableUpload 是否手动开启事件上报，默认 false；如需在预置事件中加入公共属性，需设为 true 后，
         * 再设置公共属性，再**手动调用 `DT.enableTrack()` 来开启上传**。
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
            manualEnableUpload: Boolean = false,
            commonProperties: JSONObject = JSONObject(),
        ) {
            AnalyticsImp.init(
                context,
                AnalyticsConfig.instance
                    .setContext(context.applicationContext)
                    .setAppId(appId)
                    .setServerUrl(serverUrl)
                    .setDebug(isDebug, logLevel)
                    .setChannel(channel)
                    .setManualEnableUpload(manualEnableUpload)
                    .addCommonProperties(commonProperties)
            )
        }

        @JvmStatic
        fun enableUpload() {
            AnalyticsConfig.instance.enableUpload()
        }

        @JvmStatic
        fun enableAutoTrack(event: PresetEvent) {
            AnalyticsConfig.instance.enableAutoTrack(event)
        }

        @JvmStatic
        fun disableAutoTrack(event: PresetEvent) {
            AnalyticsConfig.instance.disableAutoTrack(event)
        }
    }
}