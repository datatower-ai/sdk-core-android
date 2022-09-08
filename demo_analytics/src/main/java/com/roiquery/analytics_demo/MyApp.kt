package com.roiquery.analytics_demo

import android.app.Application
import com.roiquery.analytics.ROIQuery
import com.roiquery.analytics.ROIQueryChannel


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        //init SDK
        ROIQuery.initSDK(this,"dt_test",ROIQueryChannel.GP,true)
    }

}