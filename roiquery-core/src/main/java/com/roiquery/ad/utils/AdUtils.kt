package com.roiquery.ad.utils

import com.roiquery.ad.AdMediation
import com.roiquery.ad.AdPlatform
import com.roiquery.ad.AdType
import com.roiquery.cloudconfig.utils.StringUtils


object AdPlatformUtils {

    private const val ADMOB_ADID_PREFIX = "ca-app-pub-"
    private const val ADMOB_PLATFORM_GOOGLE = "admob_native"
    private const val ADMOB_PLATFORM_PANGLE = "pangle"
    private const val ADMOB_PLATFORM_IRONSOURCE = "ironsource"
    private const val ADMOB_PLATFORM_MOPUB = "marketplace"
    private const val ADMOB_PLATFORM_UNITY_ADS = "unity"
    private const val ADMOB_PLATFORM_FACEBOOK = "facebook"
    private const val ADMOB_PLATFORM_UNDISCLOSED = "undisclosed"

    fun getId(mediationAdId: String, networkPlacementId: String, adgroupType: String): String {
        if (isMarketPlace(networkPlacementId, adgroupType)) {
            return mediationAdId
        }
        return networkPlacementId
    }

    fun getPlatform(
        mediation: Int,
        networkName: String,
        networkPlacementId: String,
        adgroupType: String
    ) =
        when (mediation) {
            AdMediation.MOPUB.value -> getMopubPlatform(
                networkName,
                networkPlacementId,
                adgroupType
            )
            else -> AdPlatform.IDLE
        }

    private fun isMarketPlace(network: String, adgroupType: String) =
        (StringUtils.isEmpty(network) || network == "null" || network == "NULL") && adgroupType == ADMOB_PLATFORM_MOPUB

    private fun getMopubPlatform(
        networkName: String,
        networkPlacementId: String,
        adgroupType: String
    ): AdPlatform {
        if (isMarketPlace(networkName, adgroupType)) {
            return AdPlatform.MOPUB
        }
        return when (networkName) {
            ADMOB_PLATFORM_GOOGLE -> if (networkPlacementId.startsWith(ADMOB_ADID_PREFIX)) AdPlatform.ADMOB else AdPlatform.ADX
            ADMOB_PLATFORM_PANGLE -> AdPlatform.PANGLE
            ADMOB_PLATFORM_IRONSOURCE -> AdPlatform.IRONSOURCE
            ADMOB_PLATFORM_MOPUB -> AdPlatform.MOPUB
            ADMOB_PLATFORM_UNITY_ADS -> AdPlatform.UNITY_ADS
            ADMOB_PLATFORM_FACEBOOK -> AdPlatform.FACEBOOK
            ADMOB_PLATFORM_UNDISCLOSED -> AdPlatform.UNDISCLOSED
            else -> if (adgroupType == ADMOB_PLATFORM_MOPUB) AdPlatform.MOPUB else AdPlatform.IDLE
        }
    }
}

data class AdEventProperty(
    var location: String = "",
    var adType: Int = AdType.IDLE.value,
    var adPlatform: Int = AdPlatform.IDLE.value,
    var adId: String = "",
    var seq: String = "",
    var properties: MutableMap<String, Any>? = mutableMapOf(),
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