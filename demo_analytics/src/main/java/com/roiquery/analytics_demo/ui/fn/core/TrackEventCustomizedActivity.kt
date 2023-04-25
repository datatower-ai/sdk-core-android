package com.roiquery.analytics_demo.ui.fn.core

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

class TrackEventCustomizedActivity : AppCompatActivity() {

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, TrackEventCustomizedActivity::class.java))
        }
    }
}
