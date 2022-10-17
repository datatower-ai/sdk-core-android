package com.roiquery.analytics_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.roiquery.iap.ROIQueryIAPReport
import com.roiquery.iap.utils.UUIDUtils
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


        val seq = ROIQueryIAPReport.generateUUID()
        var order = generateUUID()?:"24234"
        val placement = "me"

        findViewById<View>(R.id.button_report_entrance).setOnClickListener {
            ROIQueryIAPReport.reportEntrance(order,"IDS",4.343,"eu",seq, placement)
        }

        findViewById<View>(R.id.button_track_to_purchase).setOnClickListener {
            ROIQueryIAPReport.reportToPurchase(order,"IDS",4.343,"usd",seq, placement)
        }

        findViewById<View>(R.id.button_track_purchased).setOnClickListener {
            ROIQueryIAPReport.reportPurchased(order,"IDS",4.343,"usd",seq, placement)
        }

        findViewById<View>(R.id.button_track_not_purchased).setOnClickListener {
            ROIQueryIAPReport.reportNotToPurchased(order,"IDS",4.343,"usd",seq,"1", placement)
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