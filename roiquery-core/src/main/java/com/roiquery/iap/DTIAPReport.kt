package com.roiquery.iap

import com.roiquery.ad.utils.UUIDUtils
import com.roiquery.iap.api.IAPReportImp


open class DTIAPReport {
    companion object {


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
        @JvmStatic
        @JvmOverloads
        fun reportEntrance(
            order: String,
            sku: String,
            price: Double,
            currency: String,
            seq: String,
            placement: String? = "",
        ) = IAPReportImp.getInstance().reportEntrance(order, sku, price, currency,seq, placement)

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
        @JvmStatic
        @JvmOverloads
        fun reportToPurchase(
            order: String,
            sku: String,
            price: Double,
            currency: String,
            seq: String,
            placement: String? = "",
        ) = IAPReportImp.getInstance().reportToPurchase(order, sku, price, currency, seq, placement)

        /**
         * 购买成功的时候上报，无论是否消耗
         *
         * @param order 订单
         * @param sku 商品ID
         * @param price 价格， 如 9.9
         * @param currency 货币，如usd
         * @param seq 系列行为标识
         * @param placement 位置，可为空
         */
        @JvmStatic
        @JvmOverloads
        fun reportPurchased(
            order: String,
            sku: String,
            price: Double,
            currency: String,
            seq: String,
            placement: String? = "",
        ) = IAPReportImp.getInstance().reportPurchased(order, sku, price, currency, seq, placement)

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
        @JvmStatic
        @JvmOverloads
        fun reportNotToPurchased(
            order: String,
            sku: String,
            price: Double,
            currency: String,
            seq: String,
            code: String,
            placement: String? = "",
            msg: String? = "",
        ) = IAPReportImp.getInstance()
            .reportNotToPurchased(order, sku, price, currency, seq, code, placement, msg)

        /**
         * 生成UUID
         */
        @JvmStatic
        fun generateUUID() = UUIDUtils.generateUUID()

    }



}