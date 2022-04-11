package com.roiquery.ad_report_demo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.roiquery.ad.AdMediation
import com.roiquery.ad.AdPlatform
import com.roiquery.ad.AdType

import com.roiquery.ad.ROIQueryAdReport
import com.roiquery.ad.utils.UUIDUtils
import com.roiquery.analytics.ROIQueryAnalytics


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_report)

        val seq = UUIDUtils.generateUUID()
        findViewById<View>(R.id.button_track_entrance).setOnClickListener {
            ROIQueryAnalytics.setAppsFlyerId("shafdjfkajd")
            val p = mutableMapOf<String,Any>()
            p["sd1"] = false
            p["sd2"] = 2.09
            ROIQueryAdReport.reportEntrance(
                "",
                AdType.REWARDED_INTERSTITIAL,
                AdPlatform.ADX,
                "home",
                seq,
                p,
                "main"
            )



        }
        findViewById<View>(R.id.button_track_to_show).setOnClickListener {
            ROIQueryAnalytics.setKochavaId("sdf23r243r")
            val p = mutableMapOf<String,Any>()
            p["sd3"] = "p3"
            p["sd4"] = "p4"
            ROIQueryAdReport.reportToShow(
                "",
                AdType.REWARDED_INTERSTITIAL,
                AdPlatform.ADX,
                "user",
                seq,
                p,
                "main"
            )
        }
        findViewById<View>(R.id.button_track_show).setOnClickListener {
            ROIQueryAdReport.reportShow(
                "",
                AdType.REWARDED_INTERSTITIAL,
                AdPlatform.ADX,
                "car",
                seq
            )
        }

        findViewById<View>(R.id.button_track_show_failed).setOnClickListener {
            ROIQueryAdReport.reportShowFailed(
                "",
                AdType.REWARDED_INTERSTITIAL,
                AdPlatform.ADX,
                "car",
                seq,
                12,
                "network"
            )
        }

        findViewById<View>(R.id.button_track_impression).setOnClickListener {
            ROIQueryAdReport.reportImpression(
                "4",
                AdType.REWARDED_INTERSTITIAL,
                AdPlatform.ADX,
                "home",
                seq
            )
        }
            findViewById<View>(R.id.button_track_click).setOnClickListener {
                ROIQueryAdReport.reportClick(
                    "5",
                    AdType.REWARDED_INTERSTITIAL,
                    AdPlatform.ADX,
                    "home",
                    seq
                )

            }
            findViewById<View>(R.id.button_track_open).setOnClickListener {
                ROIQueryAdReport.reportImpression(
                    "4",
                    AdType.REWARDED_INTERSTITIAL,
                    AdPlatform.ADX,
                    "home",
                    seq
                )
            }

            findViewById<View>(R.id.button_track_close).setOnClickListener {
                ROIQueryAdReport.reportClose(
                    "4",
                    AdType.REWARDED_INTERSTITIAL,
                    AdPlatform.ADX,
                    "home",
                    seq
                )
            }


            findViewById<View>(R.id.button_track_left).setOnClickListener {
                //从其他浏览器打开
                val uri = Uri.parse("https://www.baidu.com")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)

                ROIQueryAdReport.reportLeftApp(
                    "",
                    AdType.REWARDED_INTERSTITIAL,
                    AdPlatform.ADX,
                    "home",
                    seq
                )
            }

            findViewById<View>(R.id.button_track_paid).setOnClickListener {
                ROIQueryAdReport.reportPaid(
                    "",
                    AdType.REWARDED_INTERSTITIAL,
                    AdPlatform.ADX,
                    "home",
                    seq,
                    "5000",
                    "01",
                    "1"
                )
            }
            findViewById<View>(R.id.button_track_paid_mediation).setOnClickListener {
                ROIQueryAdReport.reportPaid(
                    "12435",
                    AdType.REWARDED_INTERSTITIAL,
                    "custom_native",
                    "Bigo",
                    "network",
                    "home",
                    seq,
                    AdMediation.MOPUB,
                    "32432545",
                    "5000",
                    "usd",
                    "sdf",
                    "USA"
                )
            }

            findViewById<View>(R.id.button_track_conversion_click).setOnClickListener {
                ROIQueryAdReport.reportConversionByClick(
                    "4",
                    AdType.REWARDED_INTERSTITIAL,
                    AdPlatform.ADX,
                    "home",
                    seq
                )
            }

            findViewById<View>(R.id.button_track_conversion_left_app).setOnClickListener {
                ROIQueryAdReport.reportConversionByLeftApp(
                    "4",
                    AdType.REWARDED_INTERSTITIAL,
                    AdPlatform.ADX,
                    "home",
                    seq
                )
            }

            findViewById<View>(R.id.button_track_conversion_impression).setOnClickListener {
                ROIQueryAdReport.reportConversionByImpression(
                    "4",
                    AdType.REWARDED_INTERSTITIAL,
                    AdPlatform.ADX,
                    "home",
                    seq
                )
                ROIQueryAdReport.reportConversionByRewarded(
                    "4",
                    AdType.REWARDED_INTERSTITIAL,
                    AdPlatform.ADX,
                    "home",
                    seq
                )
            }
        }

    }