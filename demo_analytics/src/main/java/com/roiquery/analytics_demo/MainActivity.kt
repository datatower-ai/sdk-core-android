package com.roiquery.analytics_demo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.roiquery.analytics.ROIQueryAnalytics
import org.json.JSONObject
import java.io.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            Thread {

                ROIQueryAnalytics.track("test", JSONObject().apply {
                    put("big_text",readAssetsTxt("test.txt"))
                })
            }.start()
        }
    }

    private fun addMemory() {
        val count = 2000
        for (i in 0 until count) {
            //mStringBuilder.append(mTempStr);
            val filename = "temp$i"
            val file = File(cacheDir, filename)
            try {
                file.createNewFile()
                val out = FileOutputStream(file)
            } catch (e: FileNotFoundException) {
            } catch (e: IOException) {
            }
        }
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