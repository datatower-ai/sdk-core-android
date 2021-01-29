package com.nodetower.roiquery_sdk;

import android.app.Application;

import androidx.lifecycle.ProcessLifecycleOwner;

import com.nodetower.analytics.api.RoiqueryAnalyticsAPI;
import com.nodetower.analytics.config.AnalyticsConfigOptions;
import com.nodetower.base.utils.LogUtils;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initAnalytics();
    }

    private void initAnalytics() {
        AnalyticsConfigOptions analyticsConfigOptions = new AnalyticsConfigOptions("12345","https://api.roiquery.com")
                .setDebug(true);

        RoiqueryAnalyticsAPI.init(this,analyticsConfigOptions);
    }


}
