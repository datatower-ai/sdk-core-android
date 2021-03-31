package com.roiquery.iap.api

interface IIapReport {

    /**
     * 展示购买入口的时候上报
     *
     * @param sku 商品ID
     * @param price 价格， 如 9.99
     * @param currency 货币，如usd
     * @param entrance 入口，可为空
     */
    fun reportEntrance(
        sku: String,
        price: Double,
        currency: String,
        entrance: String? = "",
    )

    /**
     * 点击购买的时候上报
     *
     * @param sku 商品ID
     * @param price 价格， 如 9.99
     * @param currency 货币，如usd
     * @param entrance 入口，可为空
     */
    fun reportToPurchase(
        sku: String,
        price: Double,
        currency: String,
        entrance: String? = "",
    )

    /**
     * 购买成功的时候上报，无论是否消耗
     *
     * @param sku 商品ID
     * @param price 价格， 如 9.99
     * @param currency 货币，如usd
     * @param entrance 入口，可为空
     */
    fun reportPurchased(
        sku: String,
        price: Double,
        currency: String,
        entrance: String? = "",
    )

    /**
     * 购买失败的时候上报
     *
     * @param sku 商品ID
     * @param price 价格， 如 9.99
     * @param currency 货币，如usd
     * @param code 错误码
     * @param entrance 入口，可为空
     * @param msg 额外信息，可为空
     */
    fun reportNotToPurchased(
        sku: String,
        price: Double,
        currency: String,
        code:String,
        entrance: String? = "",
        msg: String? = "",
    )
}