package com.roiquery.analytics_demo

import android.app.Application
import com.roiquery.ad.ROIQueryAdReport
import com.roiquery.analytics.ROIQuery
import com.roiquery.analytics.ROIQueryChannel


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        //init SDK
//        ROIQuery.initSDK(this,"dt_id_test",ROIQueryChannel.GP,false)
        ROIQuery.initSDK(this,"dt_529e9ffca9ea3b49",ROIQueryChannel.GP,true)

        //mock data
        if (SharedPreferencesUtils.getParam(this,"first_open",true) as Boolean) {

            SharedPreferencesUtils.setParam(this,"acid","acid-" + ROIQueryAdReport.generateUUID())
            SharedPreferencesUtils.setParam(this,"fiid","fiid-" + ROIQueryAdReport.generateUUID())
            SharedPreferencesUtils.setParam(this,"fcm_token","fcm_token" + ROIQueryAdReport.generateUUID())
            SharedPreferencesUtils.setParam(this,"afid","afid-" + ROIQueryAdReport.generateUUID())
            SharedPreferencesUtils.setParam(this,"asid","asid-" + ROIQueryAdReport.generateUUID())
            SharedPreferencesUtils.setParam(this,"koid","koid-" + ROIQueryAdReport.generateUUID())

            SharedPreferencesUtils.setParam(this,"first_open",false)
        }

    }

}