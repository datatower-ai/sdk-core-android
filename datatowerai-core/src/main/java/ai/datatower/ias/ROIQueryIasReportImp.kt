package ai.datatower.ias

import ai.datatower.analytics.DTAnalytics
import org.json.JSONObject

/**
 * author: xiaosailing
 * date: 2022-02-24
 * description:
 * version：1.0
 */
internal class ROIQueryIasReportImp {
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
                ROIQueryIasConstant.IAS_TO_SUBSCRIBE_SUCCESS_EVENT,
                JSONObject(properties?.toMutableMap() ?: mutableMapOf<String, Any?>()).apply {
                    put(ROIQueryIasConstant.IAS_ORIGINAL_ORDER_ID, originalOrderId)
                    put(ROIQueryIasConstant.IAS_ORDER_ID, orderId)
                    put(ROIQueryIasConstant.IAS_SKU, sku)
                    put(ROIQueryIasConstant.IAS_PRICE, price)
                    put(ROIQueryIasConstant.IAS_CURRENCY, currency)
                }
            )
        }
    }
}


