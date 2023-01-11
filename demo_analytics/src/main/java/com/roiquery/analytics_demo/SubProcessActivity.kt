package com.roiquery.analytics_demo

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.roiquery.analytics.DTAnalytics
import com.roiquery.analytics.DTThirdPartyShareType

class SubProcessActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub_process)
        findViewById<Button>(R.id.btn_subProcess).setOnClickListener{
            DTAnalytics.track("btn_subProcess")
        }

    }
}