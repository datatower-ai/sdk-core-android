package ai.datatower.analytics_demo.ui

import ai.datatower.ad.DTAdReport
import ai.datatower.analytics.DT
import ai.datatower.analytics.DTAnalytics
import ai.datatower.analytics.DTAnalyticsUtils
import ai.datatower.analytics.DTChannel
import ai.datatower.analytics.OnDataTowerIdListener
import ai.datatower.analytics_demo.SharedPreferencesUtils
import ai.datatower.analytics_demo.ui.theme.DataTowerSDKCoreTheme
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class StartupActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DataTowerSDKCoreTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SetupScreenContent { finish() }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupScreenContent(finishFunc: () -> Unit) {
    var serverUrl by remember { mutableStateOf("") }
    var appId by remember { mutableStateOf("") }
    var isDebug by remember { mutableStateOf(true) }
    var manualEnableUpload by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var logLevel by remember { mutableStateOf(Log.DEBUG) }
    val logLevelTextMap = mapOf(
        Log.DEBUG to "Debug",
        Log.INFO to "Info",
        Log.WARN to "Warn",
        Log.ERROR to "Error"
    )
    var isServerUrlError by remember { mutableStateOf(false) }
    var isAppIdError by remember { mutableStateOf(false) }

    val context = LocalContext.current;

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Initialize") },
                icon = { Icon(Icons.Rounded.Check, "") },
                onClick = {
                    if (serverUrl.isEmpty()) {
                        isServerUrlError = true
                    }
                    if (appId.isEmpty()) {
                        isAppIdError = true
                    }

                    if (!isServerUrlError && !isAppIdError) {
                        initDT(context, serverUrl, appId, isDebug, manualEnableUpload)
                        context.startActivity(Intent(context, MainActivity::class.java))
                        finishFunc()
                    }
                })
        },
    ) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .padding(horizontal = 60.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "DT SDK Demo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = serverUrl,
                onValueChange = {
                    serverUrl = it
                    isServerUrlError = false
                },
                label = { Text("Server Url") },
                modifier = Modifier.fillMaxWidth(),
                isError = isServerUrlError,
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = appId,
                onValueChange = {
                    appId = it
                    isAppIdError = false
                },
                label = { Text("App id") },
                supportingText = { Text("*Required") },
                modifier = Modifier.fillMaxWidth(),
                isError = isAppIdError,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Is debug", modifier = Modifier.weight(1f))
                Switch(checked = isDebug, onCheckedChange = { isDebug = it })
            }
            if (isDebug) {
                Spacer(modifier = Modifier.height(10.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = logLevelTextMap[logLevel] ?: "",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        label = { Text("Log Level") },
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }) {
                        logLevelTextMap.forEach { (id, title) ->
                            DropdownMenuItem(
                                text = { Text(title) },
                                onClick = {
                                    logLevel = id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Manually enable upload", modifier = Modifier.weight(1f))
                Switch(checked = manualEnableUpload, onCheckedChange = { manualEnableUpload = it })
            }
        }
    }
}

private fun initDT(context: Context, serverUrl: String, appId: String, isDebug: Boolean, manualEnableUpload: Boolean) {
    val initBeginTime = SystemClock.elapsedRealtime()
    Log.d("initSDK begin", initBeginTime.toString())
    DTAdReport.generateUUID()
    DTAnalytics.getDataTowerId(object : OnDataTowerIdListener {
        override fun onDataTowerIdCompleted(dataTowerId: String) {
            Log.d("BEFORE, DataTowerId", dataTowerId)
        }
    })
    DT.initSDK(context, appId, serverUrl, DTChannel.GP, isDebug, Log.VERBOSE, manualEnableUpload)
    Log.d("initSDK end", (SystemClock.elapsedRealtime() - initBeginTime).toString())
    DTAnalytics.getDataTowerId(object : OnDataTowerIdListener {
        override fun onDataTowerIdCompleted(dataTowerId: String) {
            Log.d("DataTowerId", dataTowerId)
        }
    })

    DTAnalyticsUtils.trackTimerStart("initApp")
    //mock data
    if (SharedPreferencesUtils.getParam(context, "first_open", true) as Boolean) {

        SharedPreferencesUtils.setParam(context, "acid", "acid-" + DTAdReport.generateUUID())
        SharedPreferencesUtils.setParam(context, "fiid", "fiid-" + DTAdReport.generateUUID())
        SharedPreferencesUtils.setParam(
            context,
            "fcm_token",
            "fcm_token" + DTAdReport.generateUUID()
        )
        SharedPreferencesUtils.setParam(context, "afid", "afid-" + DTAdReport.generateUUID())
        SharedPreferencesUtils.setParam(context, "asid", "asid-" + DTAdReport.generateUUID())
        SharedPreferencesUtils.setParam(context, "koid", "koid-" + DTAdReport.generateUUID())
        SharedPreferencesUtils.setParam(
            context,
            "adjustId",
            "adjustId-" + DTAdReport.generateUUID()
        )
        SharedPreferencesUtils.setParam(context, "first_open", false)
    }
    DTAnalyticsUtils.trackTimerEnd("initApp")
}
