package ai.datatower.ias

import ai.datatower.analytics.DTAnalytics
import org.json.JSONObject

/**
 * author: xiaosailing
 * date: 2022-02-24
 * description:
 * version：1.0
 */
internal class DTIasReportImp {
    companion object {

        fun trackIasSubSuccessEvent(
            originalOrderId: String,
            orderId: String,
            sku: String,
            price: Double,
            currency: String,
            properties: MutableMap<String, Any>? = mutableMapOf()
        ) {
            DTAnalytics.trackInternal(
                DTIasConstant.IAS_TO_SUBSCRIBE_SUCCESS_EVENT,
                JSONObject(properties?.toMutableMap() ?: mutableMapOf<String, Any?>()).apply {
                    put(DTIasConstant.IAS_ORIGINAL_ORDER_ID, originalOrderId)
                    put(DTIasConstant.IAS_ORDER_ID, orderId)
                    put(DTIasConstant.IAS_SKU, sku)
                    put(DTIasConstant.IAS_PRICE, price)
                    put(DTIasConstant.IAS_CURRENCY, currency)
                }
            )
        }
    }
}


