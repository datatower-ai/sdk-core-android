package com.nodetower.roiquery_sdk

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class CheckObserver : LifecycleObserver {
    private var time = 0L

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        time = System.currentTimeMillis()
        Log.d("cxg", "后台 $time")
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        Log.d("cxg", "前台 $time")
        if (time > 1 && System.currentTimeMillis() - time > 10000) {
            Log.d("cxg", "check=======")
        }
    }
}