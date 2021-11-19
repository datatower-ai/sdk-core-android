package com.roiquery.analytics.api

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.roiquery.analytics.ROIQueryAnalytics

class LifecycleObserverImpl: LifecycleEventObserver {

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_START) {
            ROIQueryAnalytics.onAppForeground()
        }else if (event == Lifecycle.Event.ON_STOP){
            ROIQueryAnalytics.onAppBackground()
        }
    }
}