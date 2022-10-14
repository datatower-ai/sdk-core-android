package com.roiquery.analytics_demo

import android.app.Application
import com.roiquery.ad.utils.UUIDUtils
import com.roiquery.analytics.ROIQuery
import com.roiquery.analytics.ROIQueryChannel
import com.roiquery.analytics.utils.LogUtils
import org.json.JSONObject


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        //init SDK
        ROIQuery.initSDK(this,"dt_c054cd56b91c8b7a",ROIQueryChannel.GP,true)

        //mock data
        if (SharedPreferencesUtils.getParam(this,"first_open",true) as Boolean) {

            SharedPreferencesUtils.setParam(this,"acid","acid-" + UUIDUtils.generateUUID())
            SharedPreferencesUtils.setParam(this,"fiid","fiid-" + UUIDUtils.generateUUID())
            SharedPreferencesUtils.setParam(this,"fcm_token","fcm_token" + UUIDUtils.generateUUID())
            SharedPreferencesUtils.setParam(this,"afid","afid-" + UUIDUtils.generateUUID())
            SharedPreferencesUtils.setParam(this,"asid","asid-" + UUIDUtils.generateUUID())
            SharedPreferencesUtils.setParam(this,"koid","koid-" + UUIDUtils.generateUUID())

            SharedPreferencesUtils.setParam(this,"first_open",false)
        }

    }

}