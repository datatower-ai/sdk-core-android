package com.roiquery.analytics_demo;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.roiquery.analytics.DTAnalytics;
import com.roiquery.analytics.ROIQueryAnalytics;

import org.json.JSONArray;
import org.json.JSONObject;

public class UserSetTest extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_set);
        findViewById(R.id.set).setOnClickListener(v -> {
            try {
                JSONObject jsonObject  = new JSONObject();
                jsonObject.put("test_property_3", false);
                jsonObject.put("test_property_4", 2.3);
                DTAnalytics.userSet(jsonObject);
            }catch (Exception ignored){

            }

        });

        findViewById(R.id.set_once).setOnClickListener(v -> {
            try {
                JSONObject jsonObject  = new JSONObject();
                jsonObject.put("test_property_3", false);
                jsonObject.put("test_property_4", 2.3);
                DTAnalytics.userSetOnce(jsonObject);
            }catch (Exception ignored){

            }
        });

        findViewById(R.id.add).setOnClickListener(v -> {
            try {
                JSONObject jsonObject  = new JSONObject();
                jsonObject.put("test_property_3", false);
                jsonObject.put("test_property_4", 2.3);
                DTAnalytics.userAdd(jsonObject);
            }catch (Exception ignored){

            }
        });

        findViewById(R.id.un_set).setOnClickListener(v -> {
            try {
                JSONObject jsonObject  = new JSONObject();
                jsonObject.put("test_property_3", false);
                jsonObject.put("test_property_4", 2.3);
                DTAnalytics.userUnset("key","key2","key3");
            }catch (Exception ignored){

            }
        });

        findViewById(R.id.delete).setOnClickListener(v -> {
            try {
                DTAnalytics.userDelete();
            }catch (Exception ignored){

            }
        });

        findViewById(R.id.append).setOnClickListener(v -> {
            try {
                JSONArray list = new JSONArray("[\"apple\", \"ball\"]");
                list.put("orage");
                JSONObject properties = new JSONObject();
                properties.put("user_list", list);
                DTAnalytics.userAppend(properties);
            }catch (Exception ignored){

            }
        });
    }
}
