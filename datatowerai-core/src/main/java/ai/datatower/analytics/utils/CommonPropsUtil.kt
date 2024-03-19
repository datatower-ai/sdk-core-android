package ai.datatower.analytics.utils

import ai.datatower.analytics.data.EventDataAdapter
import ai.datatower.analytics.taskqueue.MainQueue
import org.json.JSONObject

object CommonPropsUtil {
    internal  var dynamicProperties: JSONObject = JSONObject()
        private set
    internal var staticProperties: JSONObject = JSONObject()
        private set

    internal suspend fun init() {
        EventDataAdapter.getInstance()?.getStaticSuperProperties()?.await()?.let {
            staticProperties = it
        }
    }

    internal fun updateDynamicProperties(properties: JSONObject) {
        MainQueue.get().postTask {
            dynamicProperties = JSONObject()
            for (key in properties.keys()) {
                dynamicProperties.put(key, properties.get(key))
            }
        }
    }

    internal fun clearDynamicProperties() {
        MainQueue.get().postTask {
            dynamicProperties = JSONObject()
        }
    }

    internal fun updateStaticProperties(properties: JSONObject) {
        MainQueue.get().postTask {
            staticProperties = JSONObject()
            for (key in properties.keys()) {
                staticProperties.put(key, properties.get(key))
            }
            EventDataAdapter.getInstance()?.setStaticSuperProperties(staticProperties)
        }
    }

    internal fun clearStaticProperties() {
        MainQueue.get().postTask {
            staticProperties = JSONObject()
            EventDataAdapter.getInstance()?.setStaticSuperProperties(staticProperties)
        }
    }

    fun dumpDynamicProperties(): String {
        return dynamicProperties.toString(4)
    }

    fun dumpStaticProperties(): String {
        return staticProperties.toString(4)
    }
}