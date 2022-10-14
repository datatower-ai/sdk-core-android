package com.roiquery.analytics_demo



import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jraska.console.Console
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.api.ServerTimeListener
import com.roiquery.analytics.utils.LogUtils
import org.json.JSONObject


class AnalyticsTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)//左侧添加一个默认的返回图标
        supportActionBar?.setHomeButtonEnabled(true)
        title = "AnalyticsTest"

        findViewById<View>(R.id.button_track).setOnClickListener {
            ROIQueryAnalytics.setAccountId("7344506")
            ROIQueryAnalytics.track("app_open_like")
        }
        findViewById<View>(R.id.button_flush).setOnClickListener {
//            ROIQueryAnalytics.flush()
//                LogUtils.d()
//            Thread {
//                val time = ROIQueryAnalytics.getServerTimeSync()
//                LogUtils.d("getServerTimeSync", time)
//            }.start()

        }

        findViewById<View>(R.id.button_track_ad_show).visibility= View.GONE
        findViewById<View>(R.id.button_track_ad_click).visibility= View.GONE

        findViewById<View>(R.id.button_track_page_close).setOnClickListener {
//            ROIQueryAnalytics.trackPageClose()

        }
        findViewById<View>(R.id.button_track_app_close).setOnClickListener {
//            ROIQueryAnalytics.trackAppClose()
        }

        ROIQueryAnalytics.setFCMToken("sdfsdfdfsdf")
    }



    override fun onStart() {
        super.onStart()
//        ROIQueryAnalytics.trackPageOpen()
    }

    override fun onStop() {
        super.onStop()
//        ROIQueryAnalytics.trackPageClose()
    }

    //设置监听事件,点击返回按钮则退出当前页面
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }
}