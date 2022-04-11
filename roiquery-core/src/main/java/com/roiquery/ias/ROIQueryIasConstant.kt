package com.roiquery.ias

/**
 * author: xiaosailing
 * date: 2022-02-24
 * description:
 * version：1.0
 */
internal object ROIQueryIasConstant {
   const  val PRESET_EVENT_TAG  = "@.#"
    //展示订阅
   const val IAS_TO_SHOW_EVENT = PRESET_EVENT_TAG+"ias_to_show"
    // 展示订阅内容成功
   const val IAS_SHOW_SUCCESS_EVENT =  PRESET_EVENT_TAG+"ias_show_success"
    // 展示订阅内容失败
   const val IAS_SHOW_FAIL_EVENT =  PRESET_EVENT_TAG+"ias_show_fail"
    // 点击内购
   const val IAS_TO_SUBSCRIBE_EVENT =  PRESET_EVENT_TAG+"ias_to_subscribe"
    // 订阅成功
   const val IAS_TO_SUBSCRIBE_SUCCESS_EVENT =  PRESET_EVENT_TAG+"ias_subscribe_success"
    // 订阅失败
   const val IAS_TO_SUBSCRIBE_FAIL_EVENT =  PRESET_EVENT_TAG+"ias_subscribe_fail"

    // 系列行为唯一标识
   const val IAS_SEQ = "ias_seq"
    // 入口
   const val IAS_ENTRANCE = "ias_entrance"
    // 页面区分
   const val IAS_PLACEMENT = "ias_placement"
    // 错误码
   const val IAS_CODE = "ias_code"
    // 额外信息
   const val IAS_MSG = "ias_msg"
    //订阅的产品ID
   const val IAS_SKU = "ias_sku"
    // 订单ID
   const val IAS_ORDER_ID = "ias_order_id"
    // 价格
   const val IAS_PRICE = "ias_price"
    // 货币
   const val IAS_CURRENCY = "ias_currency"
    // 原始订单ID
   const val IAS_ORIGINAL_ORDER_ID = "ias_original_order_id"

}