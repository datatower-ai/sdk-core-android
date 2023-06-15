package com.roiquery.analytics_demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.roiquery.analytics_demo.ui.theme.DataTowerSDKCoreTheme

class DisplayAllApiActivity : AppCompatActivity() {
    companion object {
        val apiClasses = listOf(
            Pair("com.roiquery.analytics.DT", "DT"),
            Pair("com.roiquery.analytics.DTAnalytics", "DT Analytics"),
            Pair("com.roiquery.analytics.DTAnalyticsUtils", "DT Analytics Utils"),
            Pair("com.roiquery.ad.DTAdReport", "Ad"),
            Pair("com.roiquery.iap.DTIAPReport", "IAP"),
            Pair("com.roiquery.ias.DTIASReport", "IAS")
        )

        fun startActivity(context: Context) {
            context.startActivity(Intent(context, DisplayAllApiActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DataTowerSDKCoreTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ApiList()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ApiList() {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    Column(Modifier.verticalScroll(rememberScrollState())) {
        Button(onClick = {
            var result = ""
            DisplayAllApiActivity.apiClasses.forEach {
                val funcs = getFuncNamesFromClass(it.first)
                result += "=====================\n" +
                        "${it.second} (num of API: ${funcs.size})\n" +
                        "=====================\n"
                result += funcs.reduce { acc, s ->
                    "$acc\n$s"
                }
                result += "\n\n"
            }
            clipboardManager.setText(AnnotatedString(result))
        }) {
            Text("Copy")
        }

        DisplayAllApiActivity.apiClasses.forEach {
            SubApiList(clazzName = it.first, title = it.second)
            Divider()
        }
    }
}

@Composable
private fun SubApiList(clazzName: String, title: String) {
    val allApiNames = remember { mutableStateListOf<String>("<Loading...>") }
    LaunchedEffect(Unit) {
        allApiNames.clear()
        allApiNames.addAll(getFuncNamesFromClass(clazzName))
    }

    Column(Modifier.padding(horizontal = 10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 5.dp)
        )
        allApiNames.forEach {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 5.dp, horizontal = 5.dp)
            )
        }
    }
}

fun getFuncNamesFromClass(clazzName: String): Set<String> = mutableSetOf<String>().apply {
        Class.forName(clazzName).declaredMethods.forEach {
            val name = it.name
            if (
                name.contains("\$")     // Kotlin internal method
                || it.modifiers and java.lang.reflect.Modifier.PRIVATE == java.lang.reflect.Modifier.PRIVATE
            ) return@forEach
            add(it.name)
        }
    }