package com.nodetower.demo_cloudconfig

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.roiquery.analytics.utils.FileUtils
import com.roiquery.analytics.utils.LogUtils
import com.roiquery.cloudconfig.*

class MainActivity : AppCompatActivity() {



    private val remoteAppConfig by lazy { remoteConfig<String>() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        demo()
    }

    private fun demo() {
        initRemoteConfig {
            remoteResource<String>(
                storage(filesDir.absolutePath + "/configs"),
                network("https://demo7865768.mockable.io/messages.json")
            ) {
                resourceName = "welcome-config"
            }
        }
        clear()
        printCurrentConfig()
        showDefault()
        showFresh()
    }

    private fun clear() {
        remoteAppConfig.clear()
    }

    private fun showDefault() {
        remoteAppConfig.setDefaultConfig("This is the default welcome message.")
        printCurrentConfig()
    }

    private fun showFresh() {
        remoteAppConfig.fetch({
            LogUtils.e("Fetch is successful")
            remoteAppConfig.activateFetched()
            printCurrentConfig()
        }, {
            LogUtils.e("Fetch is failed: ${it.message}")
        })
    }

    private fun printCurrentConfig() {
        LogUtils.e("Config: ${remoteAppConfig.get()}")
    }

    data class AppConfig(
        val welcomeMessage: String?
    )
}