package com.roiquery.iap.api

interface IIapReport {

    /**
     * 展示购买入口的时候上报
     *
     * @param order 订单
     * @param sku 商品ID
     * @param price 价格， 如 9.99
     * @param usdPrice 美元价格
     * @param currency 货币，如usd
     * @param entrance 入口，可为空
     */
    fun reportEntrance(
        order: String,
        sku: String,
        price: Double,
        usdPrice: Double,
        currency: String,
        entrance: String? = "",
    )

    /**
     * 点击购买的时候上报
     *
     * @param order 订单
     * @param sku 商品ID
     * @param price 价格， 如 9.99
     * @param usdPrice 美元价格
     * @param currency 货币，如usd
     * @param entrance 入口，可为空
     */
    fun reportToPurchase(
        order: String,
        sku: String,
        price: Double,
        usdPrice: Double,
        currency: String,
        entrance: String? = "",
    )

    /**
     * 购买成功的时候上报，无论是否消耗
     *
     * @param order 订单
     * @param sku 商品ID
     * @param price 价格， 如 9.99
     * @param usdPrice 美元价格
     * @param currency 货币，如usd
     * @param entrance 入口，可为空
     */
    fun reportPurchased(
        order: String,
        sku: String,
        price: Double,
        usdPrice: Double,
        currency: String,
        entrance: String? = "",
    )

    /**
     * 购买失败的时候上报
     *
     * @param order 订单
     * @param sku 商品ID
     * @param price 价格， 如 9.99
     * @param usdPrice 美元价格
     * @param currency 货币，如usd
     * @param code 错误码
     * @param entrance 入口，可为空
     * @param msg 额外信息，可为空
     */
    fun reportNotToPurchased(
        order: String,
        sku: String,
        price: Double,
        usdPrice: Double,
        currency: String,
        code:String,
        entrance: String? = "",
        msg: String? = "",
    )
}