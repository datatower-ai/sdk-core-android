package com.roiquery.ad.utils

import com.roiquery.ad.AdMediation
import com.roiquery.ad.AdPlatform
import com.roiquery.ad.AdType
import com.roiquery.cloudconfig.utils.StringUtils


object AdPlatformUtils {

    private const val ADMOB_ADID_PREFIX = "ca-app-pub-"
    private const val MOPUB_PLATFORM_GOOGLE = "admob_native"
    private const val MOPUB_PLATFORM_PANGLE = "pangle"
    private const val MOPUB_PLATFORM_IRONSOURCE = "ironsource"
    private const val MOPUB_PLATFORM_MOPUB = "marketplace"
    private const val MOPUB_PLATFORM_UNITY_ADS = "unity"
    private const val MOPUB_PLATFORM_FACEBOOK = "facebook"
    private const val MOPUB_PLATFORM_UNDISCLOSED = "undisclosed"

    private const val MOPUB_ADGROUP_NAME_BIGO = "Bigo"
    private const val MOPUB_ADGROUP_TYPE_BIGO = "network"


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
        adgroupName: String,
        adgroupType: String
    ) =
        when (mediation) {
            AdMediation.MOPUB.value -> getMopubPlatform(
                networkName,
                networkPlacementId,
                adgroupName,
                adgroupType
            )
            else -> AdPlatform.IDLE
        }

    private fun isMarketPlace(network: String, adgroupType: String) =
        (StringUtils.isEmpty(network) || network == "null" || network == "NULL") && adgroupType == MOPUB_PLATFORM_MOPUB

    private fun isBigo(adgroupName: String,adgroupType: String) = adgroupType == MOPUB_ADGROUP_TYPE_BIGO && adgroupName == MOPUB_ADGROUP_NAME_BIGO

    private fun getMopubPlatform(
        networkName: String,
        networkPlacementId: String,
        adgroupName: String,
        adgroupType: String,
    ): AdPlatform {
        if (isMarketPlace(networkName, adgroupType)) {
            return AdPlatform.MOPUB
        }
        if (isBigo(adgroupName,adgroupType)){
            return AdPlatform.BIGO
        }
        return when (networkName) {
            MOPUB_PLATFORM_GOOGLE -> if (networkPlacementId.startsWith(ADMOB_ADID_PREFIX)) AdPlatform.ADMOB else AdPlatform.ADX
            MOPUB_PLATFORM_PANGLE -> AdPlatform.PANGLE
            MOPUB_PLATFORM_IRONSOURCE -> AdPlatform.IRONSOURCE
            MOPUB_PLATFORM_MOPUB -> AdPlatform.MOPUB
            MOPUB_PLATFORM_UNITY_ADS -> AdPlatform.UNITY_ADS
            MOPUB_PLATFORM_FACEBOOK -> AdPlatform.FACEBOOK
            MOPUB_PLATFORM_UNDISCLOSED -> AdPlatform.UNDISCLOSED
            else -> if (adgroupType == MOPUB_PLATFORM_MOPUB) AdPlatform.MOPUB else AdPlatform.IDLE
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