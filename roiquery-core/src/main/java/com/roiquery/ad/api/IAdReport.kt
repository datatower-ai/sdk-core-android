package com.roiquery.ad.api

import com.roiquery.ad.AdMediation

interface IAdReport {

     fun reportLoadBegin(
         id: String,
         type: Int,
         platform: Int,
         seq: String,
         properties: MutableMap<String, Any>? = mutableMapOf(),
         mediation: Int,
         mediationId: String,
     )

     fun reportLoadEnd(
         id: String,
         type: Int,
         platform: Int,
         duration: Long,
         result: Boolean,
         seq: String,
         errorCode: Int = 0,
         errorMessage: String = "",
         properties: MutableMap<String, Any>? = mutableMapOf(),
         mediation: Int,
         mediationId: String,
     )


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
    fun reportEntrance(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        properties: MutableMap<String, Any>? = mutableMapOf(),
        entrance: String? = "",
        mediation: Int,
        mediationId: String,
    )


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
    fun reportToShow(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        properties: MutableMap<String, Any>? = mutableMapOf(),
        entrance: String? = "",
        mediation: Int,
        mediationId: String,
    )
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
    fun reportShow(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        properties: MutableMap<String, Any>? = mutableMapOf(),
        entrance: String? = "",
        mediation: Int,
        mediationId: String,
    )

     /**
      * 上报 广告展示失败
      *
      * @param id 广告最小单元id
      * @param type 广告类型
      * @param platform 广告平台
      * @param location 广告位
      * @param seq 系列行为标识
      * @param errorCode 错误码
      * @param errorMessage 错误信息
      * @param properties 额外事件属性
      */
     fun reportShowFailed(
         id: String,
         type: Int,
         platform: Int,
         location: String,
         seq: String,
         errorCode: Int,
         errorMessage: String,
         properties: MutableMap<String, Any>? = mutableMapOf(),
         entrance: String? = "",
         mediation: Int,
         mediationId: String,
     )

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
     fun reportImpression(
         id: String,
         type: Int,
         platform: Int,
         location: String,
         seq: String,
         properties: MutableMap<String, Any>? = mutableMapOf(),
         entrance: String? = "",
         mediation: Int,
         mediationId: String,
     )
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
     fun reportOpen(
         id: String,
         type: Int,
         platform: Int,
         location: String,
         seq: String,
         properties: MutableMap<String, Any>? = mutableMapOf(),
         entrance: String? = "",
         mediation: Int,
         mediationId: String,
     )
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
    fun reportClose(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        properties: MutableMap<String, Any>? = mutableMapOf(),
        entrance: String? = "",
        mediation: Int,
        mediationId: String,
    )
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
    fun reportClick(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        properties: MutableMap<String, Any>? = mutableMapOf(),
        entrance: String? = "",
        mediation: Int,
        mediationId: String,
    )
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
    fun reportRewarded(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        properties: MutableMap<String, Any>? = mutableMapOf(),
        entrance: String? = "",
        mediation: Int,
        mediationId: String,
    )

    /**
     * 上报 自定义转化
     *
     * @param id 广告最小单元id
     * @param type 广告类型
     * @param platform 广告平台
     * @param location 广告位
     * @param seq 系列行为标识
     * @param conversionSource 转化来源
     * @param properties 额外事件属性
     * @param entrance 广告入口
     */
    fun reportConversion(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        conversionSource: String,
        properties: MutableMap<String, Any>? = mutableMapOf(),
        entrance: String? = "",
        mediation: Int,
        mediationId: String,
    )
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
    fun reportLeftApp(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        properties: MutableMap<String, Any>? = mutableMapOf(),
        entrance: String? = "",
        mediation: Int,
        mediationId: String,
    )

     /**
      * 上报 广告展示价值，用于单独广告平台
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
     fun reportPaid(
         id: String,
         type: Int,
         platform: Int,
         location: String,
         seq: String,
         value: String,
         currency: String,
         precision: String,
         properties: MutableMap<String, Any>? = mutableMapOf(),
         entrance: String? = "",
         mediation: Int,
         mediationId: String,
     )


     /**
      * 上报 广告展示价值，用于聚合广告平台
      *
      * @param id 广告最小单元id
      * @param type 广告类型
      * @param platform 广告平台
      * @param adgroupType 广告组名称
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
     fun reportPaid(
         id: String,
         type: Int,
         platform: String,
         adgroupName: String,
         adgroupType: String,
         location: String,
         seq: String,
         mediation: Int,
         mediationId: String,
         value: String,
         currency: String,
         precision: String,
         country: String,
         properties: MutableMap<String, Any>? = mutableMapOf(),
         entrance: String? = ""
     )


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
     fun reportPaid(
         id: String,
         type: Int,
         platform: Int,
         location: String,
         seq: String,
         mediation: Int,
         mediationId: String,
         value: String,
         precision: String,
         country: String,
         properties: MutableMap<String, Any>? = mutableMapOf(),
     )

     /**
     * 上报 访问广告链接，回到当前app(页面)
     *
     */
    fun reportReturnApp()


}