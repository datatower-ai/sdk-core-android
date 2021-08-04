package com.roiquery.ad_report_demo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.roiquery.ad.AD_MEDIATION
import com.roiquery.ad.AD_PLATFORM
import com.roiquery.ad.AD_TYPE
import com.roiquery.ad.ROIQueryAdReport
import com.roiquery.ad.utils.UUIDUtils


class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_report)

        val seq = UUIDUtils.generateUUID()
        findViewById<View>(R.id.button_track_entrance).setOnClickListener {
            ROIQueryAdReport.reportEntrance(
                "",
                AD_TYPE.BANNER,
                AD_PLATFORM.ADMOB,
                "home",
                seq,
                "main"
            )
        }
        findViewById<View>(R.id.button_track_to_show).setOnClickListener {
            ROIQueryAdReport.reportToShow(
                "",
                AD_TYPE.INTERSTITIAL,
                AD_PLATFORM.ADMOB,
                "user",
                seq,
                "main"
            )
        }
        findViewById<View>(R.id.button_track_show).setOnClickListener {
            ROIQueryAdReport.reportShow(
                "",
                AD_TYPE.BANNER,
                AD_PLATFORM.ADMOB,
                "car",
                seq,
                "home"
            )
        }
        findViewById<View>(R.id.button_track_impression).setOnClickListener {
            ROIQueryAdReport.reportImpression(
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

            }
            findViewById<View>(R.id.button_track_open).setOnClickListener {
                ROIQueryAdReport.reportImpression(
                    "4",
                    AD_TYPE.BANNER,
                    AD_PLATFORM.ADMOB,
                    "home",
                    seq,
                    "main"
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


            findViewById<View>(R.id.button_track_left).setOnClickListener {
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

            findViewById<View>(R.id.button_track_paid).setOnClickListener {
                ROIQueryAdReport.reportPaid(
                    "",
                    AD_TYPE.BANNER,
                    AD_PLATFORM.MOPUB,
                    "home",
                    seq,
                    "5000",
                    "01",
                    "1",
                    "main"
                )
            }
            findViewById<View>(R.id.button_track_paid_mediation).setOnClickListener {
                ROIQueryAdReport.reportPaid(
                    "12435",
                    "Rewarded Video",
                    "unity",
                    "home",
                    seq,
                    AD_MEDIATION.MOPUB,
                    "32432545",
                    "5000",
                    "usd",
                    "sdf",
                    "USA",
                    "hone"
                )
            }

            findViewById<View>(R.id.button_track_conversion_click).setOnClickListener {
                ROIQueryAdReport.reportConversionByClick(
                    "4",
                    AD_TYPE.BANNER,
                    AD_PLATFORM.ADMOB,
                    "home",
                    seq,
                    "main"
                )
            }

            findViewById<View>(R.id.button_track_conversion_left_app).setOnClickListener {
                ROIQueryAdReport.reportConversionByLeftApp(
                    "4",
                    AD_TYPE.BANNER,
                    AD_PLATFORM.ADMOB,
                    "home",
                    seq,
                    "main"
                )
            }

            findViewById<View>(R.id.button_track_conversion_impression).setOnClickListener {
                ROIQueryAdReport.reportConversionByImpression(
                    "4",
                    AD_TYPE.BANNER,
                    AD_PLATFORM.ADMOB,
                    "home",
                    seq,
                    "main"
                )
                ROIQueryAdReport.reportConversionByRewarded(
                    "4",
                    AD_TYPE.BANNER,
                    AD_PLATFORM.ADMOB,
                    "home",
                    seq,
                    "main"
                )
            }
        }

    }