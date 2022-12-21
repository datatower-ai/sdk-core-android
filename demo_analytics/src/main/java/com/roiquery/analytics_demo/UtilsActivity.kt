package com.roiquery.analytics_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.roiquery.analytics.DTAnalyticsUtils
import org.json.JSONObject

class UtilsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_utils)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        title = "Utils"

        val eventName = "simple_event_name"
        val illegalEventName = "#simple_event_name"


        findViewById<View>(R.id.button_trackTimerStart).setOnClickListener {
            DTAnalyticsUtils.trackTimerStart(eventName)
        }
        findViewById<View>(R.id.button_trackTimerStartIllegal).setOnClickListener {
            DTAnalyticsUtils.trackTimerStart(illegalEventName)
        }

        findViewById<View>(R.id.button_trackTimerPause).setOnClickListener {
            DTAnalyticsUtils.trackTimerPause(eventName)
        }


        findViewById<View>(R.id.button_trackTimerResume).setOnClickListener {
            DTAnalyticsUtils.trackTimerResume(eventName)
        }


        findViewById<View>(R.id.button_trackTimerEnd).setOnClickListener {
            DTAnalyticsUtils.trackTimerEnd(eventName, JSONObject().apply {
                put("test","test")
            })
        }


    }



}