package com.roiquery.analytics.api

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.util.Log
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.utils.JsonUtils
import org.json.JSONObject

class LifecycleObserverImpl : ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        ROIQueryAnalytics.onAppForeground(getStartReason(activity))
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        ROIQueryAnalytics.onAppBackground()
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    private fun getStartReason(currentActivity: Activity): String? {
        val startReasonJsonObj = JSONObject()
        val data = JSONObject()
        try {
            val activity: Activity = currentActivity
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
                    startReasonJsonObj.put("data", data)
                }
            }
        } catch (exception: Exception) {
            //exception.printStackTrace();
            return startReasonJsonObj.toString()
        }
        return startReasonJsonObj.toString()
    }

}