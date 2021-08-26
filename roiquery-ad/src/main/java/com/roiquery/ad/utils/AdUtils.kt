package com.roiquery.ad.utils

import com.roiquery.ad.AdMediation
import com.roiquery.ad.AdPlatform
import com.roiquery.ad.AdType


object AdPlatformUtils {

    fun getPlatform(mediation: Int, typeString: String) =
        when (mediation) {
            AdMediation.MOPUB.value -> getMopubPlatform(typeString).value
            else -> AdMediation.IDLE.value
        }

    private fun getMopubPlatform(typeString: String) =
        when (typeString) {
            "admob_native" -> AdPlatform.ADMOB
            "pangle" -> AdPlatform.PANGLE
            "ironsource" -> AdPlatform.IRONSOURCE
            "marketplace" -> AdPlatform.MOPUB
            "unity" -> AdPlatform.UNITY_ADS
            "facebook" -> AdPlatform.FACEBOOK
            else -> AdPlatform.IDLE
        }
}

data class AdEventProperty(
    var location: String = "",
    var adType: Int = AdType.IDLE.value,
    var adPlatform: Int = AdPlatform.IDLE.value,
    var adId: String = "",
    var seq: String = "",
    var entrance: String = "",

    var mediation: Int = AdMediation.IDLE.value,
    var mediationId: String = "",
    var value: String = "",
    var currency: String = "",
    var precision: String = "",
    var country: String = "",

    var showTS: Long = 0,
    var clickTS: Long = 0,
    var leftApplicationTS: Long = 0,
    var appBackgroundedTS: Long = 0,
    var appForegroundedTS: Long = 0,

    var isLeftApplication: Boolean = false
)