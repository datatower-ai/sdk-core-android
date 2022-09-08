package com.roiquery.iap.api


import android.annotation.SuppressLint
import android.content.Context
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.iap.Constant
import com.roiquery.iap.ROIQueryIAPReport

class IAPReportImp : IIapReport {

    override fun reportEntrance(
        order: String,
        sku: String,
        price: Double,
        currency: String,
        seq: String,
        placement: String?
    ) {
        iapTrack(
            eventName = Constant.EVENT_IAP_ENTRANCE,
            order = order,
            sku = sku,
            price = price,
            currency = currency,
            seq = seq,
            placement = placement
        )
    }

    override fun reportToPurchase(
        order: String,
        sku: String,
        price: Double,
        currency: String,
        seq: String,
        placement: String?
    ) {
        iapTrack(
            eventName = Constant.EVENT_IAP_TO_PURCHASE,
            order = order,
            sku = sku,
            price = price,
            currency = currency,
            seq = seq,
            placement = placement
        )
    }

    override fun reportPurchased(
        order: String,
        sku: String,
        price: Double,
        currency: String,
        seq: String,
        placement: String?
    ) {
        iapTrack(
            eventName = Constant.EVENT_IAP_PURCHASED,
            order = order,
            sku = sku,
            price = price,
            currency = currency,
            seq = seq,
            placement = placement
        )
    }

    override fun reportNotToPurchased(
        order: String,
        sku: String,
        price: Double,
        currency: String,
        seq: String,
        code: String,
        placement: String?,
        msg: String?
    ) {
        iapTrack(
            eventName = Constant.EVENT_IAP_NOT_PURCHASED,
            order = order,
            sku = sku,
            price = price,
            currency = currency,
            seq = seq,
            code, placement, msg
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
        placement: String? = null,
        msg: String? = null
    ) {
        ROIQueryAnalytics.trackInternal(
            eventName, generateAdReportMap(
                order,sku, price, currency,seq, code, placement, msg
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
        placement: String?,
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
        placement?.let {
            put(Constant.PROPERTY_IAP_PLACEMENT, it)
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