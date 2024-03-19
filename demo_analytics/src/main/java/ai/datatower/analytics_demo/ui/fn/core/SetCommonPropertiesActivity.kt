package ai.datatower.analytics_demo.ui.fn.core

import ai.datatower.analytics.DTAnalytics
import ai.datatower.analytics.data.EventDataAdapter
import ai.datatower.analytics.utils.CommonPropsUtil
import ai.datatower.analytics_demo.ui.theme.DataTowerSDKCoreTheme
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import kotlin.math.exp

class SetCommonPropertiesActivity: ComponentActivity() {
    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, SetCommonPropertiesActivity::class.java))
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
                    SetCommonPropertiesPage()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetCommonPropertiesPage() {
    val context = LocalContext.current;

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Common Properties") },
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.Rounded.ArrowBack, "")
                    }
                }
            )
        },
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 15.dp)
                .scrollable(rememberScrollState(), Orientation.Vertical)
        ) {
            SetDynamicCommonPropertiesSect()
            Spacer(Modifier.height(15.dp))
            SetStaticCommonPropertiesSect()
        }
    }

}

@Composable
fun SetDynamicCommonPropertiesSect(modifier:Modifier = Modifier) {
    SetCommonPropertiesSectBase(
        "Dynamic",
        CommonPropsUtil.dumpDynamicProperties(),
        onSet = { DTAnalytics.setCommonProperties(JSONObject(it)) },
        onClear = { DTAnalytics.clearCommonProperties() },
        modifier = modifier,
    )
}

@Composable
fun SetStaticCommonPropertiesSect(modifier: Modifier = Modifier) {
    SetCommonPropertiesSectBase(
        "Static",
        CommonPropsUtil.dumpStaticProperties(),
        onSet = { DTAnalytics.setStaticCommonProperties(JSONObject(it)) },
        onClear = { DTAnalytics.clearStaticCommonProperties() },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetCommonPropertiesSectBase(
    title: String,
    initProps: String,
    onSet: (String) -> Unit,
    onClear: () -> Unit,
    modifier:Modifier = Modifier
) {
    var props by remember { mutableStateOf(initProps) }
    var expanded by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    Card(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .clickable {
                        expanded = !expanded
                    }
                    .padding(vertical = 24.dp, horizontal = 24.dp)
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                )
                if (expanded) {
                    Icon(Icons.Rounded.KeyboardArrowDown, "")
                } else {
                    Icon(Icons.Rounded.KeyboardArrowRight, "")
                }
            }

            if (expanded) {
                OutlinedTextField(
                    value = props,
                    onValueChange = {
                        props = it
                        hasError = false
                    },
                    placeholder = { Text("{}") },
                    isError = hasError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            try {
                                props = "{}"
                                onClear()
                            } catch (t: Throwable) {
                                hasError = true
                            }
                        }
                    ) {
                        Text("Clear")
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    ElevatedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            try {
                                onSet(props)
                                props = JSONObject(props).toString(4)
                            } catch (t: Throwable) {
                                hasError = true
                            }
                        }
                    ) {
                        Text("Set")
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}