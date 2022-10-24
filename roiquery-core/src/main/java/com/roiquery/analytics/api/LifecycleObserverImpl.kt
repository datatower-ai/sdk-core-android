package com.roiquery.analytics.api

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.utils.JsonUtils
import org.json.JSONObject
import java.lang.ref.WeakReference

class LifecycleObserverImpl : ActivityLifecycleCallbacks {

    private var mCurrentActivity: WeakReference<Activity?>? = null
    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object: LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_START) {
                    ROIQueryAnalytics.onAppForeground(getStartReason())
                }else if (event == Lifecycle.Event.ON_STOP){
                    ROIQueryAnalytics.onAppBackground()
                }
            }
        })
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        mCurrentActivity = WeakReference(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        mCurrentActivity = WeakReference(activity)
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    private fun getStartReason(): String? {
        val startReasonJsonObj = JSONObject()
        val data = JSONObject()
        try {
            mCurrentActivity?.get()?.let {activity->
            val intent = activity.intent
            if (intent != null) {
                val uri = intent.dataString
                if (uri?.isNotEmpty() == true) {
                    startReasonJsonObj.put("url", uri)
                }
                val bundle = intent.extras
                bundle?.let {
                    val keys = bundle.keySet()
                    for (key in keys) {
                        val value = bundle[key]
                        val supportValue: Any? = JsonUtils.wrap(
                            value
                        )
                        if (supportValue != null && supportValue !== JSONObject.NULL) {
                            data.put(
                                key, JsonUtils.wrap(
                                    value
                                )
                            )
                        }
                    }
                    if (data.toString() != "{}") {
                        startReasonJsonObj.put("data", data)
                    }
                }
            }
            }
        } catch (exception: Exception) {
            //exception.printStackTrace();
            return null
        }
        return  if (startReasonJsonObj.toString() == "{}"){
             null
        }else{
            startReasonJsonObj.toString()
        }
    }

}