package com.roiquery.iap.api

interface IIapReport {

    /**
     * 展示购买入口的时候上报
     *
     * @param order 订单
     * @param sku 商品ID
     * @param price 价格， 如 9.99
     * @param currency 货币，如usd
     * @param seq 系列行为标识
     * @param placement 位置，可为空
     */
    fun reportEntrance(
        order: String,
        sku: String,
        price: Double,
        currency: String,
        seq: String,
        placement: String? = "",
    )

    /**
     * 点击购买的时候上报
     *
     * @param order 订单
     * @param sku 商品ID
     * @param price 价格， 如 9.99
     * @param currency 货币，如usd
     * @param seq 系列行为标识
     * @param placement 位置，可为空
     */
    fun reportToPurchase(
        order: String,
        sku: String,
        price: Double,
        currency: String,
        seq: String,
        placement: String? = "",
    )

    /**
     * 购买成功的时候上报，无论是否消耗
     *
     * @param order 订单
     * @param sku 商品ID
     * @param price 价格， 如 9.99
     * @param currency 货币，如usd
     * @param seq 系列行为标识
     * @param placement 位置，可为空
     */
    fun reportPurchased(
        order: String,
        sku: String,
        price: Double,
        currency: String,
        seq: String,
        placement: String? = "",
    )

    /**
     * 购买失败的时候上报
     *
     * @param order 订单
     * @param sku 商品ID
     * @param price 价格， 如 9.99
     * @param currency 货币，如usd
     * @param seq 系列行为标识
     * @param code 错误码
     * @param placement 位置，可为空
     * @param msg 额外信息，可为空
     */
    fun reportNotToPurchased(
        order: String,
        sku: String,
        price: Double,
        currency: String,
        seq: String,
        code:String,
        placement: String? = "",
        msg: String? = "",
    )
}