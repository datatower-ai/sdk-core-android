package com.nodetower.demo_cloudconfig

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.infinum.dbinspector.DbInspector
import com.roiquery.analytics.utils.LogUtils
import com.roiquery.cloudconfig.*
import com.roiquery.cloudconfig.core.ConfigFetchListener

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
        findViewById<View>(R.id.tv_logih).setOnClickListener {
//
//            ROIQueryAnalytics.setAccountId("7344506")
//            ROIQueryAnalytics.track(
//                "app_open_like",
//                PropertyBuilder.newInstance()
//                    .append("test_property_1", "自定义属性值1")
//                    .append("test_property_2", "自定义属性值2")
//                    .toJSONObject()
//            )
            printCurrentConfig()
        }
        demo()
    }

    private fun demo() {
        clear()
        printCurrentConfig()
        showDefault()
//        showFresh()
    }

    private fun clear() {
//        remoteAppConfig.clear()
    }

    private fun showDefault() {
//        remoteAppConfig.setDefaultConfig("This is the default welcome message.")
        printCurrentConfig()
    }

    private fun showFresh() {
        ROIQueryCloudConfig.fetch(object :ConfigFetchListener{
            override fun onSuccess() {
                printCurrentConfig()
            }

            override fun onError(errorMessage: String) {
                LogUtils.e(errorMessage)
            }

        })
    }

    private fun printCurrentConfig() {
        LogUtils.e("Config3: ${ROIQueryCloudConfig.getConfigString()}")
    }


}