package com.nodetower.demo_cloudconfig

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.infinum.dbinspector.DbInspector
import com.roiquery.analytics.utils.LogUtils
import com.roiquery.cloudconfig.*
import com.roiquery.cloudconfig.utils.AESCoder

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.tv_test).setOnClickListener {
            DbInspector.show()
        }
        findViewById<View>(R.id.tv_fetch).setOnClickListener {
            showFresh()
        }
        demo()
//        AESCoder.main()
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
        LogUtils.e("Config: ${ROIQueryCloudConfig.getConfigString()}")
    }


}