package com.nodetower.roiquery_sdk

import android.app.Application
import com.jraska.console.Console
import com.nodetower.analytics.api.RoiqueryAnalyticsAPI

import com.nodetower.analytics.config.AnalyticsConfigOptions
import com.nodetower.base.utils.LogUtils


class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        //just for test
        LogUtils.getConfig().setOnConsoleOutputListener { type, tag, content ->
            run {
                if (tag.contains("AnalyticsManager")
                    || tag.contains("AnalyticsApi")) {
                    Console.writeLine(tag)
                    Console.writeLine(content)
                }
            }
        }
        initAnalytics()
    }

    private fun initAnalytics() {
        RoiqueryAnalyticsAPI.init(
            this,
            AnalyticsConfigOptions("demo_test", "https://api.roiquery.com")
                .setDebug(true)
        )
    }
}