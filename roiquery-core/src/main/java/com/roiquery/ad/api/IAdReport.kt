package com.roiquery.ad.api

 interface IAdReport {


    /**
     * 上报 广告展示
     *
     * @param id 广告最小单元id
     * @param type 广告类型
     * @param platform 广告平台
     * @param properties 额外事件属性
     */
    fun reportShow(
        id: String,
        type: Int,
        platform: Int,
        properties: MutableMap<String, Any>? = mutableMapOf()
    )



    /**
     * 上报 自定义转化
     *
     * @param id 广告最小单元id
     * @param type 广告类型
     * @param platform 广告平台
     * @param properties 额外事件属性
     */
    fun reportConversion(
        id: String,
        type: Int,
        platform: Int,
        properties: MutableMap<String, Any>? = mutableMapOf()
    )


}