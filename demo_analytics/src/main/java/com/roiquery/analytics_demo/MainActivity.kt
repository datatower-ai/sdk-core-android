package com.roiquery.analytics_demo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.roiquery.analytics.ROIQueryAnalytics
import org.json.JSONObject
import org.koin.android.ext.koin.ERROR_MSG
import java.io.*
import java.util.HashMap

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("test","MainActivity --")
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<View>(R.id.button_analytics_core).setOnClickListener {
            startActivity(Intent(this, CoreActivity::class.java))
        }

        findViewById<View>(R.id.button_ad_report).setOnClickListener {
            startActivity(Intent(this, AdReportActivity::class.java))
        }


        findViewById<View>(R.id.button_iap_report).setOnClickListener {
            startActivity(Intent(this, IapReportActivity::class.java))
        }

        findViewById<View>(R.id.button_ias_report).setOnClickListener {
            startActivity(Intent(this, IasReportActivity::class.java))
        }

        findViewById<View>(R.id.button_test_memory).setOnClickListener {
            params["SOURCE"] = "source"
            params["RESULT"] = "result"
            params["ERROR_MSG"] = "errorMessage"
            ROIQueryAnalytics.track("SERVERS_REFRESH_FINISH1", params)
            params.clear()

            params["SOURCE"] = "source"
            params["RESULT"] = "result"
            params["ERROR_MSG"] = "errorMessage"
            ROIQueryAnalytics.track("SERVERS_REFRESH_FINISH2", params)
            params.clear()
        }
    }

    private val params: MutableMap<String, Any> = HashMap()

    private fun track1() {
        params["SOURCE"] = "source"
        params["RESULT"] = "result"
        params["ERROR_MSG"] = "errorMessage"
        ROIQueryAnalytics.track("SERVERS_REFRESH_FINISH1", params)
        params.clear()
    }

    private fun track2() {
        params["SOURCE"] = "source"
        params["RESULT"] = "result"
        params["ERROR_MSG"] = "errorMessage"
        ROIQueryAnalytics.track("SERVERS_REFRESH_FINISH2", params)
        params.clear()
    }





//    public String getFromAssets(String fileName) {
//        try {
//            InputStreamReader inputReader = new InputStreamReader(getResources().getAssets().open(fileName));
//            BufferedReader bufReader = new BufferedReader(inputReader);
//            String line = "";
//            String result = "";
//            while ((line = bufReader.readLine()) != null)
//                result += line;
//            return result;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }

    fun readAssetsTxt(fileName: String): String?
    {
        val input = assets.open("$fileName")
        val instruments = BufferedReader(InputStreamReader(input))
        try
        {

//            Log.e("获取的assets文本内容----", instruments.readLines().toString()!!)
            return instruments.readLines().toString()
        }
        catch (e: IOException)
        {
            e.printStackTrace()
        }
        finally
        {
            instruments.close()
        }
        return "读取失败,请检查文件名称及文件是否存在!"
    }


}