package com.roiquery.analytics_demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.roiquery.ad.AdPlatform
import com.roiquery.ad.AdType
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


        findViewById<View>(R.id.button_track_load_begin).setOnClickListener {
            val p1 = mutableMapOf<String, Any>().apply {
                put("property_1", 3)
                put("property_2", "combo-123")
            }

            ROIQueryAdReport.reportLoadBegin(
                ad_unit,
                ad_type,
                ad_platform,
                ad_seq,
                p1
            )
        }

        findViewById<View>(R.id.button_track_load_end).setOnClickListener {
            ROIQueryAdReport.reportLoadEnd(
                ad_unit,
                ad_type,
                ad_platform,
                1000,
                true,
                ad_seq
            )
        }


        findViewById<View>(R.id.button_track_to_show).setOnClickListener {
            ROIQueryAdReport.reportToShow(
                ad_unit,
                ad_type,
                AdPlatform.IDLE,
                ad_location,
                ad_seq
            )
        }



        findViewById<View>(R.id.button_track_show).setOnClickListener {
            ROIQueryAdReport.reportShow(
                ad_unit,
                ad_type,
                ad_platform,
                ad_location,
                ad_seq
            )
        }

        findViewById<View>(R.id.button_track_paid).setOnClickListener {
            ROIQueryAdReport.reportPaid(
                ad_unit,
                ad_type,
                ad_platform,
                ad_location,
                ad_seq,
                "5000",
                "USD",
                "publisher"
            )
        }

        findViewById<View>(R.id.button_track_show_failed).setOnClickListener {
            ROIQueryAdReport.reportShowFailed(
                ad_unit,
                ad_type,
                ad_platform,
                ad_location,
                ad_seq,
                12,
                "network error"
            )
        }


        findViewById<View>(R.id.button_track_click).setOnClickListener {
            ROIQueryAdReport.reportClick(
                ad_unit,
                ad_type,
                ad_platform,
                ad_location,
                ad_seq
            )

            ROIQueryAdReport.reportConversionByClick(
                ad_unit,
                ad_type,
                ad_platform,
                ad_location,
                ad_seq
            )

        }

        findViewById<View>(R.id.button_track_close).setOnClickListener {
            ROIQueryAdReport.reportClose(
                "4",
                AdType.REWARDED_INTERSTITIAL,
                AdPlatform.ADX,
                "home",
                ad_seq
            )
        }


        findViewById<View>(R.id.button_track_left).setOnClickListener {

            ROIQueryAdReport.reportLeftApp(
                ad_unit,
                ad_type,
                ad_platform,
                ad_location,
                ad_seq
            )

            ROIQueryAdReport.reportConversionByLeftApp(
                ad_unit,
                ad_type,
                ad_platform,
                ad_location,
                ad_seq
            )
        }


        findViewById<View>(R.id.button_track_rewarded).setOnClickListener {
            ROIQueryAdReport.reportRewarded(
                ad_unit,
                ad_type,
                ad_platform,
                ad_location,
                ad_seq
            )

            ROIQueryAdReport.reportConversionByRewarded(
                ad_unit,
                ad_type,
                ad_platform,
                ad_location,
                ad_seq
            )
        }

    }

}