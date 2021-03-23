package com.roiquery.analytics_demo



import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jraska.console.Console
import com.roiquery.analytics.api.PropertyBuilder
import com.roiquery.analytics.api.ROIQueryAnalytics


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
            ROIQueryAnalytics.track(
                "app_open_like",
                PropertyBuilder.newInstance()
                    .append("test_property_1", "自定义属性值1")
                    .append("test_property_2", "自定义属性值2")
                    .toJSONObject()
            )
        }
        findViewById<View>(R.id.button_flush).setOnClickListener {
            ROIQueryAnalytics.flush()
        }

        findViewById<View>(R.id.button_track_ad_show).visibility= View.GONE
        findViewById<View>(R.id.button_track_ad_click).visibility= View.GONE
        findViewById<View>(R.id.button_clear_log).setOnClickListener {
            Console.clear()
        }

        findViewById<View>(R.id.button_track_page_close).setOnClickListener {
            ROIQueryAnalytics.trackPageClose(
                PropertyBuilder.newInstance()
                    .append("#sdk_type", "关闭了页面1")
                    .append("page_close_property_2", "关闭了页面2")
                    .toJSONObject()
            )
        }
        findViewById<View>(R.id.button_track_app_close).setOnClickListener {
            ROIQueryAnalytics.trackAppClose(
                PropertyBuilder.newInstance().append("app_close_property", "关闭了app").toJSONObject()
            )
        }

    }



    override fun onStart() {
        super.onStart()
        ROIQueryAnalytics.trackPageOpen()
    }

    override fun onStop() {
        super.onStop()
        ROIQueryAnalytics.trackPageClose()
    }

    //设置监听事件,点击返回按钮则退出当前页面
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }
}