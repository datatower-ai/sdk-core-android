package com.nodetower.roiquery_sdk

import android.content.Context
import android.os.Bundle
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.nodetower.analytics.api.PropertyBuilder
import com.nodetower.analytics.api.RoiqueryAnalyticsAPI
import com.nodetower.base.utils.LogUtils


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_first)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<View>(R.id.button_track).setOnClickListener {

            RoiqueryAnalyticsAPI.getInstance(this)
                .track(
                    "test",
                    PropertyBuilder.newInstance().append("test", "test pro").toJSONObject()
                )
        }
        findViewById<View>(R.id.button_flush).setOnClickListener {

            RoiqueryAnalyticsAPI.getInstance(this)
                .flush()
        }
    }
}