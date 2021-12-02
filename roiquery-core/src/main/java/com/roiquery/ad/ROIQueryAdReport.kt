package com.roiquery.ad

import com.roiquery.ad.api.AdReportImp
import com.roiquery.ad.utils.AdPlatformUtils
import com.roiquery.ad.utils.UUIDUtils

open class ROIQueryAdReport {
    companion object {


        /**
         * 上报 广告入口
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param properties 额外事件属性
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportEntrance(
            id: String,
            type: AdType,
            platform: AdPlatform,
            location: String,
            seq: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportEntrance(id, type.value, platform.value, location, seq, properties, entrance)

        /**
         * 上报 广告展示请求
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param properties 额外事件属性
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportToShow(
            id: String,
            type: AdType,
            platform: AdPlatform,
            location: String,
            seq: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportToShow(id, type.value, platform.value, location, seq, properties, entrance)

        /**
         * 上报 广告展示
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param properties 额外事件属性
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportShow(
            id: String,
            type: AdType,
            platform: AdPlatform,
            location: String,
            seq: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportShow(id, type.value, platform.value, location, seq, properties, entrance)


        /**
         * 上报 广告曝光
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param properties 额外事件属性
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportImpression(
            id: String,
            type: AdType,
            platform: AdPlatform,
            location: String,
            seq: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportImpression(id, type.value, platform.value, location, seq, properties, entrance)


        /**
         * 上报 广告打开
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param properties 额外事件属性
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        @Deprecated("this method has be deprecated",ReplaceWith("reportLeftApp"),DeprecationLevel.HIDDEN)
        fun reportOpen(
            id: String,
            type: AdType,
            platform: AdPlatform,
            location: String,
            seq: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportOpen(id, type.value, platform.value, location, seq, properties, entrance)


        /**
         * 上报 广告关闭
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param properties 额外事件属性
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportClose(
            id: String,
            type: AdType,
            platform: AdPlatform,
            location: String,
            seq: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportClose(id, type.value, platform.value, location, seq, properties, entrance)

        /**
         * 上报 广告点击
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param properties 额外事件属性
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportClick(
            id: String,
            type: AdType,
            platform: AdPlatform,
            location: String,
            seq: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportClick(id, type.value, platform.value, location, seq, properties, entrance)

        /**
         * 上报 激励广告已获得奖励
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param properties 额外事件属性
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportRewarded(
            id: String,
            type: AdType,
            platform: AdPlatform,
            location: String,
            seq: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportRewarded(id, type.value, platform.value, location, seq, properties, entrance)


        /**
         * 上报 自定义转化，通过点击
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param properties 额外事件属性
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportConversionByClick(
            id: String,
            type: AdType,
            platform: AdPlatform,
            location: String,
            seq: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportConversion(id, type.value, platform.value, location, seq,
                AD_CONVERSION_SOURCE.CLICK, properties, entrance)



        /**
         * 上报 自定义转化，通过跳出app
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param properties 额外事件属性
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportConversionByLeftApp(
            id: String,
            type: AdType,
            platform: AdPlatform,
            location: String,
            seq: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportConversion(id, type.value, platform.value, location, seq,
                AD_CONVERSION_SOURCE.LEFT_APP, properties, entrance)


        /**
         * 上报 自定义转化，通过曝光
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param properties 额外事件属性
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportConversionByImpression(
            id: String,
            type: AdType,
            platform: AdPlatform,
            location: String,
            seq: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportConversion(id, type.value, platform.value, location, seq,
                AD_CONVERSION_SOURCE.IMPRESSION, properties, entrance)



        /**
         * 上报 自定义转化事件，通过获得激励
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param properties 额外事件属性
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportConversionByRewarded(
            id: String,
            type: AdType,
            platform: AdPlatform,
            location: String,
            seq: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportConversion(id, type.value, platform.value, location, seq,
                AD_CONVERSION_SOURCE.REWARDED, properties, entrance)



        /**
         * 上报 广告展示价值
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param value 价值
         * @param currency 货币
         * @param precision 精确度
         * @param properties 额外事件属性
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportPaid(
            id: String,
            type: AdType,
            platform: AdPlatform,
            location: String,
            seq: String,
            value: String,
            currency: String,
            precision: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportPaid(id, type.value, platform.value, location, seq, value, currency, precision, properties,entrance)

        /**
         * 上报 广告展示价值
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param adgroupType 广告组类别
         * @param location 广告位
         * @param seq 系列行为标识
         * @param mediation 聚合平台
         * @param mediationId 聚合平台广告id
         * @param value 价值
         * @param currency 货币
         * @param precision 精确度
         * @param country 国家
         * @param properties 额外事件属性
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportPaid(
            id: String,
            type: AdType,
            platform: String,
            adgroupType: String,
            location: String,
            seq: String,
            mediation: AdMediation,
            mediationId: String,
            value: String,
            currency: String,
            precision: String,
            country: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportPaid(id, type.value, platform, adgroupType, location, seq, mediation.value, mediationId, value, currency, precision, country, properties, entrance)

        /**
         * 上报 访问广告链接，离开当前app(页面)
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param properties 额外事件属性
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportLeftApp(
            id: String,
            type: AdType,
            platform: AdPlatform,
            location: String,
            seq: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportLeftApp(id, type.value, platform.value, location, seq, properties, entrance)

        /**
         * 上报 访问广告链接，回到当前app(页面)
         */
        @JvmStatic
        fun reportReturnApp() = AdReportImp.getInstance()
            .reportReturnApp()


        /**
         * 生成UUID
         */
        @JvmStatic
        fun generateUUID() = UUIDUtils.generateUUID()


        /**
         * 获取聚合平台具体广告网络的广告单元
         */
        @JvmStatic
        fun getPlatform(
            mediation: Int,
            networkName: String,
            networkPlacementId: String,
            adgroupType: String
        ) = AdPlatformUtils.getPlatform(mediation, networkName, networkPlacementId, adgroupType)

    }
}