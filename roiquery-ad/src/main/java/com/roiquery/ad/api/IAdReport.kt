package com.roiquery.ad.api

 interface IAdReport {

    /**
     * 上报 广告入口
     *
     * @param id 广告最小单元id
     * @param type 广告类型
     * @param platform 广告平台
     * @param location 广告位
     * @param seq 系列行为标识
     * @param entrance 广告入口
     */
    fun reportEntrance(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        entrance: String? = "",
    )


    /**
     * 上报 广告展示请求
     *
     * @param id 广告最小单元id
     * @param type 广告类型
     * @param platform 广告平台
     * @param location 广告位
     * @param seq 系列行为标识
     * @param entrance 广告入口
     */
    fun reportToShow(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        entrance: String? = "",
    )
    /**
     * 上报 广告展示
     *
     * @param id 广告最小单元id
     * @param type 广告类型
     * @param platform 广告平台
     * @param location 广告位
     * @param seq 系列行为标识
     * @param entrance 广告入口
     */
    fun reportShow(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        entrance: String? = "",
    )
    /**
     * 上报 广告关闭
     *
     * @param id 广告最小单元id
     * @param type 广告类型
     * @param platform 广告平台
     * @param location 广告位
     * @param seq 系列行为标识
     * @param entrance 广告入口
     */
    fun reportClose(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        entrance: String? = "",
    )
    /**
     * 上报 广告点击
     *
     * @param id 广告最小单元id
     * @param type 广告类型
     * @param platform 广告平台
     * @param location 广告位
     * @param seq 系列行为标识
     * @param entrance 广告入口
     */
    fun reportClick(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        entrance: String? = "",
    )
    /**
     * 上报 激励广告已获得奖励
     *
     * @param id 广告最小单元id
     * @param type 广告类型
     * @param platform 广告平台
     * @param location 广告位
     * @param seq 系列行为标识
     * @param entrance 广告入口
     */
    fun reportRewarded(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        entrance: String? = "",
    )
    /**
     * 上报 访问广告链接，离开当前app(页面)
     *
     * @param id 广告最小单元id
     * @param type 广告类型
     * @param platform 广告平台
     * @param location 广告位
     * @param seq 系列行为标识
     * @param entrance 广告入口
     */
    fun reportLeftApp(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        entrance: String? = "",
    )

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
         entrance: String? = "",
     )


     /**
      * 上报 广告展示价值
      *
      * @param id 广告最小单元id
      * @param type 广告类型
      * @param platform 广告平台
      * @param location 广告位
      * @param seq 系列行为标识
      * @param mediationId 聚合平台广告id
      * @param mediation 聚合平台
      * @param value 价值
      * @param currency 货币
      * @param precision 精确度
      * @param country 国家
      * @param entrance 广告入口
      */
     fun reportImpression(
         id: String,
         type: String,
         platform: String,
         location: String,
         seq: String,
         mediation: Int,
         mediationId: String,
         value: String,
         currency: String,
         precision: String,
         country: String,
         entrance: String? = "",
     )

     /**
     * 上报 访问广告链接，回到当前app(页面)
     *
     */
    fun reportReturnApp()


}