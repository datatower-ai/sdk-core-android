package com.roiquery.analytics_demo;


import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.roiquery.analytics.ROIQueryAnalytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class CoreActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setTitle("Core");

        String acid = (String) SharedPreferencesUtils.Companion.getParam(this, "acid", "");
        String fiid = (String) SharedPreferencesUtils.Companion.getParam(this, "fiid", "");
        String fcm_token = (String) SharedPreferencesUtils.Companion.getParam(this, "fcm_token", "");
        String afid = (String) SharedPreferencesUtils.Companion.getParam(this, "afid", "");
        String asid = (String) SharedPreferencesUtils.Companion.getParam(this, "asid", "");
        String koid = (String) SharedPreferencesUtils.Companion.getParam(this, "koid", "");

        TextView tv_acid = findViewById(R.id.tv_acid);
        TextView tv_fiid = findViewById(R.id.tv_fiid);
        TextView tv_fcm = findViewById(R.id.tv_fcm);
        TextView tv_afid = findViewById(R.id.tv_afid);
        TextView tv_asid = findViewById(R.id.tv_asid);
        TextView tv_koid = findViewById(R.id.tv_koid);

        tv_acid.setText(acid);
        tv_fiid.setText(fiid);
        tv_fcm.setText(fcm_token);
        tv_afid.setText(afid);
        tv_asid.setText(asid);
        tv_koid.setText(koid);


        findViewById(R.id.button_set_acid).setOnClickListener(v -> {
            ROIQueryAnalytics.setAccountId(acid);
        });

        findViewById(R.id.button_set_fiid).setOnClickListener(v -> {
            ROIQueryAnalytics.setFirebaseAppInstanceId(fiid);
        });

        findViewById(R.id.button_fcm).setOnClickListener(v -> {
            ROIQueryAnalytics.setFCMToken(fcm_token);
        });

        findViewById(R.id.button_afid).setOnClickListener(v -> {
            ROIQueryAnalytics.setAppsFlyerId(afid);
        });

        findViewById(R.id.button_asid).setOnClickListener(v -> {
            ROIQueryAnalytics.setAppSetId(asid);
        });

        findViewById(R.id.button_set_koid).setOnClickListener(v -> {
            ROIQueryAnalytics.setKochavaId(koid);
        });

        findViewById(R.id.button_track).setOnClickListener(v -> {
            ROIQueryAnalytics.track("dt_track_simple");
        });

        findViewById(R.id.button_track_illegal_event_name).setOnClickListener(v -> {
            ROIQueryAnalytics.track("#dt_track_simple");
        });

        findViewById(R.id.button_track_illegal_property_1).setOnClickListener(v -> {
            try {
                JSONObject property = new JSONObject();
                property.put("#property_1","sdf");
                ROIQueryAnalytics.track("dt_track_simple_2", property);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        findViewById(R.id.button_track_illegal_property_2).setOnClickListener(v -> {
            try {
                JSONObject property = new JSONObject();
                property.put("$property_2","sdf");
                ROIQueryAnalytics.track("dt_track_simple_3", property);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });


        findViewById(R.id.set).setOnClickListener(v -> {
            try {
                JSONObject jsonObject  = new JSONObject();
                jsonObject.put("test_property_3", false);
                jsonObject.put("test_property_4", 2.3);
                ROIQueryAnalytics.userSet(jsonObject);
            }catch (Exception ignored){

            }

        });

        findViewById(R.id.set_once).setOnClickListener(v -> {
            try {
                JSONObject jsonObject  = new JSONObject();
                jsonObject.put("test_property_3", false);
                jsonObject.put("test_property_4", 2.3);
                ROIQueryAnalytics.userSetOnce(jsonObject);
            }catch (Exception ignored){

            }
        });

        findViewById(R.id.add).setOnClickListener(v -> {
            try {
                JSONObject jsonObject  = new JSONObject();
                jsonObject.put("test_property_3", false);
                jsonObject.put("test_property_4", 2.3);
                ROIQueryAnalytics.userAdd(jsonObject);
            }catch (Exception ignored){

            }
        });

        findViewById(R.id.un_set).setOnClickListener(v -> {
            try {
                JSONObject jsonObject  = new JSONObject();
                jsonObject.put("test_property_3", false);
                jsonObject.put("test_property_4", 2.3);
                ROIQueryAnalytics.userUnset("key","key2","key3");
            }catch (Exception ignored){

            }
        });

        findViewById(R.id.delete).setOnClickListener(v -> {
            try {
                ROIQueryAnalytics.userDelete();
            }catch (Exception ignored){

            }
        });

        findViewById(R.id.append).setOnClickListener(v -> {
            try {
                JSONArray list = new JSONArray("[\"apple\", \"ball\"]");
                list.put("orage");
                JSONObject properties = new JSONObject();
                properties.put("user_list", list);
                ROIQueryAnalytics.userAppend(properties);
            }catch (Exception ignored){

            }
        });
    }

}
