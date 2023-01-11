package com.roiquery.ad

import com.roiquery.ad.api.AdReportImp
import com.roiquery.ad.utils.AdPlatformUtils
import com.roiquery.ad.utils.UUIDUtils

open class DTAdReport {
    companion object {
        /**
         * 上报 广告展示
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param properties 额外事件属性
         */
        @JvmOverloads
        @JvmStatic
        fun reportShow(
            id: String,
            type: AdType,
            platform: AdPlatform,
            properties: MutableMap<String, Any>? = mutableMapOf(),
        ) = AdReportImp.getInstance().reportShow(id, type.value, platform.value, properties)


        /**
         * 上报 自定义转化
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param properties 额外事件属性
         */
        @JvmOverloads
        @JvmStatic
        fun reportConversion(
            id: String,
            type: AdType,
            platform: AdPlatform,
            properties: MutableMap<String, Any>? = mutableMapOf(),
        ) = AdReportImp.getInstance().reportConversion(id, type.value, platform.value, properties)


        /**
         * 生成UUID
         */
        @JvmStatic
        fun generateUUID() = UUIDUtils.generateUUID()


    }
}