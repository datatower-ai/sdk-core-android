package ai.datatower.iap.api

interface IIapReport {

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

}