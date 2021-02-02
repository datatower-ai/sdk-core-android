package com.nodetower.roiquery_sdk



import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jraska.console.Console
import com.nodetower.analytics.api.IAnalyticsApi
import com.nodetower.analytics.api.PropertyBuilder
import com.nodetower.analytics.api.RoiqueryAnalyticsAPI


class AnalyticsTestActivity : AppCompatActivity() {

    var mApi: IAnalyticsApi? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)//左侧添加一个默认的返回图标
        supportActionBar?.setHomeButtonEnabled(true)
        title = "AnalyticsTest"

        mApi = RoiqueryAnalyticsAPI.getInstance(this)

        findViewById<View>(R.id.button_track).setOnClickListener {

            mApi?.apply {
                accountId = "7344506"
                track(
                    "test",
                    PropertyBuilder.newInstance()
                        .append("test_property_1", "自定义属性值1")
                        .append("test_property_2", "自定义属性值2")
                        .toJSONObject()
                )
            }
        }
        findViewById<View>(R.id.button_flush).setOnClickListener {
            mApi?.flush()
        }

        findViewById<View>(R.id.button_track_ad_show).setOnClickListener {
            mApi?.trackAdShow(
                PropertyBuilder.newInstance()
                    .append("ad_show_property_1", "广告展示了1")
                    .append("ad_show_property_2", "广告展示了2")
                    .toJSONObject()
            )
        }
        findViewById<View>(R.id.button_track_ad_click).setOnClickListener {
            mApi?.trackAdClick(
                PropertyBuilder.newInstance()
                    .append("ad_click_property_1", "点击了广告1")
                    .append("ad_click_property_2", "点击了广告2")
                    .toJSONObject()
            )
        }

        findViewById<View>(R.id.button_track_page_close).setOnClickListener {
            mApi?.trackPageClose(
                PropertyBuilder.newInstance()
                    .append("page_close_property_1", "关闭了页面1")
                    .append("page_close_property_2", "关闭了页面2")
                    .toJSONObject()
            )
        }
        findViewById<View>(R.id.button_track_app_close).setOnClickListener {
            mApi?.trackPageClose(
                PropertyBuilder.newInstance().append("app_close_property", "关闭了app").toJSONObject()
            )
        }

        findViewById<View>(R.id.button_clear_log).setOnClickListener {
            Console.clear()
        }


    }

    override fun onStart() {
        super.onStart()
        mApi?.trackPageOpen()
    }

    override fun onStop() {
        super.onStop()
        mApi?.trackPageClose()
    }

    //设置监听事件,点击返回按钮则退出当前页面
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }
}