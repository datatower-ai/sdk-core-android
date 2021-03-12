package com.roiquery.ad_report_demo

import android.app.Application
import com.roiquery.analytics.ROIQuerySDK
import com.roiquery.analytics.api.PropertyBuilder
import com.roiquery.analytics.utils.LogUtils
import org.json.JSONObject


class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        var pro = PropertyBuilder.newInstance().append("#sdk_type","Unity").toJSONObject()
        if (pro != null) {
            ROIQuerySDK.init(this,"android_ad_report_demo",true, LogUtils.V,pro)
        }
    }

}