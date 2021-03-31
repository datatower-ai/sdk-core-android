package com.roiquery.ad_report_demo

import android.app.Application
import com.roiquery.analytics.ROIQuerySDK
import com.roiquery.analytics.utils.LogUtils


class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ROIQuerySDK.init(this,"android_ad",true, LogUtils.V)
    }

}