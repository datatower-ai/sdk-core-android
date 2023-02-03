package com.roiquery.analytics_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.roiquery.ias.DTIASReport
import com.roiquery.ias.ROIQueryIasReport
import java.lang.StringBuilder
import java.util.*

class IasReportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ias_report)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        title = "IasReport"

        val seq = DTIASReport.generateUUID()
        var order = generateUUID() ?: "24234"
        var o_order = generateUUID() ?: "sdsd"
        val entrance = "new_page"
        val placement = "new_page"
        val sku = "plan_b"
        val price = 0.09
        val currency = "usd"

        findViewById<View>(R.id.button_report_to_show).setOnClickListener {
            DTIASReport.reportToShow(seq, entrance, placement)
        }

        findViewById<View>(R.id.button_track_show_success).setOnClickListener {
            DTIASReport.reportShowSuccess(seq, entrance, placement)
        }

        findViewById<View>(R.id.button_track_show_fail).setOnClickListener {
            DTIASReport.reportShowFail(seq, entrance, placement,"-1", "no network")
        }

        findViewById<View>(R.id.button_track_to_subscribe).setOnClickListener {
            DTIASReport.reportSubscribe(seq, entrance, placement, sku ,order, price.toString(), currency)
        }

        findViewById<View>(R.id.button_track_subscribe_success).setOnClickListener {
            DTIASReport.reportSubscribeSuccess(seq, entrance, placement, sku ,order, o_order, price.toString(), currency)
        }

        findViewById<View>(R.id.button_track_subscribe_fail).setOnClickListener {
            DTIASReport.reportSubscribeFail(seq, entrance, placement, sku ,order, o_order,price.toString(), currency,"-2")
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