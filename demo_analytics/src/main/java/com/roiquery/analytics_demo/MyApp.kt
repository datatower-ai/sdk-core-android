package com.roiquery.analytics_demo

import android.app.Application
import android.util.Log
import com.roiquery.ad.DTAdReport
import com.roiquery.analytics.*


class MyApp : Application() {

    //ROIQuery base url
    private val SERVER_URL_TEST       = "https://test.roiquery.com"
    private val SERVER_URL_INNER      = "https://report-inner.roiquery.com"
    private val SERVER_URL_EXTERNAL   = "https://report.roiquery.com"
    override fun onCreate() {
        super.onCreate()
        DT.initSDK(
            this,
            "dt_461a208fdd075c27",
            SERVER_URL_TEST,
            DTChannel.GP,
            true,
            Log.VERBOSE,
            object : InitCallback {
                override fun onInitComplete(isSuccess: Boolean, msg: String) {
                    if (isSuccess) {
                        DTAnalytics.enableThirdShare(0)
                    }
                }
            }

        )

        Log.DEBUG

        //mock data
        if (SharedPreferencesUtils.getParam(this, "first_open", true) as Boolean) {

            SharedPreferencesUtils.setParam(this, "acid", "acid-" + DTAdReport.generateUUID())
            SharedPreferencesUtils.setParam(this, "fiid", "fiid-" + DTAdReport.generateUUID())
            SharedPreferencesUtils.setParam(
                this,
                "fcm_token",
                "fcm_token" + DTAdReport.generateUUID()
            )
            SharedPreferencesUtils.setParam(this, "afid", "afid-" + DTAdReport.generateUUID())
            SharedPreferencesUtils.setParam(this, "asid", "asid-" + DTAdReport.generateUUID())
            SharedPreferencesUtils.setParam(this, "koid", "koid-" + DTAdReport.generateUUID())
            SharedPreferencesUtils.setParam(
                this,
                "adjustId",
                "adjustId-" + DTAdReport.generateUUID()
            )


            SharedPreferencesUtils.setParam(this, "first_open", false)
        }

    }

}