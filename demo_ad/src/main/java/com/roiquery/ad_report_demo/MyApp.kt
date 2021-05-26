package com.roiquery.ad_report_demo

import android.app.Application

import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.roiquery.analytics.ROIQuery
import com.roiquery.analytics.ROIQueryAnalytics



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