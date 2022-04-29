package com.roiquery.analytics_demo

import android.app.Application
import android.content.Intent
import com.jraska.console.Console
import com.roiquery.analytics.ROIQuery
import com.roiquery.analytics.ROIQueryChannel
import com.roiquery.analytics.utils.LogUtils


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        LogUtils.getConfig().setOnConsoleOutputListener { _, tag, content ->
            run {
                if (tag.contains("AnalyticsManager")
                    || tag.contains("AnalyticsApi")) {
                    Console.writeLine(tag)
                    Console.writeLine(content)
                }
            }
        }
        LogUtils.d("roiquery_app","app init ${this.applicationContext}")


        //init SDK
        ROIQuery.initSDK(this,"android_analytics",ROIQueryChannel.GP,true)
    }

}