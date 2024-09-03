package ai.datatower.analytics.utils

import ai.datatower.analytics.Constant
import android.content.Context
import android.content.res.Resources.NotFoundException
import org.json.JSONObject

internal class PresetPropManager {
    companion object {
        @Volatile private var instance: PresetPropManager? = null
        fun get(context: Context): PresetPropManager {
            return instance ?: PresetPropManager().apply {
                initDisableList(context)
                instance = this
            }
        }

        fun get(): PresetPropManager? = instance

        private val disableList = HashSet<String>()
        @Synchronized
        private fun initDisableList(context: Context) {
            try {
                val resources = context.resources
                val array = resources.getStringArray(
                    // Query from application's resources, so we cannot simply use R.array.xxx
                    resources.getIdentifier(
                        "DTDisPresetProperties",
                        "array",
                        context.packageName
                    )
                )
                disableList.addAll(array)

                // 不可被关闭的属性
                val ensureNotInside = listOf(
                    Constant.EVENT_INFO_BUNDLE_ID,
                    Constant.EVENT_INFO_APP_ID,
                    Constant.EVENT_INFO_DEBUG,
                    Constant.EVENT_INFO_DT_ID,
                    Constant.EVENT_INFO_ACID,
                    Constant.COMMON_PROPERTY_EVENT_SESSION,
                )
                ensureNotInside.forEach {
                    disableList.remove(it)
                }
            } catch (e: NotFoundException) {
                // pass, config file is not defined.
            } catch (e: NoClassDefFoundError) {
                LogUtils.e(Constant.LOG_TAG, e.toString())
            } catch (e: Exception) {
                LogUtils.e(Constant.LOG_TAG, e.toString())
            }
        }
    }

    val meta = PPMap()     // 事件头
    val common = PPMap()        // 事件通用属性
    val userActive = PPMap()    // 用户 active 属性

    override fun toString(): String = "PresetPropManager(\n" +
            "\tdisableList: $disableList, \n" +
            "\teventProperties: $common\n" +
            "\tactiveUserProperties: $userActive\n" +
        ")"

    fun checkNSet(jsonObject: JSONObject, key: String, value: Any?) {
        if (disableList.contains(key)) return
        jsonObject.put(key, value)
    }

    fun checkNSet(map: MutableMap<String, Any?>, key: String, value: Any?) {
        if (disableList.contains(key)) return
        if (value != null) {
            map[key] = value
        } else {
            map.remove(key)
        }
    }


    class PPMap internal constructor() {
        private val _map =  mutableMapOf<String, Any>()
        val map: Map<String, Any>
            get() = _map

        operator fun set(key: String, value: Any?) {
            if (disableList.contains(key)) return
            if (value != null) {
                _map[key] = value
            } else {
                _map.remove(key)
            }
        }

        operator fun get(key: String): Any? {
            return _map[key]
        }

        fun release() {
            _map.clear()
        }

        override fun toString(): String = "$_map"
    }
}

enum class PresetEvent {
    Install,
    SessionStart,
    SessionEnd;

    companion object {
        private val status: MutableMap<PresetEvent, Boolean> by lazy {
            values().associateWith { true }.toMutableMap()
        }
    }

    internal fun isOn(): Boolean = synchronized(this) { status[this] == true }

    internal fun enable() = synchronized(this) {
        status[this] = true
    }

    internal fun disable() = synchronized(this) {
        status[this] = false
    }
}