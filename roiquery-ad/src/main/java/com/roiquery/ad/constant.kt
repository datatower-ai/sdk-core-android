package com.roiquery.ad

internal object AdReportConstant {


    //预置事件名称
    private const val PRESET_EVENT_TAG = "@.#"
    const val EVENT_AD_ENTRANCE = PRESET_EVENT_TAG + "ad_entrance"
    const val EVENT_AD_TO_SHOW = PRESET_EVENT_TAG + "ad_to_show"
    const val EVENT_AD_SHOW = PRESET_EVENT_TAG + "ad_show"
    const val EVENT_AD_CLOSE = PRESET_EVENT_TAG + "ad_close"
    const val EVENT_AD_CLICK = PRESET_EVENT_TAG + "ad_click"
    const val EVENT_AD_LEFT_APP = PRESET_EVENT_TAG + "ad_left_app"
    const val EVENT_AD_RETURN_APP = PRESET_EVENT_TAG + "ad_return_app"
    const val EVENT_AD_REWARDED = PRESET_EVENT_TAG + "ad_rewarded"
    const val EVENT_AD_PAID = PRESET_EVENT_TAG + "ad_paid"


    const val PROPERTY_AD_ID = "ad_id"
    const val PROPERTY_AD_TYPE = "ad_type"
    const val PROPERTY_AD_PLATFORM = "ad_platform"
    const val PROPERTY_AD_LOCATION = "ad_location"
    const val PROPERTY_AD_ENTRANCE = "ad_entrance"
    const val PROPERTY_AD_SEQ = "ad_seq"
    const val PROPERTY_AD_CLICK_GAP = "ad_click_gap"
    const val PROPERTY_AD_RETURN_GAP = "ad_return_gap"

    const val PROPERTY_AD_MEDIAITON = "ad_mediaiton"
    const val PROPERTY_AD_MEDIAITON_ID = "ad_mediaiton_id"
    const val PROPERTY_AD_VALUE_MICROS = "ad_value"
    const val PROPERTY_AD_CURRENCY_CODE = "ad_currency"
    const val PROPERTY_AD_PRECISION_TYPE = "ad_precision"
    const val PROPERTY_AD_COUNTRY = "ad_country"
}

interface AD_TYPE {
    companion object {
        const val IDLE = -1
        const val BANNER = 0
        const val INTERSTITIAL = 1
        const val NATIVE = 2
        const val REWARDED = 3
    }
}

interface AD_MEDIATION {
    companion object {
        const val IDLE = -1
        const val MOPUB = 0
        const val IRONSOURCE = 1
    }
}

interface AD_PLATFORM {
    companion object {
        const val IDLE = -1
        const val ADMOB = 0
        const val MOPUB = 1
        const val ADCOLONY = 2
        const val APPLOVIN = 3
        const val CHARTBOOST = 4
        const val FACEBOOK = 5
        const val INMOBI = 6
        const val IRONSOURCE = 7
        const val PANGLE = 8
        const val SNAP_AUDIENCE_NETWORK = 9
        const val TAPJOY = 10
        const val UNITY_ADS = 11
        const val VERIZON_MEDIA = 12
        const val VUNGLE = 13
    }
}