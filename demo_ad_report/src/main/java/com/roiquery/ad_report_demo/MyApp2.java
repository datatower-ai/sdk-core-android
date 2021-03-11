package com.roiquery.ad_report_demo;

import android.app.Application;

import com.roiquery.analytics.ROIQuerySDK;

public class MyApp2 extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ROIQuerySDK.init(this,"android_ad_report_demo",true);
    }
}
