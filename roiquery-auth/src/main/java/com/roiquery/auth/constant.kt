package com.roiquery.iap

internal object Constant {


    //预置事件名称
    private const val PRESET_EVENT_TAG = "@.#"
    const val EVENT_IAP_ENTRANCE = PRESET_EVENT_TAG + "iap_entrance"
    const val EVENT_IAP_TO_PURCHASE = PRESET_EVENT_TAG + "iap_to_purchase"
    const val EVENT_IAP_PURCHASED = PRESET_EVENT_TAG + "iap_purchased"
    const val EVENT_IAP_NOT_PURCHASED = PRESET_EVENT_TAG + "iap_not_purchased"

    const val PROPERTY_IAP_ENTRANCE = "iap_entrance"
    const val PROPERTY_IAP_ORDER = "iap_order"
    const val PROPERTY_IAP_SKU = "iap_sku"
    const val PROPERTY_IAP_USD_PRICE = "iap_usd_price"
    const val PROPERTY_IAP_PRICE = "iap_price"
    const val PROPERTY_IAP_CURRENCY = "iap_currency"
    const val PROPERTY_IAP_CODE = "iap_code"
    const val PROPERTY_IAP_MSG = "iap_msg"

    const val ENABLE_IAP_SDK_KEY = "enable_iap_sdk"
}


