package com.roiquery.ad

import com.roiquery.ad.api.AdReportImp
import com.roiquery.ad.utils.UUIDUtils

open class DTAdReport {
    companion object {



        /**
         * 上报 广告开始加载
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param seq 系列行为标识
         * @param properties 自定义事件属性
         * @param mediation 聚合平台
         * @param mediationId 聚合平台广告id
         */
        @JvmOverloads
        @JvmStatic
        fun reportLoadBegin(
            id: String,
            type: AdType,
            platform: AdPlatform,
            seq: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
            mediation: AdMediation = AdMediation.IDLE,
            mediationId: String = "",
        ) = AdReportImp.getInstance()
            .reportLoadBegin(id, type.value, platform.value,  seq, properties, mediation.value, mediationId)




        /**
         * 上报 广告结束加载
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param duration 广告加载时长
         * @param result 广告加载结果
         * @param seq 系列行为标识
         * @param errorCode 失败错误码
         * @param errorMessage 失败错误信息
         * @param properties 自定义事件属性
         * @param mediation 聚合平台
         * @param mediationId 聚合平台广告id
         */
        @JvmOverloads
        @JvmStatic
        fun reportLoadEnd(
            id: String,
            type: AdType,
            platform: AdPlatform,
            duration: Long,
            result: Boolean,
            seq: String,
            errorCode: Int = 0,
            errorMessage: String = "",
            properties: MutableMap<String, Any>? = mutableMapOf(),
            mediation: AdMediation = AdMediation.IDLE,
            mediationId: String = "",
        ) = AdReportImp.getInstance()
            .reportLoadEnd(id, type.value, platform.value, duration, result, seq,errorCode, errorMessage, properties, mediation.value, mediationId)



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
         * @param mediation 聚合平台
         * @param mediationId 聚合平台广告id
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
            entrance: String? = "",
            mediation: AdMediation = AdMediation.IDLE,
            mediationId: String = "",
        ) = AdReportImp.getInstance()
            .reportToShow(id, type.value, platform.value, location, seq, properties, entrance, mediation.value, mediationId)

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
         * @param mediation 聚合平台
         * @param mediationId 聚合平台广告id
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
            entrance: String? = "",
            mediation: AdMediation = AdMediation.IDLE,
            mediationId: String = "",
        ) = AdReportImp.getInstance()
            .reportShow(id, type.value, platform.value, location, seq, properties, entrance, mediation.value, mediationId)

        /**
         * 上报 广告展示失败
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param properties 额外事件属性
         * @param entrance 广告入口
         * @param mediation 聚合平台
         * @param mediationId 聚合平台广告id
         */
        @JvmOverloads
        @JvmStatic
        fun reportShowFailed(
            id: String,
            type: AdType,
            platform: AdPlatform,
            location: String,
            seq: String,
            errorCode: Int,
            errorMessage: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
            entrance: String? = "",
            mediation: AdMediation = AdMediation.IDLE,
            mediationId: String = "",
        ) = AdReportImp.getInstance()
            .reportShowFailed(id, type.value, platform.value, location, seq, errorCode,errorMessage,properties,entrance, mediation.value, mediationId)



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
         * @param mediation 聚合平台
         * @param mediationId 聚合平台广告id
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
            entrance: String? = "",
            mediation: AdMediation = AdMediation.IDLE,
            mediationId: String = "",
        ) = AdReportImp.getInstance()
            .reportClose(id, type.value, platform.value, location, seq, properties, entrance, mediation.value, mediationId)

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
         * @param mediation 聚合平台
         * @param mediationId 聚合平台广告id
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
            entrance: String? = "",
            mediation: AdMediation = AdMediation.IDLE,
            mediationId: String = "",
        ) = AdReportImp.getInstance()
            .reportClick(id, type.value, platform.value, location, seq, properties, entrance, mediation.value, mediationId)

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
         * @param mediation 聚合平台
         * @param mediationId 聚合平台广告id
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
            entrance: String? = "",
            mediation: AdMediation = AdMediation.IDLE,
            mediationId: String = "",
        ) = AdReportImp.getInstance()
            .reportRewarded(id, type.value, platform.value, location, seq, properties, entrance, mediation.value, mediationId)


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
         * @param mediation 聚合平台
         * @param mediationId 聚合平台广告id
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
            entrance: String? = "",
            mediation: AdMediation = AdMediation.IDLE,
            mediationId: String = "",
        ) = AdReportImp.getInstance()
            .reportConversion(id, type.value, platform.value, location, seq,
                AD_CONVERSION_SOURCE.CLICK, properties,
                entrance, mediation.value, mediationId)



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
         * @param mediation 聚合平台
         * @param mediationId 聚合平台广告id
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
            entrance: String? = "",
            mediation: AdMediation = AdMediation.IDLE,
            mediationId: String = "",
        ) = AdReportImp.getInstance()
            .reportConversion(id, type.value, platform.value, location, seq,
                AD_CONVERSION_SOURCE.LEFT_APP, properties, entrance,
                mediation.value, mediationId)




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
         * @param mediation 聚合平台
         * @param mediationId 聚合平台广告id
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
            entrance: String? = "",
            mediation: AdMediation = AdMediation.IDLE,
            mediationId: String = "",
        ) = AdReportImp.getInstance()
            .reportConversion(id, type.value, platform.value, location, seq,
                AD_CONVERSION_SOURCE.REWARDED, properties, entrance,
                mediation.value, mediationId)



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
         * @param mediation 聚合平台
         * @param mediationId 聚合平台广告id
         */
        @JvmOverloads
        @JvmStatic
        fun reportPaid(
            id: String,
            type: AdType,
            platform: AdPlatform,
            location: String,
            seq: String,
            value: Double,
            currency: String,
            precision: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
            entrance: String? = "",
            mediation: AdMediation = AdMediation.IDLE,
            mediationId: String = "",
        ) = AdReportImp.getInstance()
            .reportPaid(id, type.value, platform.value, location, seq, value, currency, precision, properties, entrance, mediation.value, mediationId)

        /**
         * 上报 广告展示价值，用于聚合广告平台MAX
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param mediation 聚合平台
         * @param mediationId 聚合平台广告id
         * @param value 价值
         * @param precision 精确度
         * @param country 国家
         * @param properties 额外事件属性
         */
        @JvmOverloads
        @JvmStatic
        fun reportPaid(
            id: String,
            type: AdType,
            platform: AdPlatform,
            location: String,
            seq: String,
            mediation: AdMediation,
            mediationId: String,
            value: Double,
            precision: String,
            country: String,
            properties: MutableMap<String, Any>? = mutableMapOf(),
        ) = AdReportImp.getInstance()
            .reportPaid(id,type.value,platform.value,location,seq,mediation.value,mediationId, value, precision, country, properties)

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
         * @param mediation 聚合平台
         * @param mediationId 聚合平台广告id
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
            entrance: String? = "",
            mediation: AdMediation = AdMediation.IDLE,
            mediationId: String = "",
        ) = AdReportImp.getInstance()
            .reportLeftApp(id, type.value, platform.value, location, seq, properties, entrance, mediation.value, mediationId)
        

        /**
         * 生成UUID
         */
        @JvmStatic
        fun generateUUID() = UUIDUtils.generateUUID()


    }
}