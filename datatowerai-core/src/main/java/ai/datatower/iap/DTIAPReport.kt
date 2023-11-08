package ai.datatower.iap

import ai.datatower.ad.utils.UUIDUtils
import ai.datatower.iap.api.IAPReportImp


open class DTIAPReport {
    companion object {

        /**
         * 购买成功上报
         *
         * @param order 订单
         * @param sku 商品ID
         * @param price 价格， 如 9.9
         * @param currency 货币，如usd
         * @param properties 自定义属性
         */
        @JvmStatic
        @JvmOverloads
        fun reportPurchaseSuccess(
            order: String,
            sku: String,
            price: Double,
            currency: String,
            properties: MutableMap<String, Any>? = mutableMapOf()
        ) = IAPReportImp.getInstance().reportPurchased(order, sku, price, currency, properties)


        /**
         * 生成UUID
         */
        @JvmStatic
        fun generateUUID() = UUIDUtils.generateUUID()

    }



}