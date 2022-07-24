package com.roiquery.ad_report_demo;

import android.app.Application;

import androidx.annotation.NonNull;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.roiquery.ad.AdPlatform;
import com.roiquery.analytics.ROIQueryAnalytics;
import com.roiquery.analytics.ROIQueryChannel;
import com.roiquery.analytics.ROIQuery;
import com.roiquery.analytics.utils.LogUtils;
import com.roiquery.cloudconfig.ROIQueryCloudConfig;
import com.roiquery.cloudconfig.core.ConfigFetchListener;

import org.json.JSONObject;

import java.util.Map;

public class MyApp2 extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //rq_unity_demo
        ROIQuery.initSDK(this,"rq_nocard", ROIQueryChannel.GP,true);
        AppsFlyerLib.getInstance().getAppsFlyerUID(this);

        ROIQueryCloudConfig.fetch(new ConfigFetchListener() {
            @Override
            public void onSuccess() {
                String ad = ROIQueryCloudConfig.getString("ad_config");
                String config4 = ROIQueryCloudConfig.getString("adc");
                int config5 = ROIQueryCloudConfig.getInt("test_int");
                boolean config6 = ROIQueryCloudConfig.getBoolean("test_boolean");
                double config7 = ROIQueryCloudConfig.getDouble("double_key");
                JSONObject config8 = ROIQueryCloudConfig.getJsonObject("test_json");
                LogUtils.i(ad);
            }

            @Override
            public void onError(@NonNull String errorMessage) {

            }
        });

    }
}
