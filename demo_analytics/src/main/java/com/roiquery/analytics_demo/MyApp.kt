package com.roiquery.analytics_demo

import android.app.Application
import com.roiquery.ad.DTAdReport
import com.roiquery.analytics.DT
import com.roiquery.analytics.DTChannel


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        //init SDK
//        ROIQuery.initSDK(this,"dt_id_test",ROIQueryChannel.GP,false)
        DT.initSDK(this,"dt_529e9ffca9ea3b49", DTChannel.GP,true)

        //mock data
        if (SharedPreferencesUtils.getParam(this,"first_open",true) as Boolean) {

            SharedPreferencesUtils.setParam(this,"acid","acid-" + DTAdReport.generateUUID())
            SharedPreferencesUtils.setParam(this,"fiid","fiid-" + DTAdReport.generateUUID())
            SharedPreferencesUtils.setParam(this,"fcm_token","fcm_token" + DTAdReport.generateUUID())
            SharedPreferencesUtils.setParam(this,"afid","afid-" + DTAdReport.generateUUID())
            SharedPreferencesUtils.setParam(this,"asid","asid-" + DTAdReport.generateUUID())
            SharedPreferencesUtils.setParam(this,"koid","koid-" + DTAdReport.generateUUID())

            SharedPreferencesUtils.setParam(this,"first_open",false)
        }

    }

}