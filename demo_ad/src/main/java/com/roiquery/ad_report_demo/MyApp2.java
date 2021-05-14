package com.roiquery.ad_report_demo;

import android.app.Application;

import com.roiquery.analytics.ROIQueryChannel;
import com.roiquery.analytics.ROIQuery;

public class MyApp2 extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ROIQuery.initSDK(this,"android_ad", ROIQueryChannel.GP,true);
    }
}
