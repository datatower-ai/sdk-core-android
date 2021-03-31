package com.nodetower.demo_iap

import android.app.Application
import com.roiquery.analytics.ROIQuerySDK



class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ROIQuerySDK.init(this,"android_iap",true)
    }

}