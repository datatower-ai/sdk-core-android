package com.roiquery.ad_report_demo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.roiquery.ad.AD_PLATFORM
import com.roiquery.ad.AD_TYPE
import com.roiquery.ad.ROIQueryAdReport
import com.roiquery.ad.utils.UUIDUtils
import com.roiquery.analytics.ROIQueryAnalytics


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_report)

        val seq = UUIDUtils.generateUUID()
        findViewById<View>(R.id.button_track_entrance).setOnClickListener {
            ROIQueryAdReport.reportEntrance(
                "1",
                AD_TYPE.BANNER,
                AD_PLATFORM.ADMOB,
                "home",
                seq,
                "main"
            )
        }
        findViewById<View>(R.id.button_track_to_show).setOnClickListener {
            ROIQueryAdReport.reportToShow(
                "2",
                AD_TYPE.INTERSTITIAL,
                AD_PLATFORM.ADMOB,
                "user",
                seq,
                "main"
            )
        }
        findViewById<View>(R.id.button_track_show).setOnClickListener {
            ROIQueryAdReport.reportShow(
                "3",
                AD_TYPE.BANNER,
                AD_PLATFORM.ADMOB,
                "car",
                seq,
                "home"
            )
        }
        findViewById<View>(R.id.button_track_close).setOnClickListener {
            ROIQueryAdReport.reportClose(
                "4",
                AD_TYPE.BANNER,
                AD_PLATFORM.ADMOB,
                "home",
                seq,
                "main"
            )
        }
        findViewById<View>(R.id.button_track_click).setOnClickListener {
            ROIQueryAdReport.reportClick(
                "5",
                AD_TYPE.BANNER,
                AD_PLATFORM.ADMOB,
                "home",
                seq,
                "main"
            )

            //从其他浏览器打开
            val uri = Uri.parse("https://www.baidu.com")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)

            ROIQueryAdReport.reportLeftApp(
                "",
                AD_TYPE.BANNER,
                AD_PLATFORM.ADMOB,
                "home",
                seq,
                "main"
            )
        }
        findViewById<View>(R.id.button_track_rewarded).setOnClickListener {
            ROIQueryAdReport.reportRewarded(
                "",
                AD_TYPE.BANNER,
                AD_PLATFORM.ADMOB,
                "home",
                seq,
                "main"
            )
        }
        findViewById<View>(R.id.button_track_sample).setOnClickListener {
            ROIQueryAnalytics.apply {
                setAccountId("1234567")
                track("login")
            }
        }

    }
}