package com.roiquery.ad_report_demo;

import android.app.Application;

import androidx.annotation.NonNull;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.roiquery.analytics.ROIQueryAnalytics;
import com.roiquery.analytics.ROIQueryChannel;
import com.roiquery.analytics.ROIQuery;
import com.roiquery.analytics.utils.LogUtils;
import com.roiquery.cloudconfig.ROIQueryCloudConfig;
import com.roiquery.cloudconfig.core.ConfigFetchListener;

import java.util.Map;

public class MyApp2 extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ROIQuery.initSDK(this,"android_ad", ROIQueryChannel.GP,true);
        AppsFlyerLib.getInstance().getAppsFlyerUID(this);
        ROIQueryCloudConfig.fetch(new ConfigFetchListener() {
            @Override
            public void onSuccess() {
                String ad = ROIQueryCloudConfig.getString("ad_config");
                LogUtils.i(ad);
            }

            @Override
            public void onError(@NonNull String errorMessage) {

            }
        });

    }
}
