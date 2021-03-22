package com.nodetower.demo_cloudconfig

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.roiquery.analytics.utils.FileUtils
import com.roiquery.analytics.utils.LogUtils
import com.roiquery.cloudconfig.*

class MainActivity : AppCompatActivity() {





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        demo()

    }

    private fun demo() {
        clear()
        printCurrentConfig()
        showDefault()
        showFresh()
    }

    private fun clear() {
//        remoteAppConfig.clear()
    }

    private fun showDefault() {
//        remoteAppConfig.setDefaultConfig("This is the default welcome message.")
        printCurrentConfig()
    }

    private fun showFresh() {
        ROIQueryCloudConfig.fetch({
            LogUtils.e("Fetch is successful")
//            remoteAppConfig.activateFetched()
            printCurrentConfig()
        }, {
            LogUtils.e("Fetch is failed: ${it.message}")
        })
    }

    private fun printCurrentConfig() {
        LogUtils.e("Config: ${ROIQueryCloudConfig.getInt("welcomeMessage")}")
    }


}