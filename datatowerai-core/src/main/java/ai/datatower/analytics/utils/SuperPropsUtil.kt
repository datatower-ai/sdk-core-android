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
        EventDataAdapter.getInstance()?.getStaticSuperProperties()?.await()?.let {
            staticProperties = it
        }
    }

    fun updateDynamicProperties(properties: JSONObject) {
        MainQueue.get().postTask {
            dynamicProperties = JSONObject()
            for (key in properties.keys()) {
                dynamicProperties.put(key, properties.get(key))
            }
        }
    }

    fun clearDynamicProperties() {
        MainQueue.get().postTask {
            dynamicProperties = JSONObject()
        }
    }

    fun updateStaticProperties(properties: JSONObject) {
        MainQueue.get().postTask {
            staticProperties = JSONObject()
            for (key in properties.keys()) {
                staticProperties.put(key, properties.get(key))
            }
            EventDataAdapter.getInstance()?.setStaticSuperProperties(staticProperties)
        }
    }

    fun clearStaticProperties() {
        MainQueue.get().postTask {
            staticProperties = JSONObject()
            EventDataAdapter.getInstance()?.setStaticSuperProperties(staticProperties)
        }
    }
}