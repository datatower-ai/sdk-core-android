package com.roiquery.analytics_demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.roiquery.ad.AdPlatform
import com.roiquery.ad.AdType
import com.roiquery.ad.DTAdReport
import com.roiquery.ad.ROIQueryAdReport


class AdReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_report)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        title = "AdReport"

        val ad_seq = ROIQueryAdReport.generateUUID()
        val ad_unit = "ad_uint-sf234234"
        val ad_type = AdType.REWARDED
        val ad_platform = AdPlatform.ADX
        val ad_location = "home"



        findViewById<View>(R.id.button_track_show).setOnClickListener {
            val properties: HashMap<String, Any> = HashMap<String, Any>().apply {
                put("location",ad_location)
            }

//            DTAdReport.reportShow(
//                ad_unit,
//                ad_type,
//                ad_platform,
//                properties
//            )
        }



        findViewById<View>(R.id.button_track_conversion).setOnClickListener {
            val properties: HashMap<String, Any> = HashMap<String, Any>().apply {
                put("seq",ad_seq)
            }
//            DTAdReport.reportConversion(
//                ad_unit,
//                ad_type,
//                ad_platform,
//                properties
//            )
        }

    }

}