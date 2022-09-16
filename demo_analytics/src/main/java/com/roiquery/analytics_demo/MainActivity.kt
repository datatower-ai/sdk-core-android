package com.roiquery.analytics_demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<View>(R.id.button_analytics_core).setOnClickListener{
            startActivity(Intent(this, CoreActivity::class.java))
        }

        findViewById<View>(R.id.button_ad_report).setOnClickListener{
            startActivity(Intent(this, AdReportActivity::class.java))
        }


        findViewById<View>(R.id.button_iap_report).setOnClickListener{
            startActivity(Intent(this, IapReportActivity::class.java))
        }

        findViewById<View>(R.id.button_ias_report).setOnClickListener{
            startActivity(Intent(this, IasReportActivity::class.java))
        }


    }


}