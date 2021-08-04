package com.roiquery.ad_report_demo;

import android.app.Application;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.roiquery.analytics.ROIQueryAnalytics;
import com.roiquery.analytics.ROIQueryChannel;
import com.roiquery.analytics.ROIQuery;

import java.util.Map;

public class MyApp2 extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ROIQuery.initSDK(this,"android_ad", ROIQueryChannel.GP,true);
        ROIQueryAnalytics.setFirebaseAppInstanceId("");
        AppsFlyerLib.getInstance().registerConversionListener(this,new AppsFlyerConversionListener(){

            @Override
            public void onConversionDataSuccess(Map<String, Object> map) {
                String ID = (String) map.get("appsflyer_id ");
            }

            @Override
            public void onConversionDataFail(String s) {

            }

            @Override
            public void onAppOpenAttribution(Map<String, String> map) {

            }

            @Override
            public void onAttributionFailure(String s) {

            }
        });
    }
}
