package ai.datatower.analytics.utils

import ai.datatower.analytics.data.EventDataAdapter
import ai.datatower.analytics.taskqueue.MainQueue
import android.util.Log
import org.json.JSONObject

object SuperPropsUtil {
    var dynamicProperties: JSONObject = JSONObject()
        private set
    var staticProperties: JSONObject = JSONObject()
        private set

    suspend fun init() {
        Log.w("xxx", "SuperPropsUtil init")
        EventDataAdapter.getInstance()?.getStaticSuperProperties()?.await()?.let {
            Log.w("xxx", "SuperPropsUtil init, JSONObject: $it")
            staticProperties = it
        }
    }

    fun updateDynamicProperties(properties: JSONObject) {
        clearDynamicProperties()
        for (key in properties.keys()) {
            dynamicProperties.put(key, properties.get(key))
        }
    }

    fun clearDynamicProperties() {
        dynamicProperties = JSONObject()
    }

    fun updateStaticProperties(properties: JSONObject) {
        clearStaticProperties()
        for (key in properties.keys()) {
            staticProperties.put(key, properties.get(key))
        }
        MainQueue.get().postTask {
            EventDataAdapter.getInstance()?.setStaticSuperProperties(staticProperties)
        }
    }

    fun clearStaticProperties() {
        staticProperties = JSONObject()
        EventDataAdapter.getInstance()?.setStaticSuperProperties(staticProperties)
    }
}