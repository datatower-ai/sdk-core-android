package com.roiquery.analytics_demo.ui.fn.core

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.get
import com.roiquery.analytics.DTAnalytics
import com.roiquery.analytics.OnDataTowerIdListener
import com.roiquery.analytics_demo.DisplayAllApiActivity
import com.roiquery.analytics_demo.R
import com.roiquery.quality.PerfLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DtSdkCoreFnFragment : PreferenceFragmentCompat(), CoroutineScope {
    override val coroutineContext: CoroutineContext get() = lifecycleScope.coroutineContext

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.dt_sdk_core_fn_as_prefs, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceScreen.get<Preference>("dt_anal_get_dtid")
            ?.setOnPreferenceClickListener(this::guiDTIDUpdate)

        preferenceScreen.get<PreferenceCategory>("dt_anal_user_builtin_prop_group")?.let {
            for (i in 0 until it.preferenceCount) {
                val pref = it[i]
                Log.d("DTDEMO", "key=${pref.key}")
                if (pref.key?.startsWith("dt_anal_user_builtin_prop_") == true) {
                    pref.setOnPreferenceChangeListener(this::onBuiltinPropPrefChanged)
                }
            }
        }

        preferenceScreen.get<Preference>("dt_anal_get_db_count")?.let {
            it.setOnPreferenceClickListener {
                guiDBItemUpdate(it)
            }
        }

        assignBuiltinUserPropertiesToSdk()
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when (preference.key) {
            "dt_anal_track_predefined" -> trackEventPredefined()
            "dt_anal_track_custom" -> trackEventCustomDialogShow()
            "dt_anal_invoke_user_api" -> invokeUserApiDialogShow()
            "dt_anal_invoke_all_api" -> invokeAllApiDialogShow()
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun assignBuiltinUserPropertiesToSdk() {
        val sharedPrefs = this.preferenceManager?.sharedPreferences ?: return
        val keys = sharedPrefs.all.keys.filter { it.startsWith("dt_anal_user_builtin_prop_") }
        for (key in keys) {
            val endsWith = key.substringAfter("dt_anal_user_builtin_prop_")
            val value = sharedPrefs.getString(key, null) ?: continue
            when (endsWith) {
                "acid" -> DTAnalytics.setAccountId(value)
                "firebase_id" -> DTAnalytics.setFirebaseAppInstanceId(value)
                "appsflyer_id" -> DTAnalytics.setAppsFlyerId(value)
                "kochava_id" -> DTAnalytics.setKochavaId(value)
                "adjust_id" -> DTAnalytics.setAdjustId(value)
            }
        }
    }

    private fun trackEventPredefined() {
        Result.runCatching {
            DTAnalytics.track(
                "dt_track_simple",
                mapOf(Pair("property_object", generatePredefinedEventProperties))
            )
        }.onFailure { it.printStackTrace() }
    }

    private fun guiDTIDUpdate(ignored: Preference): Boolean {
        launch {
            val pref = preferenceScreen.get<Preference>("dt_anal_get_dtid") ?: return@launch
            pref.summary = "DTID=<Loading..>"

            val dtid = async(Dispatchers.IO) {
                suspendCoroutine<String> {
                    DTAnalytics.getDataTowerId(object : OnDataTowerIdListener {
                        override fun onDataTowerIdCompleted(dataTowerId: String) {
                            it.resume(dataTowerId)
                        }
                    })
                }
            }.await()
            pref.summary = "DTID=$dtid"

            val clipboardMgr =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    ?: return@launch
            clipboardMgr.setPrimaryClip(ClipData.newPlainText("DTID", dtid))
            Toast.makeText(requireActivity(), "DTID copied to clipboard.", Toast.LENGTH_SHORT)
                .show()
        }
        return true
    }

    private fun guiDBItemUpdate(ignored: Preference): Boolean {
        launch {
            val pref = preferenceScreen.get<Preference>("dt_anal_get_db_count") ?: return@launch
            pref.summary = "DB Item=<Loading..>"

            val dbItmeCount = PerfLogger.getDBItemCount()

            pref.summary = "DB Item=$dbItmeCount"
        }
        return true
    }

    private fun trackEventCustomDialogShow() {
        TrackEventCustomizedActivity.startActivity(requireActivity())
    }

    private  fun invokeUserApiDialogShow() {
        UserApiActivity.startActivity(requireActivity())
    }

    private fun invokeAllApiDialogShow() {
        DisplayAllApiActivity.startActivity(requireActivity())
    }

    private fun onBuiltinPropPrefChanged(preference: Preference, newValue: Any?): Boolean {
        if (preference !is EditTextPreference) return false
        val key = preference.key?.substringAfter("dt_anal_user_builtin_prop_") ?: return false
        val value = newValue as? CharSequence ?: return false

        when (key) {
            "acid" -> DTAnalytics.setAccountId(value.toString())
            "firebase_id" -> DTAnalytics.setFirebaseAppInstanceId(value.toString())
            "appsflyer_id" -> DTAnalytics.setAppsFlyerId(value.toString())
            "kochava_id" -> DTAnalytics.setKochavaId(value.toString())
            "adjust_id" -> DTAnalytics.setAdjustId(value.toString())
        }
        return true
    }

    private val generatePredefinedEventProperties: Map<String, Any?>
        get() = mapOf(
            Pair("hero_name", "刘备"),
            Pair("hero_level", 22),
            Pair("hero_if_support", false),
            Pair(
                "hero_equipment", listOf(
                    "雌雄双股剑", "的卢",
                )
            ),
            Pair(
                "hero_sub_obj", mapOf(
                    Pair("hero_name", "刘备"),
                    Pair("hero_level", 22),
                    Pair("hero_if_support", false),
                    Pair(
                        "hero_equipment", listOf(
                            "雌雄双股剑", "的卢",
                        )
                    ),
                )
            ),
        )
}
