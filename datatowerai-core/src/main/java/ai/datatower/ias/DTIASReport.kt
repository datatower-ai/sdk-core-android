package ai.datatower.ias

import ai.datatower.ad.utils.UUIDUtils

/**
 * author: xiaosailing
 * date: 2022-02-24
 * description:
 * version：1.0
 */
open class DTIASReport {
    companion object {

        /**
         * 订阅成功上报
         *
         * @param originalOrderId 原始订阅订单id
         * @param orderId 订阅订单id
         * @param sku 商品ID
         * @param price 价格， 如 9.9
         * @param currency 货币类型，如usd
         * @param properties 自定义属性
         */
        @JvmStatic
        @JvmOverloads
        fun reportSubscribeSuccess(
            originalOrderId: String,
            orderId: String,
            sku: String,
            price: Double,
            currency: String,
            properties: MutableMap<String, Any>? = mutableMapOf()
        ) {
            ROIQueryIasReportImp.trackIasSubSuccessEvent(originalOrderId, orderId, sku, price, currency, properties)
        }


        /**
         * 生成UUID
         */
        @JvmStatic
        fun generateUUID() = UUIDUtils.generateUUID()

    }
}