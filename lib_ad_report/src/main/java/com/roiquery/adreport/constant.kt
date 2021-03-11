package com.roiquery.adreport

internal object AdReportConstant {


    //预置事件名称
    const val EVENT_AD_ENTRANCE = "ad_entrance"
    const val EVENT_AD_TO_SHOW = "ad_to_show"
    const val EVENT_AD_SHOW = "ad_show"
    const val EVENT_AD_CLOSE = "ad_close"
    const val EVENT_AD_CLICK = "ad_click"
    const val EVENT_AD_LEFT_APP = "ad_left_app"
    const val EVENT_AD_RETURN_APP = "ad_return_app"
    const val EVENT_AD_REWARDED = "ad_rewarded"


    const val PROPERTY_AD_ID = "ad_id"
    const val PROPERTY_AD_TYPE = "ad_type"
    const val PROPERTY_AD_PLATFORM = "ad_platform"
    const val PROPERTY_AD_LOCATION = "ad_location"
    const val PROPERTY_AD_ENTRANCE = "ad_entrance"
    const val PROPERTY_AD_SEQ = "ad_seq"
    const val PROPERTY_AD_CLICK_GAP = "ad_click_gap"
    const val PROPERTY_AD_RETURN_GAP = "ad_return_gap"
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

 interface AD_PLATFORM {
    companion object {
        const val IDLE = -1
        const val ADMOB = 0

    }
}