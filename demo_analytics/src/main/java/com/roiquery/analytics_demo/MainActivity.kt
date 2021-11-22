package com.roiquery.analytics_demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.infinum.dbinspector.DbInspector
import com.roiquery.analytics.utils.LogUtils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<View>(R.id.button_analytics_test).setOnClickListener {
            startActivity(Intent(this,AnalyticsTestActivity::class.java))
        }
        findViewById<View>(R.id.button_analytics_process).setOnClickListener {
            startService(Intent(this, SubProcessService::class.java))

        }


        findViewById<View>(R.id.button_db_view).setOnClickListener {
            DbInspector.show()
        }


    }


    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }


}