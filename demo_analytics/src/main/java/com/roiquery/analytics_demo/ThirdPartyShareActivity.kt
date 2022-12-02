package com.roiquery.analytics_demo

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.roiquery.analytics.DT
import com.roiquery.analytics.DTAnalytics
import com.roiquery.analytics.DTShareType

class ThirdPartyShareActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third_party_share)
        findViewById<Button>(R.id.btn_adjust).setOnClickListener{
            DTAnalytics.enableThirdShare(DTShareType.ADJUST)
        }
        findViewById<Button>(R.id.btn_appsflyer).setOnClickListener {
//            DT.enableThirdShare(ThirdSDKShareType.APPSFly)
        }
    }
}