package com.roiquery.analytics_demo

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.roiquery.analytics.ROIQueryAnalytics
import org.json.JSONObject

class SubProcessService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        ROIQueryAnalytics.track("sub_process_service_oncreate", JSONObject().apply {
//            put("process_name",ProcessUtils.getProcessName(application))
        })
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ROIQueryAnalytics.track("sub_process_service_onstartcommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}