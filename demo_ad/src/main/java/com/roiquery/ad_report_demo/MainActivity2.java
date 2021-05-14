package com.roiquery.ad_report_demo;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.installations.FirebaseInstallations;
import com.roiquery.ad.AD_PLATFORM;
import com.roiquery.ad.AD_TYPE;
import com.roiquery.ad.ROIQueryAdReport;
import com.roiquery.ad.utils.UUIDUtils;
import com.roiquery.analytics.utils.LogUtils;
import com.roiquery.analytics.utils.ThreadUtils;

import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;


public class MainActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_report);

        String seq = UUIDUtils.generateUUID();
        findViewById(R.id.button_track_entrance).setOnClickListener(v -> {
            ROIQueryAdReport.reportEntrance(
                    "1",
                    AD_TYPE.BANNER,
                    AD_PLATFORM.MOPUB,
                    "home",
                    seq,
                    "main"
            );
        });

        findViewById(R.id.button_track_to_show).setOnClickListener(v -> {
            ROIQueryAdReport.reportToShow(
                    "2",
                    AD_TYPE.INTERSTITIAL,
                    AD_PLATFORM.MOPUB,
                    "user",
                    seq,
                    "main"
            );
        });
        findViewById(R.id.button_track_show).setOnClickListener(v ->
                ROIQueryAdReport.reportShow(
                        "3",
                        AD_TYPE.BANNER,
                        AD_PLATFORM.MOPUB,
                        "car",
                        seq,
                        "home"
                )
        );
        findViewById(R.id.button_track_close).setOnClickListener(v ->
                ROIQueryAdReport.reportClose(
                        "4",
                        AD_TYPE.BANNER,
                        AD_PLATFORM.MOPUB,
                        "home",
                        seq,
                        "main"
                )
        );
        findViewById(R.id.button_track_click).setOnClickListener(v -> {
            ROIQueryAdReport.reportClick(
                    "5",
                    AD_TYPE.BANNER,
                    AD_PLATFORM.ADMOB,
                    "home",
                    seq,
                    "main"
            );

            //从其他浏览器打开
            Uri uri = Uri.parse("https://www.baidu.com");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);

            ROIQueryAdReport.reportLeftApp(
                    "",
                    AD_TYPE.BANNER,
                    AD_PLATFORM.MOPUB,
                    "home",
                    seq,
                    "main"
            );
        });
        findViewById(R.id.button_track_rewarded).setOnClickListener(v ->
                ROIQueryAdReport.reportRewarded(
                        "",
                        AD_TYPE.BANNER,
                        AD_PLATFORM.MOPUB,
                        "home",
                        seq,
                        "main"
                )
        );
        findViewById(R.id.button_track_sample).setOnClickListener(v -> {
            ROIQueryAdReport.reportPaid("",
                    AD_TYPE.BANNER,
                    AD_PLATFORM.MOPUB,
                    "home",
                    seq,
                    "5000",
                    "01",
                    "1",
                    "main");

            ROIQueryAdReport.reportPaid("",
                    AD_TYPE.BANNER,
                    AD_PLATFORM.MOPUB,
                    "home",
                    seq,
                    "5001",
                    "01",
                    "1",
                    "main");
            ROIQueryAdReport.reportRewarded(
                    "",
                    AD_TYPE.BANNER,
                    AD_PLATFORM.MOPUB,
                    "home",
                    seq,
                    "main"
            );

        });

    }

}
