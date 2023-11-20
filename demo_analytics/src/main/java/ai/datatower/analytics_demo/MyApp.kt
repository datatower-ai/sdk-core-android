package ai.datatower.analytics_demo

import android.app.Application
import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.multidex.MultiDex
import ai.datatower.ad.DTAdReport
import ai.datatower.analytics.*


class MyApp : Application() {

    //ROIQuery base url
    private val SERVER_URL_TEST = "https://test.roiquery.com"
    override fun onCreate() {
        super.onCreate()
        val initBeginTime = SystemClock.elapsedRealtime()
        Log.d("initSDK begin", initBeginTime.toString())
        DTAdReport.generateUUID()
        DT.initSDK(
            this,
//            "dt_53ecce7c85c3daab",
            "dt_beb231f90a5a20ba", // test app
            SERVER_URL_TEST,
            DTChannel.GP,
            true,
            Log.VERBOSE
        )
        Log.d("initSDK end", (SystemClock.elapsedRealtime() - initBeginTime).toString())
        DTAnalytics.getDataTowerId(object : OnDataTowerIdListener {
            override fun onDataTowerIdCompleted(dataTowerId: String) {
                Log.d("DataTowerId", dataTowerId)
            }
        })

        DTAnalyticsUtils.trackTimerStart("initApp")
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
        DTAnalyticsUtils.trackTimerEnd("initApp")
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}