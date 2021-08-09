package com.nodetower.demo_cloudconfig

import android.app.Application
import com.roiquery.analytics.ROIQuery

class MyApp:Application() {

    override fun onCreate() {
        super.onCreate()
        ROIQuery.initSDK(this,"rq_debug",true)
    }
}