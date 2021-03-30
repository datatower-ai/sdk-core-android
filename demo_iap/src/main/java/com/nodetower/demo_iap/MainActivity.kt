package com.nodetower.demo_iap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.roiquery.iap.ROIQueryIAPReport

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.button_report_entrance).setOnClickListener {
            ROIQueryIAPReport.reportEntrance("IDS",4.343,"usd","home")
        }

        findViewById<View>(R.id.button_track_to_purchase).setOnClickListener {
            ROIQueryIAPReport.reportToPurchase("IDS",4.343,"usd","home")
        }

        findViewById<View>(R.id.button_track_purchased).setOnClickListener {
            ROIQueryIAPReport.reportPurchased("IDS",4.343,"usd","home")
        }

        findViewById<View>(R.id.button_track_not_purchased).setOnClickListener {
            ROIQueryIAPReport.reportNotToPurchased("IDS",4.343,"usd","301","user","no meney")
        }

    }
}