package com.roiquery.analytics_demo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.roiquery.analytics.ROIQueryAnalytics
import org.json.JSONObject
import org.koin.android.ext.koin.ERROR_MSG
import java.io.*
import java.util.HashMap

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("test","MainActivity --")
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<View>(R.id.button_analytics_core).setOnClickListener {
            startActivity(Intent(this, CoreActivity::class.java))
        }

        findViewById<View>(R.id.button_ad_report).setOnClickListener {
            startActivity(Intent(this, AdReportActivity::class.java))
        }


        findViewById<View>(R.id.button_iap_report).setOnClickListener {
            startActivity(Intent(this, IapReportActivity::class.java))
        }

        findViewById<View>(R.id.button_ias_report).setOnClickListener {
            startActivity(Intent(this, IasReportActivity::class.java))
        }

        findViewById<View>(R.id.button_test_memory).setOnClickListener {

        }
    }


    override fun onPause() {
        super.onPause()
        Handler().postDelayed({
            ROIQueryAnalytics.track("activity_on_pause")
        },2000)

    }

}