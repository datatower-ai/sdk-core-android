package com.roiquery.analytics_demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.infinum.dbinspector.DbInspector

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<View>(R.id.button_analytics_test).setOnClickListener {
            startActivity(Intent(this,AnalyticsTestActivity::class.java))
        }
        findViewById<View>(R.id.User_Operate).setOnClickListener {
            startActivity(Intent(this, UserSetTest::class.java))
        }

        findViewById<View>(R.id.button_db_view).setOnClickListener {
           startService(Intent(this, SubProcessService::class.java))
        }
    }


    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }


}