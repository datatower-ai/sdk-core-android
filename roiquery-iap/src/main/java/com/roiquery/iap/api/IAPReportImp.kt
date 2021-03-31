package com.roiquery.iap.api


import android.content.Context
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.iap.Constant
import com.roiquery.iap.ROIQueryIAPReport
import org.json.JSONObject

class IAPReportImp : IIapReport {

    private var mContext: Context? = null

    override fun reportEntrance(
        sku: String,
        price: Double,
        currency: String,
        entrance: String?
    ) {
        iapTrack(
            eventName = Constant.EVENT_IAP_ENTRANCE,
            sku = sku,
            price = price,
            currency = currency,
            entrance = entrance
        )
    }

    override fun reportToPurchase(
        sku: String,
        price: Double,
        currency: String,
        entrance: String?
    ) {
        iapTrack(
            eventName = Constant.EVENT_IAP_TO_PURCHASE,
            sku = sku,
            price = price,
            currency = currency,
            entrance = entrance
        )
    }

    override fun reportPurchased(
        sku: String,
        price: Double,
        currency: String,
        entrance: String?
    ) {
        iapTrack(
            eventName = Constant.EVENT_IAP_PURCHASED,
            sku = sku,
            price = price,
            currency = currency,
            entrance = entrance
        )
    }

    override fun reportNotToPurchased(
        sku: String,
        price: Double,
        currency: String,
        code: String,
        entrance: String?,
        msg: String?
    ) {
        iapTrack(
            eventName = Constant.EVENT_IAP_NOT_PURCHASED,
            sku = sku,
            price = price,
            currency = currency,
            code, entrance, msg
        )
    }

    private fun iapTrack(
        eventName: String,
        sku: String,
        price: Double,
        currency: String,
        code: String? = null,
        entrance: String? = null,
        msg: String? = null
    ) {
        if (!ROIQueryIAPReport.isSDKEnable()) return
        ROIQueryAnalytics.track(
            eventName, generateAdReportJson(
                sku, price, currency, code, entrance, msg
            )
        )
    }

    private fun generateAdReportJson(
        sku: String,
        price: Double,
        currency: String,
        code: String? = null,
        entrance: String?,
        msg: String?
    ) = JSONObject().apply {
        put(Constant.PROPERTY_IAP_SKU, sku)
        put(Constant.PROPERTY_IAP_PRICE, price)
        put(Constant.PROPERTY_IAP_CURRENCY, currency)
        code?.let {
            put(Constant.PROPERTY_IAP_CODE, it)
        }
        entrance?.let {
            put(Constant.PROPERTY_IAP_ENTRANCE, it)
        }
        msg?.let {
            put(Constant.PROPERTY_IAP_MSG, it)
        }

    }


    companion object {

        const val TAG = "IAPReport"

        @Volatile
        private var instance: IAPReportImp? = null
        internal fun getInstance(context: Context? = null): IAPReportImp {
            var pContext = context
            if (pContext == null) {
                pContext = ROIQueryAnalytics.getContext()
            }
            return instance ?: synchronized(this) {
                instance ?: IAPReportImp().also { instance = it }
            }
        }

    }


}