package com.roiquery.iap.api


import android.annotation.SuppressLint
import android.content.Context
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.iap.Constant
import com.roiquery.iap.ROIQueryIAPReport

class IAPReportImp : IIapReport {

    private var mContext: Context? = null

    override fun reportEntrance(
        order: String,
        sku: String,
        price: Double,
        currency: String,
        seq: String,
        entrance: String?
    ) {
        iapTrack(
            eventName = Constant.EVENT_IAP_ENTRANCE,
            order = order,
            sku = sku,
            price = price,
            currency = currency,
            seq = seq,
            entrance = entrance
        )
    }

    override fun reportToPurchase(
        order: String,
        sku: String,
        price: Double,
        currency: String,
        seq: String,
        entrance: String?
    ) {
        iapTrack(
            eventName = Constant.EVENT_IAP_TO_PURCHASE,
            order = order,
            sku = sku,
            price = price,
            currency = currency,
            seq = seq,
            entrance = entrance
        )
    }

    override fun reportPurchased(
        order: String,
        sku: String,
        price: Double,
        currency: String,
        seq: String,
        entrance: String?
    ) {
        iapTrack(
            eventName = Constant.EVENT_IAP_PURCHASED,
            order = order,
            sku = sku,
            price = price,
            currency = currency,
            seq = seq,
            entrance = entrance
        )
    }

    override fun reportNotToPurchased(
        order: String,
        sku: String,
        price: Double,
        currency: String,
        seq: String,
        code: String,
        entrance: String?,
        msg: String?
    ) {
        iapTrack(
            eventName = Constant.EVENT_IAP_NOT_PURCHASED,
            order = order,
            sku = sku,
            price = price,
            currency = currency,
            seq = seq,
            code, entrance, msg
        )
    }

    private fun iapTrack(
        eventName: String,
        order: String,
        sku: String,
        price: Double,
        currency: String,
        seq: String,
        code: String? = null,
        entrance: String? = null,
        msg: String? = null
    ) {
        if (!ROIQueryIAPReport.isSDKEnable()) return
        ROIQueryAnalytics.track(
            eventName, generateAdReportMap(
                order,sku, price, currency,seq, code, entrance, msg
            )
        )
    }

    private fun generateAdReportMap(
        order: String,
        sku: String,
        price: Double,
        currency: String,
        seq: String,
        code: String? = null,
        entrance: String?,
        msg: String?
    ) = mutableMapOf<String,Any>().apply {

        put(Constant.PROPERTY_IAP_SEQ,seq)
        put(Constant.PROPERTY_IAP_ORDER,order)
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

        @SuppressLint("StaticFieldLeak")
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