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

        findViewById<View>(R.id.button_track_subscribe_success).setOnClickListener {
            val properties: HashMap<String, Any> = HashMap<String, Any>().apply {
                put("placement",placement)
            }
            DTIASReport.reportSubscribeSuccess(o_order, order, sku, price, currency,properties)
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