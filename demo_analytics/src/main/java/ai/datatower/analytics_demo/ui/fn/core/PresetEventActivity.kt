@file:Suppress("OPT_IN_IS_NOT_ENABLED")

package ai.datatower.analytics_demo.ui.fn.core

import ai.datatower.analytics.DT
import ai.datatower.analytics.utils.PresetEvent
import ai.datatower.analytics_demo.ui.theme.DataTowerSDKCoreTheme
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class PresetEventActivity: ComponentActivity() {
    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, PresetEventActivity::class.java))
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
                    PresetEventPage()
                }
            }
        }
    }
}

var presetEventEnabled = PresetEvent.values().map { true }.toTypedArray()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetEventPage() {
    val context = LocalContext.current

    val peEnabled = remember {
        mutableStateListOf(*presetEventEnabled)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Preset Event") },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.Rounded.ArrowBack, "")
                    }
                }
            )
        }
    ) { contentPaddings ->
        Column(
            modifier = Modifier.padding(contentPaddings)
        ) {
            PresetEvent.values().forEachIndexed { idx, pe ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(pe.name, modifier = Modifier.weight(1f))
                    Switch(
                        checked = peEnabled[idx],
                        onCheckedChange = {
                            if (peEnabled[idx]) {
                                peEnabled[idx] = false
                                DT.disableAutoTrack(pe)
                            } else {
                                peEnabled[idx] = true
                                DT.enableAutoTrack(pe)
                            }
                        }
                    )
                }
            }
        }
    }
}