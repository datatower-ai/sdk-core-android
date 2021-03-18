package com.nodetower.demo_cloudconfig

import android.app.Application
import com.roiquery.analytics.ROIQuerySDK

class MyApp:Application() {

    override fun onCreate() {
        super.onCreate()
        ROIQuerySDK.init(this,"demo_cloudconfig",true)
    }
}