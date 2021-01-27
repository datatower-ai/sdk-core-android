package com.nodetower.roiquery_sdk;

import android.app.Application;

import androidx.lifecycle.ProcessLifecycleOwner;

import com.nodetower.analytics.api.RoiqueryAnalyticsAPI;
import com.nodetower.analytics.config.AnalyticsConfigOptions;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initAnalytics();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new CheckObserver());
    }

    private void initAnalytics() {
        AnalyticsConfigOptions analyticsConfigOptions = new AnalyticsConfigOptions("www.baidu.com");
        analyticsConfigOptions.enableLog(true);
        RoiqueryAnalyticsAPI.init(this,analyticsConfigOptions);
    }


}
