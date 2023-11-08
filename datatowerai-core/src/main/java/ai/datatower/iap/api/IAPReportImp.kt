package ai.datatower.iap.api


import android.annotation.SuppressLint
import android.content.Context
import ai.datatower.analytics.DTAnalytics
import ai.datatower.iap.Constant
import org.json.JSONObject

class IAPReportImp {


     fun reportPurchased(
         order: String,
         sku: String,
         price: Double,
         currency: String,
         properties: MutableMap<String, Any>? = mutableMapOf()
    ) {
        DTAnalytics.trackInternal(
            Constant.EVENT_IAP_PURCHASED,
            JSONObject(properties?.toMutableMap() ?: mutableMapOf<String, Any?>()).apply {
                put(Constant.PROPERTY_IAP_ORDER,order)
                put(Constant.PROPERTY_IAP_SKU, sku)
                put(Constant.PROPERTY_IAP_PRICE, price)
                put(Constant.PROPERTY_IAP_CURRENCY, currency)
            }
        )
    }





    companion object {

        const val TAG = "IAPReport"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: IAPReportImp? = null
        internal fun getInstance(): IAPReportImp {
            return instance ?: synchronized(this) {
                instance ?: IAPReportImp().also { instance = it }
            }
        }

    }


}