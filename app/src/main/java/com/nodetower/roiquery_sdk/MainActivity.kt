package com.nodetower.roiquery_sdk

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jraska.console.Console
import com.nodetower.analytics.api.IAnalyticsApi
import com.nodetower.analytics.api.PropertyBuilder
import com.nodetower.analytics.api.RoiqueryAnalyticsAPI
import com.nodetower.base.utils.LogUtils


class MainActivity : AppCompatActivity() {

    var mApi: IAnalyticsApi? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        
        findViewById<View>(R.id.button_analytics_test).setOnClickListener {
            startActivity(Intent(this,AnalyticsTestActivity::class.java))
        }

        findViewById<View>(R.id.button_clear_log).setOnClickListener {
            Console.clear()
        }
    }

    override fun onStart() {
        super.onStart()
        mApi?.trackPageOpen()
    }

    override fun onStop() {
        super.onStop()
        mApi?.trackPageClose()
    }


}