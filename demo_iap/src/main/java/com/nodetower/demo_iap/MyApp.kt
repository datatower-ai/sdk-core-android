package com.nodetower.demo_iap

import android.app.Application
import com.roiquery.analytics.ROIQuerySDK

import org.json.JSONObject


class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ROIQuerySDK.init(this,"android_iap",true)
    }

}