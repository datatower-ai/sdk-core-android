package com.nodetower.roiquery_sdk

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.nodetower.analytics.api.RoiqueryAnalyticsAPI
import com.nodetower.base.utils.LogUtils

class SubProcessService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        LogUtils.d("SubProcessService","onCreate")
        RoiqueryAnalyticsAPI.getInstance(this).track("sub_process_service_oncreate")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LogUtils.d("SubProcessService","onStartCommand")
        RoiqueryAnalyticsAPI.getInstance(this).track("sub_process_service_onstartcommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}