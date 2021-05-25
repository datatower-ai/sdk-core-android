package com.roiquery.ad.utils

import com.roiquery.ad.AD_MEDIATION
import com.roiquery.ad.AD_PLATFORM
import com.roiquery.ad.AD_TYPE

object AdTypeUtils {

    fun getType(mediation: Int, typeString: String) =
        when (mediation) {
            AD_MEDIATION.MOPUB -> getMopubType(typeString)
            else -> AD_TYPE.IDLE
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
            else -> AD_PLATFORM.IDLE
        }

    private fun getMopubPlatform(typeString: String) =
        when (typeString) {
            "admob_native" -> AD_PLATFORM.ADMOB
            "pangle" -> AD_PLATFORM.PANGLE
            "ironsource" -> AD_PLATFORM.IRONSOURCE
            else -> AD_TYPE.IDLE
        }
}

data class AdEventProperty (
    private var mCurrentLocation: String = "",
    private var mCurrentAdType: Int = AD_TYPE.IDLE,
    private var mCurrentAdPlatform: Int = AD_PLATFORM.IDLE,
    private var mCurrentAdId: String = "",
    private var mCurrentSeq: String = "",
    private var mCurrentEntrance: String = "",

    private var mShowTS: Long = 0,
    private var mClickTS: Long = 0,
    private var mLeftApplicationTS: Long = 0,
    private var mAppBackgroundedTS: Long = 0,
    private var mAppForegroundedTS: Long = 0
){

}