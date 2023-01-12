package com.roiquery.ad.api


import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.text.TextUtils
import com.roiquery.ad.AdPlatform
import com.roiquery.ad.AdReportConstant
import com.roiquery.ad.AdReportConstant.PROPERTY_AD_SHOW_ERROR_CODE
import com.roiquery.ad.AdReportConstant.PROPERTY_AD_SHOW_ERROR_MESSAGE
import com.roiquery.ad.AdType
import com.roiquery.ad.utils.AdEventProperty
import com.roiquery.ad.utils.AdPlatformUtils
import com.roiquery.analytics.DTAnalytics
import com.roiquery.analytics.utils.AppInfoUtils
import com.roiquery.analytics.utils.AppLifecycleHelper.OnAppStatusListener
import com.roiquery.analytics.utils.EventUtils
import com.roiquery.analytics.utils.LogUtils
import org.json.JSONObject

class AdReportImp : IAdReport {


    override fun reportShow(
        id: String,
        type: Int,
        platform: Int,
        properties: MutableMap<String, Any>?,
    ) {
        try {
            adTrack(
                AdReportConstant.EVENT_AD_SHOW,
                JSONObject(properties?.toMutableMap() ?: mutableMapOf<String, Any?>()).apply {
                    put(AdReportConstant.PROPERTY_AD_ID, id)
                    put(AdReportConstant.PROPERTY_AD_TYPE, type)
                    put(AdReportConstant.PROPERTY_AD_PLATFORM, platform)
                }
            )
        }catch (e: Exception){

        }
    }

    override fun reportConversion(
        id: String,
        type: Int,
        platform: Int,
        properties: MutableMap<String, Any>?,
    ) {
        try {
            adTrack(
                AdReportConstant.EVENT_AD_CONVERSION,
                JSONObject(properties?.toMutableMap() ?: mutableMapOf<String, Any?>()).apply {
                    put(AdReportConstant.PROPERTY_AD_ID, id)
                    put(AdReportConstant.PROPERTY_AD_TYPE, type)
                    put(AdReportConstant.PROPERTY_AD_PLATFORM, platform)
                }
            )
        }catch (e: Exception){

        }
    }




    private fun adTrack(
        eventName: String,
        properties: JSONObject?,
    ) {
        DTAnalytics.trackInternal(eventName, properties)
    }




    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: AdReportImp? = null
        internal fun getInstance(): AdReportImp {
            return instance ?: synchronized(this) {
                instance ?: AdReportImp().also { instance = it }
            }
        }

    }


}