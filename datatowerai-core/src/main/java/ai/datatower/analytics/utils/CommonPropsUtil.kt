package ai.datatower.analytics.utils

import ai.datatower.analytics.data.DataParams
import ai.datatower.analytics.data.EventDataAdapter
import ai.datatower.analytics.taskqueue.MainQueue
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import org.json.JSONObject

object CommonPropsUtil {
    private var dynamicPropertiesGetter: (() -> JSONObject)? = null
    private var staticProperties: JSONObject = JSONObject()
    private var internalProperties: JSONObject = JSONObject()

    internal suspend fun init() {
        staticProperties = restoreCommonProperties(DataParams.CONFIG_STATIC_SUPER_PROPERTY, staticProperties)
        internalProperties = restoreCommonProperties(DataParams.CONFIG_INTERNAL_SUPER_PROPERTY, internalProperties)
    }

    private suspend fun restoreCommonProperties(@CommonPropertiesKey key: String, original: JSONObject): JSONObject {
        return EventDataAdapter.getInstance()?.restoreCommonProperties(key)?.await()?.apply {
            original.keys().forEach { k ->
                put(k, original.get(k))
            }
        } ?: original
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
            EventDataAdapter.getInstance()?.saveCommonProperties(DataParams.CONFIG_STATIC_SUPER_PROPERTY, staticProperties)
        }
    }

    internal fun clearStaticProperties() {
        MainQueue.get().postTask {
            staticProperties = JSONObject()
            EventDataAdapter.getInstance()?.saveCommonProperties(DataParams.CONFIG_STATIC_SUPER_PROPERTY, staticProperties)
        }
    }

    fun dumpDynamicProperties(): String {
        return dynamicPropertiesGetter?.invoke()?.toString(4) ?: "null"
    }

    fun dumpStaticProperties(): String {
        return staticProperties.toString(4)
    }

    internal fun applyCommonPropertiesToEvent(json: JSONObject) {
        val dynamicProperties = try {
            dynamicPropertiesGetter?.invoke() ?: JSONObject()
        } catch (t: Throwable) {
            JSONObject()
        }

        // Priority: dynamic > static > internal
        val allProperties = listOf(dynamicProperties, staticProperties, internalProperties)
        for (props in allProperties) {
            for (key in props.keys()) {
                if (json.has(key)) continue
                json.put(key, props[key])
            }
        }
    }

    @WorkerThread
    internal fun updateInternalCommonProperties(key: String, value: Any?) {
        val old = internalProperties.get(key)
        internalProperties.put(key, value)

        if (old != value) {
            // only if the value changes
            EventDataAdapter.getInstance()?.saveCommonProperties(
                DataParams.CONFIG_INTERNAL_SUPER_PROPERTY,
                internalProperties
            )
        }
    }
}