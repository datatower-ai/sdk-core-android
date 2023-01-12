package com.roiquery.analytics_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.roiquery.iap.DTIAPReport
import com.roiquery.iap.ROIQueryIAPReport
import java.lang.StringBuilder
import java.util.*

class IapReportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iap_report)


        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        title = "IapReport"


        val seq = DTIAPReport.generateUUID()
        var order = generateUUID()?:"24234"
        val placement = "me"



        findViewById<View>(R.id.button_track_purchased).setOnClickListener {
            val properties: HashMap<String, Any> = HashMap<String, Any>().apply {
                put("placement",placement)
            }
            DTIAPReport.reportPurchaseSuccess(order,"IDS",4.343,"usd", properties)
        }

    }
    fun generateUUID(): String? {
        val uuid = StringBuilder()
        for (i in 0..15) {
            uuid.append(Integer.toHexString(Random().nextInt(16)))
        }
        return uuid.toString()
    }
}