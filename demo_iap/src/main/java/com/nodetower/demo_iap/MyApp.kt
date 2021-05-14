package com.nodetower.demo_iap

import android.app.Application
import com.roiquery.analytics.ROIQuery



class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ROIQuery.initSDK(this,"android_iap",true)
    }

}