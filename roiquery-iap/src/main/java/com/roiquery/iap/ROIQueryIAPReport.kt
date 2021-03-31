package com.roiquery.iap

import com.roiquery.cloudconfig.ROIQueryCloudConfig
import com.roiquery.iap.api.IAPReportImp


open class ROIQueryIAPReport {
    companion object {


        /**
         * sdk 是否可用，默认可用，由cloud config 控制
         */
        internal fun isSDKEnable() =
            ROIQueryCloudConfig.getBoolean(Constant.ENABLE_IAP_SDK_KEY, true)

        /**
         * 展示购买入口的时候上报
         *
         * @param sku 商品ID
         * @param price 价格， 如 9.99
         * @param currency 货币，如usd
         * @param entrance 入口，可为空
         */
        @JvmStatic
        @JvmOverloads
        fun reportEntrance(
            sku: String,
            price: Double,
            currency: String,
            entrance: String? = "",
        ) = IAPReportImp.getInstance().reportEntrance(sku, price, currency, entrance)

        /**
         * 点击购买的时候上报
         *
         * @param sku 商品ID
         * @param price 价格， 如 9.99
         * @param currency 货币，如usd
         * @param entrance 入口，可为空
         */
        @JvmStatic
        @JvmOverloads
        fun reportToPurchase(
            sku: String,
            price: Double,
            currency: String,
            entrance: String? = "",
        ) = IAPReportImp.getInstance().reportToPurchase(sku, price, currency, entrance)

        /**
         * 购买成功的时候上报，无论是否消耗
         *
         * @param sku 商品ID
         * @param price 价格， 如 9.99
         * @param currency 货币，如usd
         * @param entrance 入口，可为空
         */
        @JvmStatic
        @JvmOverloads
        fun reportPurchased(
            sku: String,
            price: Double,
            currency: String,
            entrance: String? = "",
        ) = IAPReportImp.getInstance().reportPurchased(sku, price, currency, entrance)

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
        @JvmStatic
        @JvmOverloads
        fun reportNotToPurchased(
            sku: String,
            price: Double,
            currency: String,
            code: String,
            entrance: String? = "",
            msg: String? = "",
        ) = IAPReportImp.getInstance()
            .reportNotToPurchased(sku, price, currency, code, entrance, msg)
    }

}