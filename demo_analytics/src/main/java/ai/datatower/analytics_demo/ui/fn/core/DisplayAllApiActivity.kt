package ai.datatower.analytics_demo.ui.fn.core

import ai.datatower.ad.AdMediation
import ai.datatower.ad.AdPlatform
import ai.datatower.ad.AdType
import ai.datatower.ad.DTAdReport
import ai.datatower.analytics.DT
import ai.datatower.analytics.DTAnalytics
import ai.datatower.analytics.DTAnalyticsUtils
import ai.datatower.analytics.utils.toMap
import ai.datatower.analytics_demo.ui.theme.DataTowerSDKCoreTheme
import ai.datatower.iap.DTIAPReport
import ai.datatower.ias.DTIASReport
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.json.JSONObject
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance

class DisplayAllApiActivity : ComponentActivity() {
    companion object {
        val apiClasses = listOf(
            Pair("ai.datatower.analytics.DT", "DT"),
            Pair("ai.datatower.analytics.DTAnalytics", "DT Analytics"),
            Pair("ai.datatower.analytics.DTAnalyticsUtils", "DT Analytics Utils"),
            Pair("ai.datatower.ad.DTAdReport", "Ad"),
            Pair("ai.datatower.iap.DTIAPReport", "IAP"),
            Pair("ai.datatower.ias.DTIASReport", "IAS")
        )
        val apiClasses2 = listOf(
            Pair(DT::class, "DT"),
            Pair(DTAnalytics::class, "DT Analytics"),
            Pair(DTAnalyticsUtils::class, "DT Analytics Utils"),
            Pair(DTAdReport::class, "Ad"),
            Pair(DTIAPReport::class, "IAP"),
            Pair(DTIASReport::class, "IAS")
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

@Composable
private fun ApiList() {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val dialogState: MutableState<Pair<KFunction<Any?>, KClass<*>>?> = remember {
        mutableStateOf(null)
    }
    Box(Modifier.fillMaxSize()) {
        ApiDialog(dialogState)

        Column(Modifier.verticalScroll(rememberScrollState())) {
            Button(onClick = {
                Handler(Looper.getMainLooper()).post {
                    Log.w("TESTTT", "====================")
                    for (i in 1..1000000) Log.i("T", "$i")
                }
            }) {
                Text("Loop 1000000")
            }
            Button(onClick = {
                Handler(Looper.getMainLooper()).post {
                    Log.w("TESTTT", "====================")
                    for (i in 1..10000) Log.i("T", "$i")
                }
            }) {
                Text("Loop 10000")
            }

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

            DisplayAllApiActivity.apiClasses2.forEach {
                SubApiList2(dialogState = dialogState, clazz = it.first, title = it.second)
                Divider()
            }
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


private val excludeMethods = listOf("equals", "hashCode", "toString",)

@Composable
private fun SubApiList2(dialogState: MutableState<Pair<KFunction<Any?>, KClass<*>>?>, clazz: KClass<*>, title: String) {
    Column(Modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 16.dp)
        )

        clazz.companionObject?.members?.forEach {
            if (it !is KFunction<*>) return@forEach
            if (it.visibility != KVisibility.PUBLIC) return@forEach
            val name = it.name
            if (excludeMethods.contains(name)) return@forEach

            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        dialogState.value = Pair(it, clazz)
                    }
                    .padding(vertical = 10.dp, horizontal = 24.dp)
            )
        } ?: Text("NULL")
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun ApiDialog(dialogState: MutableState<Pair<KFunction<Any?>, KClass<*>>?>) {
    var hasPreError by mutableStateOf(false)

    dialogState.value?.first?.let {
        val arguments: SnapshotStateMap<KParameter, Any?> = remember { mutableStateMapOf() }

        Dialog(
            onDismissRequest = { dialogState.value = null },
            properties = DialogProperties()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxHeight(0.95f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(it.name, style = MaterialTheme.typography.titleLarge)

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (it.parameters.size > 1) {
                            it.parameters.subList(1, it.parameters.size).forEach { p ->
                                ParamComposable(p, arguments) { hasPreError = true }
                            }
                        } else {
                            Text("No argument needed")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        TextButton(onClick = { dialogState.value = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        ElevatedButton(
                            enabled = !hasPreError,
                            onClick = {
                                Log.w("DisplayAllApi", "callBy: ${arguments.toMap()}")
                                it.callBy(arguments.apply {
                                    put(it.parameters[0], dialogState.value!!.second.companionObjectInstance)
                                })

                                dialogState.value = null
                            }
                        ) {
                            Text("Call")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParamComposable(param: KParameter, arguments: SnapshotStateMap<KParameter, Any?>, whenError: () -> Unit) {
    var isNull by remember { mutableStateOf(false) }
    val value = arguments[param]
    var strJsonValue by remember { mutableStateOf(value?.toString() ?: "") }

    LaunchedEffect(Unit) {
        val dv = setDefaultValue(param)
        if (dv is Optional.Some) {
            arguments[param] = dv.value
        }
    }

    Column(Modifier.padding(vertical = 10.dp)) {
        val type = param.type
        param.name?.let {
            Text("$it ($type)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        } ?: Text("UNKNOWN",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.error
        ).also {
            whenError()
        }

        Spacer(modifier = Modifier.height(5.dp))

        if (type.isMarkedNullable) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isNull,
                    onCheckedChange = {
                        isNull = it
                        if (it) {
                            arguments.remove(param)
                        } else {
                            val dv = setDefaultValue(param)
                            if (dv is Optional.Some) {
                                arguments[param] = dv.value
                            }
                        }
                    }
                )
                Spacer(Modifier.width(8.dp))
                Text("null")
            }
            Spacer(modifier = Modifier.height(5.dp))
        }

        if (!isNull) {
            when (type.classifier) {
                String::class -> {
                    OutlinedTextField(
                        value = value?.toString() ?: "<NULL>",
                        onValueChange = { arguments[param] = it }
                    )
                }
                Int::class -> {
                    OutlinedTextField(
                        value = value?.toString() ?: "",
                        onValueChange = { arguments[param] = it.toIntOrNull() }
                    )
                }
                Long::class -> {
                    OutlinedTextField(
                        value = value?.toString() ?: "",
                        onValueChange = { arguments[param] = it.toLongOrNull() }
                    )
                }
                Double::class -> {
                    OutlinedTextField(
                        value = value?.toString() ?: "",
                        onValueChange = { arguments[param] = it.toDoubleOrNull() }
                    )
                }
                AdType::class -> {
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        AdType.values().forEach {
                            FilterChip(
                                selected = value == it,
                                onClick = { arguments[param] = it },
                                label = { Text(it.name) },
                                modifier = Modifier.padding(horizontal = 4.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
                AdMediation::class -> {
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        AdMediation.values().forEach {
                            FilterChip(
                                selected = value == it,
                                onClick = { arguments[param] = it },
                                label = { Text(it.name) },
                                modifier = Modifier.padding(horizontal = 4.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
                AdPlatform::class -> {
                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        AdPlatform.values().forEach {
                            FilterChip(
                                selected = value == it,
                                onClick = { arguments[param] = it },
                                label = { Text(it.name) },
                                modifier = Modifier.padding(horizontal = 4.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
                Boolean::class -> {
                    Switch(checked = value == true, onCheckedChange = { arguments[param] = it })
                }
                Map::class -> {
                    OutlinedTextField(
                        value = strJsonValue,
                        onValueChange = { strJsonValue = it },
                        trailingIcon = {
                            IconButton(onClick = {
                                try {
                                    arguments[param] = JSONObject(strJsonValue).toMap()
                                } catch (t: Throwable) {}
                            }) {
                                Icon(Icons.Rounded.Done, null)
                            }
                        }
                    )
                }
                JSONObject::class -> {
                    OutlinedTextField(
                        value = strJsonValue,
                        onValueChange = { strJsonValue = it },
                        trailingIcon = {
                            IconButton(onClick = {
                                try {
                                    arguments[param] = JSONObject(strJsonValue)
                                } catch (t: Throwable) {}
                            }) {
                                Icon(Icons.Rounded.Done, null)
                            }
                        }
                    )
                }
                Array<String>::class -> {
                    OutlinedTextField(
                        value = strJsonValue,
                        onValueChange = { strJsonValue = it },
                        trailingIcon = {
                            IconButton(onClick = {
                                if (arguments[param] == null) {
                                    arguments[param] = arrayOf<String>()
                                } else {
                                    arguments[param] = (arguments[param] as Array<String>)
                                        .toMutableList()
                                        .apply {
                                            add(strJsonValue)
                                        }.toTypedArray<String>()
                                }
                            }) {
                                Icon(Icons.Rounded.Done, null)
                            }
                        }
                    )

                    Row(Modifier.horizontalScroll(rememberScrollState())) {
                        val v = try { arguments[param] as Array<String> } catch (t: Throwable) { arrayOf<String>() }

                        v.forEach {
                            FilterChip(
                                selected = false,
                                onClick = { arguments[param] = v.filter { f -> f != it }.toTypedArray() },
                                label = { Text(it) },
                                modifier = Modifier.padding(horizontal = 4.dp),
                            )
                        }

                    }
                }
                else -> {
                    whenError()
                    Text(
                        "Unimplemented Type: ${type.classifier}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        Text(arguments[param].toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

fun setDefaultValue(param: KParameter): Optional {
    return when (param.type.classifier) {
        String::class -> Optional.Some("")
        AdType::class -> Optional.Some(AdType.IDLE)
        AdMediation::class -> Optional.Some(AdMediation.IDLE)
        AdPlatform::class -> Optional.Some(AdPlatform.IDLE)
        Int::class -> Optional.Some(0)
        Long::class -> Optional.Some(0L)
        Double::class -> Optional.Some(0.0)
        Boolean::class -> Optional.Some(true)
        Map::class -> Optional.Some(mutableMapOf<String, Any?>())
        JSONObject::class -> Optional.Some(JSONObject())
        Array<String>::class -> Optional.Some(arrayOf<String>())
        else -> {
            if (param.type.isMarkedNullable) {
                Optional.Some(null)
            } else {
                Optional.None
            }
        }
    }
}

sealed interface Optional {
    class Some(val value: Any?): Optional
    object None: Optional
}