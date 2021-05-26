package com.roiquery.ad.utils

import com.roiquery.ad.AD_MEDIATION
import com.roiquery.ad.AD_PLATFORM
import com.roiquery.ad.AD_TYPE

object AdTypeUtils {

    fun getType(mediation: Int, typeString: String) =
        when (mediation) {
            AD_MEDIATION.MOPUB -> getMopubType(typeString)
            else -> AD_MEDIATION.IDLE
        }

    private fun getMopubType(typeString: String) =
        when (typeString) {
            "Banner" -> AD_TYPE.BANNER
            "Fullscreen" -> AD_TYPE.INTERSTITIAL
            "Custom" -> AD_TYPE.NATIVE
            "Medium Rectangle" -> AD_TYPE.NATIVE
            "Rewarded Ad" -> AD_TYPE.REWARDED
            else -> AD_TYPE.IDLE
        }
}

object AdPlatformUtils {

    fun getPlatform(mediation: Int, typeString: String) =
        when (mediation) {
            AD_MEDIATION.MOPUB -> getMopubPlatform(typeString)
            else -> AD_MEDIATION.IDLE
        }

    private fun getMopubPlatform(typeString: String) =
        when (typeString) {
            "admob_native" -> AD_PLATFORM.ADMOB
            "pangle" -> AD_PLATFORM.PANGLE
            "ironsource" -> AD_PLATFORM.IRONSOURCE
            "marketplace" -> AD_PLATFORM.MOPUB
            else -> AD_PLATFORM.IDLE
        }
}

data class AdEventProperty(
    var location: String = "",
    var adType: Int = AD_TYPE.IDLE,
    var adPlatform: Int = AD_PLATFORM.IDLE,
    var adId: String = "",
    var seq: String = "",
    var entrance: String = "",

    var mediation: Int = AD_MEDIATION.IDLE,
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