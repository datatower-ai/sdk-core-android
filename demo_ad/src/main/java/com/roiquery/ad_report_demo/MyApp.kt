package com.roiquery.ad_report_demo

import android.R
import android.app.Application
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import com.roiquery.analytics.ROIQuery
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.api.AbstractAnalytics.Companion.TAG
import com.roiquery.analytics.utils.LogUtils


class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ROIQuery.initSDK(this, "android_ad", true)
        FirebaseApp.initializeApp(this)
        FirebaseAnalytics.getInstance(this).appInstanceId.addOnSuccessListener {
            ROIQueryAnalytics.setFirebaseAppInstanceId(it)
        }


    }

}