package com.roiquery.analytics_demo;


import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.roiquery.analytics.DT;
import com.roiquery.analytics.DTAnalytics;
import com.roiquery.analytics.DTChannel;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kotlin.Function;
import kotlin.Unit;


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
        String adjustId = (String) SharedPreferencesUtils.Companion.getParam(this,"adjustId","");

        TextView tv_acid = findViewById(R.id.tv_acid);
        TextView tv_fiid = findViewById(R.id.tv_fiid);
        TextView tv_fcm = findViewById(R.id.tv_fcm);
        TextView tv_afid = findViewById(R.id.tv_afid);
        TextView tv_asid = findViewById(R.id.tv_asid);
        TextView tv_koid = findViewById(R.id.tv_koid);
        TextView tvAdjust =findViewById(R.id.tv_adjust);

        tv_acid.setText(acid);
        tv_fiid.setText(fiid);
        tv_fcm.setText(fcm_token);
        tv_afid.setText(afid);
        tv_asid.setText(asid);
        tv_koid.setText(koid);
        tvAdjust.setText(adjustId);


        findViewById(R.id.button_set_acid).setOnClickListener(v -> {
            DTAnalytics.setAccountId(acid);
        });

        findViewById(R.id.button_set_fiid).setOnClickListener(v -> {
            DTAnalytics.setFirebaseAppInstanceId(fiid);
        });

        findViewById(R.id.button_fcm).setOnClickListener(v -> {
        });

        findViewById(R.id.button_afid).setOnClickListener(v -> {
            DTAnalytics.setAppsFlyerId(afid);
        });

        findViewById(R.id.button_asid).setOnClickListener(v -> {
        });

        findViewById(R.id.button_set_koid).setOnClickListener(v -> {
            DTAnalytics.setKochavaId(koid);
        });

        findViewById(R.id.button_set_adjust_id).setOnClickListener(
                v -> {
                    DTAnalytics.setAdjustId(adjustId);
                }
        );

        findViewById(R.id.button_track).setOnClickListener(v -> {
            DTAnalytics.track("dt_track_simple");
        });

        findViewById(R.id.button_track_illegal_event_name).setOnClickListener(v -> {
            DTAnalytics.track("#dt_track_simple");
        });

        findViewById(R.id.button_track_illegal_property_1).setOnClickListener(v -> {
            try {
                JSONObject property = new JSONObject();
                property.put("#property_1","sdf");
                DTAnalytics.track("dt_track_simple_2", property);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        findViewById(R.id.button_track_illegal_property_2).setOnClickListener(v -> {
            try {
                JSONObject property = new JSONObject();
                property.put("$property_2","sdf");
                DTAnalytics.track("dt_track_simple_3", property);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });


        findViewById(R.id.set).setOnClickListener(v -> {
            try {
                JSONObject jsonObject  = new JSONObject();
                jsonObject.put("test_property_3", false);
                jsonObject.put("test_property_4", 2.3);
                DTAnalytics.userSet(jsonObject);
            } catch (Exception ignored){

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
