package com.nodetower.analytics.api

import com.nodetower.base.utils.LogUtils
import org.json.JSONObject
import java.util.*


class PropertyBuilder private constructor() {
    private val innerPropertyMap: LinkedHashMap<String?, Any> = LinkedHashMap()

    /**
     * 添加 key - value 对
     *
     * @param key key
     * @param value value
     * @return PropertyBuilder
     */
    fun append(key: String?, value: Any): PropertyBuilder {
        innerPropertyMap[key] = value
        return this
    }

    /**
     * 添加 Map 集合
     *
     * @param propertyMap propertyMap
     * @return PropertyBuilder
     */
    fun append(propertyMap: Map<String?, Any>?): PropertyBuilder {
        if (propertyMap != null && !propertyMap.isEmpty()) {
            innerPropertyMap.putAll(propertyMap)
        }
        return this
    }

    /**
     * 获取 JSONObject 对象
     *
     * @return JSONObject
     */
    fun toJSONObject(): JSONObject? {
        innerPropertyMap.remove(null)
        if (innerPropertyMap.isEmpty()) {
            return null
        }
        val jsonObject = JSONObject()
        for (key in innerPropertyMap.keys) {
            try {
                jsonObject.put(key, innerPropertyMap[key])
            } catch (ex: Exception) {
                LogUtils.printStackTrace(ex)
            }
        }
        return jsonObject
    }

    /**
     * 获取属性个数
     *
     * @return size
     */
    fun size(): Int {
        return innerPropertyMap.size
    }

    /**
     * 删除指定属性
     *
     * @param key key
     * @return 删除成功返回 key 对应的 value，否则返回 null (假如 key 对应的 value 是 null，那么返回的值也是 null)
     */
    fun remove(key: String?): Any {
        return innerPropertyMap.remove(key)!!
    }

    /**
     * 删除所有的 property
     */
    fun clear() {
        innerPropertyMap.clear()
    }

    companion object {
        private const val TAG = "PropertyBuilder"
        fun newInstance(): PropertyBuilder {
            return PropertyBuilder()
        }
    }

}
