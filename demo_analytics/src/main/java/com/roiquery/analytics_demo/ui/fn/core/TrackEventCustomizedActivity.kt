package com.roiquery.analytics_demo.ui.fn.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Toast
import androidx.annotation.ArrayRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.roiquery.analytics.DTAnalytics
import com.roiquery.analytics_demo.R
import com.roiquery.analytics_demo.databinding.EventTrackingFinalPaneListItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap
import kotlin.coroutines.CoroutineContext

interface EventMarker
data class OnEventNameInputEvent(val text: String) : EventMarker
data class OnEventPropertiesInputEvent(val text: String) : EventMarker
data class OnEventTrackingSubmitEvent(val repeats: UInt, val interval: UInt) : EventMarker

class TrackEventCustomizedActivity : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext get() = lifecycleScope.coroutineContext

    private val recyclerView by lazy {
        val view = RecyclerView(this)
        view.layoutParams = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            RecyclerView.LayoutParams.MATCH_PARENT
        )
        view.layoutManager = LinearLayoutManager(this)
        view.clipChildren = false
        view.clipToPadding = false
        view
    }
    private val recyclerViewAdapter by lazy { RecyclerViewAdapter() }

    private var eventTrackingName = ""
    private var eventTrackingProperties = ""
    private var eventTrackingRepeats = 1u
    private var eventTrackingInterval = 500u
    private var eventTrackingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(recyclerView)
        recyclerView.adapter = recyclerViewAdapter
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventBusMessage(event: EventMarker) {
        when (event) {
            is OnEventNameInputEvent -> {
                eventTrackingName = event.text
            }

            is OnEventPropertiesInputEvent -> {
                eventTrackingProperties = event.text
            }

            is OnEventTrackingSubmitEvent -> {
                eventTrackingRepeats = event.repeats
                eventTrackingInterval = event.interval
                launchEventTrackingIfNotRunning()
            }
        }
    }

    private fun launchEventTrackingIfNotRunning() {
        val job = eventTrackingJob
        if (job == null || job.isCompleted) {
            eventTrackingJob = launchEventTracking(
                eventTrackingName,
                eventTrackingProperties,
                eventTrackingRepeats,
                eventTrackingInterval
            )
        }
    }

    private fun launchEventTracking(
        name: String,
        properties: String,
        repeats: UInt, interval: UInt,
    ) = launch {
        var mapping: JSONObject? = null
        try {
            mapping = JSONObject(properties)
        } catch (e: JSONException) {

        }

        if (mapping != null) {
            for (nthTime in 0u until repeats) {
                mapping.put("seq",nthTime.toString())
                DTAnalytics.track(name, mapping)
                delay(interval.toLong())
            }
        } else {
            Toast.makeText(applicationContext,"非法的json字符串!", Toast.LENGTH_SHORT).show();
        }
    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, TrackEventCustomizedActivity::class.java))
        }
    }
}

class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.event_tracking_name_input_list_item ->
                EventNameInputViewHolder.create(parent)

            R.layout.event_tracking_properties_input_list_item ->
                EventPropertyInputViewHolder.create(parent)

            R.layout.event_tracking_final_pane_list_item ->
                EventFinalPaneViewHolder.create(parent)

            else -> throw IllegalStateException("unreachable")
        }

    override fun getItemViewType(position: Int): Int =
        when (position) {
            0 -> R.layout.event_tracking_name_input_list_item
            1 -> R.layout.event_tracking_properties_input_list_item
            2 -> R.layout.event_tracking_final_pane_list_item
            else -> throw IllegalStateException("unreachable")
        }

    override fun getItemCount(): Int = 3

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EventNameInputViewHolder -> holder.bind()
            is EventPropertyInputViewHolder -> holder.bind()
            is EventFinalPaneViewHolder -> holder.bind()
        }
    }

    companion object {
        class EventNameInputViewHolder private constructor(
            val rootView: TextInputLayout
        ) : RecyclerView.ViewHolder(rootView) {
            private val editText: AppCompatAutoCompleteTextView get() = rootView.findViewById(R.id.edit_text)!!

            fun bind() {
                val adapter = TextFnContainsFilteredTextArrayAdapter(
                    rootView.context,
                    R.array.dtsdk_builtin_event_names,
                )
                editText.setAdapter(adapter)
                editText.doOnTextChanged(this::editText_onTextChanged)
                editText.setText("eventName")
            }

            private fun editText_onTextChanged(
                text: CharSequence?, start: Int, before: Int, count: Int
            ) {
                EventBus.getDefault().post(OnEventNameInputEvent(text?.toString() ?: ""))
            }

            companion object {
                fun create(parent: ViewGroup): EventNameInputViewHolder {
                    val inflater = LayoutInflater.from(parent.context)
                    val view = inflater
                        .inflate(R.layout.event_tracking_name_input_list_item, parent, false)
                    return EventNameInputViewHolder(view as TextInputLayout)
                }
            }
        }

        class EventPropertyInputViewHolder(
            private val rootView: View
        ) : RecyclerView.ViewHolder(rootView) {
            private val editText: TextInputEditText get() = rootView.findViewById(R.id.edit_text)!!

            fun bind() {
                editText.doOnTextChanged(this::editText_onTextChanged)

                editText.setText("{\n" +
                        "\"action\": \"test\",\n" +
                        "\"id\": \"1234556\"\n" +
                        "}")
            }

            private fun editText_onTextChanged(
                text: CharSequence?, start: Int, before: Int, count: Int
            ) {
                EventBus.getDefault().post(OnEventPropertiesInputEvent(text?.toString() ?: ""))
            }

            companion object {
                fun create(parent: ViewGroup): EventPropertyInputViewHolder {
                    val inflater = LayoutInflater.from(parent.context)
                    val view = inflater
                        .inflate(R.layout.event_tracking_properties_input_list_item, parent, false)
                    return EventPropertyInputViewHolder(view)
                }
            }
        }

        class EventFinalPaneViewHolder(
            private val rootView: View
        ) : RecyclerView.ViewHolder(rootView) {
            private var binding: EventTrackingFinalPaneListItemBinding? = null

            fun bind() {
                binding = EventTrackingFinalPaneListItemBinding.bind(rootView)
                binding?.runButton?.setOnClickListener(this::runButton_onClicked)
            }

            private fun runButton_onClicked(view: View) {
                val repeats = binding?.repeatsEditText?.text?.toString()?.toUIntOrNull() ?: 1u
                val interval = binding?.intervalEditText?.text?.toString()?.toUIntOrNull() ?: 500u
                EventBus.getDefault().post(OnEventTrackingSubmitEvent(repeats, interval))
            }

            companion object {
                fun create(parent: ViewGroup): EventFinalPaneViewHolder {
                    val inflater = LayoutInflater.from(parent.context)
                    val view = inflater
                        .inflate(R.layout.event_tracking_final_pane_list_item, parent, false)
                    return EventFinalPaneViewHolder(view)
                }
            }
        }
    }
}

internal class TextFnContainsFilteredTextArrayAdapter(
    context: Context,
    @ArrayRes textArrayResId: Int,
) : ArrayAdapter<CharSequence>(
    context,
    android.R.layout.simple_dropdown_item_1line,
) {
    private val filtered = arrayListOf<CharSequence>().also {
        it.addAll(context.resources.getStringArray(textArrayResId))
    }

    override fun getFilter(): Filter = filter

    override fun getCount(): Int = filtered.size

    override fun getItem(position: Int): CharSequence? = filtered.getOrNull(position)

    private val filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val adapter = this@TextFnContainsFilteredTextArrayAdapter
            val results = FilterResults()
            var textArray = adapter.context.resources.getStringArray(textArrayResId).toList()
            if (constraint == null) {
                results.count = textArray.size
                results.values = textArray
                return results
            }

            val pattern = constraint.split("_", " ")
                .joinToString(prefix = "(", separator = ")|(", postfix = ")")
            val regex = Regex(pattern)
            textArray = textArray.filter { it.contains(regex) }
                .sortedByDescending { regex.findAll(it).toList().size }
            results.count = textArray.size
            results.values = textArray
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            val adapter = this@TextFnContainsFilteredTextArrayAdapter
            val filtered = (results?.values as? List<*>)?.filterIsInstance<CharSequence>() ?: return
            adapter.filtered.clear()
            adapter.filtered.addAll(filtered)
            adapter.notifyDataSetChanged()
        }
    }
}
