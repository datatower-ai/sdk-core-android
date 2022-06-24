package com.roiquery.ad_report_demo;



import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.roiquery.ad.AdMediation;
import com.roiquery.ad.AdPlatform;
import com.roiquery.ad.AdType;
import com.roiquery.ad.ROIQueryAdReport;
import com.roiquery.ad.utils.UUIDUtils;
import com.roiquery.analytics.ROIQueryAnalytics;

import java.util.HashMap;

public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_report);

        String seq = ROIQueryAdReport.generateUUID();
        String seq2 = ROIQueryAdReport.generateUUID();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ROIQueryAnalytics.setAccountId("login_1");
            }
        },2000);

        findViewById(R.id.button_track_entrance).setOnClickListener(v -> {
            HashMap<String, Object> pro = new HashMap<>();
            pro.put("test_property_3", false);
            pro.put("test_property_4", 2.3);
            ROIQueryAdReport.reportEntrance(
                    "1",
                    AdType.BANNER,
                    AdPlatform.MOPUB,
                    "home",
                    seq,
                    pro
            );

        });

        findViewById(R.id.button_track_to_show).setOnClickListener(v -> {
            ROIQueryAdReport.reportToShow(
                    "2",
                    AdType.INTERSTITIAL,
                    AdPlatform.MOPUB,
                    "user",
                    seq

            );

            ROIQueryAdReport.reportToShow(
                    "2",
                    AdType.INTERSTITIAL,
                    AdPlatform.MOPUB,
                    "user2",
                    seq2
            );
        });
        findViewById(R.id.button_track_show).setOnClickListener(v -> {
                    ROIQueryAdReport.reportShow(
                            "3",
                            AdType.BANNER,
                            AdPlatform.MOPUB,
                            "car",
                            seq
                    );
                    ROIQueryAdReport.reportShow(
                            "3",
                            AdType.BANNER,
                            AdPlatform.MOPUB,
                            "car2",
                            seq2
                    );
                }

        );
        findViewById(R.id.button_track_close).setOnClickListener(v -> {
//                    ROIQueryAdReport.reportPaid(
//                            "12435",
//                            AD_TYPE.REWARDED,
//                            "unity",
//                            "home",
//                            seq2,
//                            AD_MEDIATION.MOPUB,
//                            "32432545",
//                            "5000",
//                            "usd",
//                            "sdf",
//                            "USA",
//                            "hone"
//                    );
                }

        );
        findViewById(R.id.button_track_click).setOnClickListener(v -> {
            ROIQueryAdReport.reportClick(
                    "",
                    AdType.IDLE,
                    AdPlatform.IDLE,
                    "home",
                    seq2
            );

            //从其他浏览器打开
            Uri uri = Uri.parse("https://www.baidu.com");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);

            ROIQueryAdReport.reportLeftApp(
                    "",
                    AdType.IDLE,
                    AdPlatform.IDLE,
                    "home",
                    seq2
            );
        });
        findViewById(R.id.button_track_paid).setOnClickListener(v ->
                ROIQueryAdReport.reportRewarded(
                        "",
                        AdType.IDLE,
                        AdPlatform.IDLE,
                        "home",
                        seq2
                )
        );
        findViewById(R.id.button_track_paid).setOnClickListener(v -> {
            HashMap<String, Object> pro = new HashMap<>();
            pro.put("ad_mediation_id", "sdkfjskldjfks");
            pro.put("ad_mediation", 3);

            ROIQueryAdReport.reportPaid("",
                    AdType.BANNER,
                    AdPlatform.MOPUB,
                    "home",
                    seq,
                    "5000",
                    "01",
                    "1",
                        pro
                    );

//            ROIQueryAdReport.reportPaid("",
//                    AdType.BANNER,
//                    AdPlatform.MOPUB,
//                    "home",
//                    seq,
//                    "5001",
//                    "01",
//                    "1");
//            ROIQueryAdReport.reportRewarded(
//                    "",
//                    AdType.BANNER,
//                    AdPlatform.MOPUB,
//                    "home",
//                    seq
//            );

        });

    }

}
