package ai.datatower.analytics.utils

import ai.datatower.analytics.data.EventDataAdapter
import ai.datatower.analytics.taskqueue.MainQueue
import org.json.JSONObject

object CommonPropsUtil {
    private var dynamicPropertiesGetter: (() -> JSONObject)? = null
    private var staticProperties: JSONObject = JSONObject()

    internal suspend fun init() {
        EventDataAdapter.getInstance()?.getStaticSuperProperties()?.await()?.let {
            staticProperties = it
        }
    }

    internal fun updateDynamicProperties(propertiesGetter: () -> JSONObject) {
        MainQueue.get().postTask {
            dynamicPropertiesGetter = propertiesGetter
        }
    }

    internal fun clearDynamicProperties() {
        MainQueue.get().postTask {
            dynamicPropertiesGetter = null
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
        return dynamicPropertiesGetter?.invoke()?.toString(4) ?: "null"
    }

    fun dumpStaticProperties(): String {
        return staticProperties.toString(4)
    }

    internal fun insertCommonProperties(json: JSONObject) {
        val dynamicProperties = try {
            dynamicPropertiesGetter?.invoke() ?: JSONObject()
        } catch (t: Throwable) {
            JSONObject()
        }
        // Priority: dynamic > static
        for (key in dynamicProperties.keys()) {
            if (json.has(key)) continue
            json.put(key, dynamicProperties[key])
        }
        for (key in staticProperties.keys()) {
            if (json.has(key)) continue
            json.put(key, staticProperties[key])
        }
    }
}