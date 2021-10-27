package com.roiquery.ad

internal object AdReportConstant {


    //预置事件名称
    private const val PRESET_EVENT_TAG = "@.#"
    const val EVENT_AD_ENTRANCE = PRESET_EVENT_TAG + "ad_entrance"
    const val EVENT_AD_TO_SHOW = PRESET_EVENT_TAG + "ad_to_show"
    const val EVENT_AD_SHOW = PRESET_EVENT_TAG + "ad_show"
    const val EVENT_AD_IMPRESSION = PRESET_EVENT_TAG + "ad_impression"
    const val EVENT_AD_OPEN = PRESET_EVENT_TAG + "ad_open"
    const val EVENT_AD_CLOSE = PRESET_EVENT_TAG + "ad_close"
    const val EVENT_AD_CLICK = PRESET_EVENT_TAG + "ad_click"
    const val EVENT_AD_LEFT_APP = PRESET_EVENT_TAG + "ad_left_app"
    const val EVENT_AD_RETURN_APP = PRESET_EVENT_TAG + "ad_return_app"
    const val EVENT_AD_REWARDED = PRESET_EVENT_TAG + "ad_rewarded"
    const val EVENT_AD_CONVERSION = PRESET_EVENT_TAG + "ad_conversion"
    const val EVENT_AD_PAID = PRESET_EVENT_TAG + "ad_paid"


    const val PROPERTY_AD_ID = "ad_id"
    const val PROPERTY_AD_TYPE = "ad_type"
    const val PROPERTY_AD_PLATFORM = "ad_platform"
    const val PROPERTY_AD_LOCATION = "ad_location"
    const val PROPERTY_AD_ENTRANCE = "ad_entrance"
    const val PROPERTY_AD_SEQ = "ad_seq"
    const val PROPERTY_AD_CONVERSION_SOURCE = "ad_conversion_source"
    const val PROPERTY_AD_CLICK_GAP = "ad_click_gap"
    const val PROPERTY_AD_RETURN_GAP = "ad_return_gap"

    const val PROPERTY_AD_MEDIAITON = "ad_mediation"
    const val PROPERTY_AD_MEDIAITON_ID = "ad_mediation_id"
    const val PROPERTY_AD_VALUE_MICROS = "ad_value"
    const val PROPERTY_AD_CURRENCY_CODE = "ad_currency"
    const val PROPERTY_AD_PRECISION_TYPE = "ad_precision"
    const val PROPERTY_AD_COUNTRY = "ad_country"
}

interface AD_CONVERSION_SOURCE {
    companion object {
        const val CLICK = "by_click"
        const val LEFT_APP = "by_left_app"
        const val IMPRESSION = "by_impression"
        const val REWARDED = "by_rewarded"
    }
}

enum class AdType(val value: Int) {
    IDLE(-1),
    BANNER(0),
    INTERSTITIAL(1),
    NATIVE(2),
    REWARDED(3),
    REWARDED_INTERSTITIAL(4),
    APP_OPEN(5)
}

enum class AdMediation(val value: Int){
    IDLE(-1),
    MOPUB(0),
    IRONSOURCE(1)
}

enum class AdPlatform(val value: Int){
     UNDISCLOSED(-2),
     IDLE(-1),
     ADMOB(0),
     MOPUB(1),
     ADCOLONY(2),
     APPLOVIN(3),
     CHARTBOOST(4),
     FACEBOOK(5),
     INMOBI(6),
     IRONSOURCE(7),
     PANGLE(8),
     SNAP_AUDIENCE_NETWORK(9),
     TAPJOY(10),
     UNITY_ADS (11),
     VERIZON_MEDIA(12),
     VUNGLE(13),
     ADX(14)
}