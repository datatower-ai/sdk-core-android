package com.nodetower.demo_iap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.roiquery.iap.ROIQueryIAPReport
import com.roiquery.iap.utils.UUIDUtils

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val order = UUIDUtils.generateUUID()

        findViewById<View>(R.id.button_report_entrance).setOnClickListener {
            ROIQueryIAPReport.reportEntrance(order,"IDS",4.343,1.24,"eu","home")
        }

        findViewById<View>(R.id.button_track_to_purchase).setOnClickListener {
            ROIQueryIAPReport.reportToPurchase(order,"IDS",4.343,1.24,"usd","home")
        }

        findViewById<View>(R.id.button_track_purchased).setOnClickListener {
            ROIQueryIAPReport.reportPurchased(order,"IDS",4.343,1.24,"usd","home")
        }

        findViewById<View>(R.id.button_track_not_purchased).setOnClickListener {
            ROIQueryIAPReport.reportNotToPurchased(order,"IDS",4.343,1.24,"usd","301","user","no meney")
        }

    }
}