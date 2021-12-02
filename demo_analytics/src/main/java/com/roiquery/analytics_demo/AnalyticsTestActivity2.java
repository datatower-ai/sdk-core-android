package com.roiquery.analytics_demo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.roiquery.analytics.ROIQueryAnalytics;

import org.json.JSONObject;

import java.util.HashMap;

public class AnalyticsTestActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true);
        setTitle("AnalyticsTest");

        findViewById(R.id.button_track).setOnClickListener(v -> {

            ROIQueryAnalytics.setAccountId("7344506");
            HashMap<String, Object> pro = new HashMap<>();
            pro.put("test_property_3", false);
            pro.put("test_property_4", 2.3);

            ROIQueryAnalytics.track(
                    "test", pro

            );
        });

        findViewById(R.id.button_track_app_close).setOnClickListener(v -> {

            HashMap<String, Object> pro = new HashMap<>();
            pro.put("user_sex_man", false);
            pro.put("user_pwd", "5201314");
            pro.put("user_age", 23);
            pro.put("user_money", 23000.01);

        });


    }

    private void test1(String name) {
        ROIQueryAnalytics.setAccountId("7344506");
        HashMap<String, Object> pro = new HashMap<>();
        pro.put("test_property_3", false);
        pro.put("test_property_4", 2.3);

        ROIQueryAnalytics.track(
                "test_" + name, pro

        );
    }

    private void test2() {
        try {
            JSONObject pro = new JSONObject();
            pro.put("user_sex_man", false);
            pro.put("user_pwd", "5201314");
            pro.put("user_age", 23);
            pro.put("user_money", 23000.01);

            ROIQueryAnalytics.userSet(
                    pro
            );
        }catch (Exception ignored){

        }


    }
}
