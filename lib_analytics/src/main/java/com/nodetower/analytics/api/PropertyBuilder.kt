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
     * 添加键值对，可变参数中，奇数位置对应的是 Key，偶数位置对应的是 Value，如果参数长度不是偶数，那么就忽略最后
     * 一位，保持在偶数长度配对，如果存在将覆盖；如果奇数位 Key 不是 String 类型，那么就忽略对应位置的 Value。
     *
     * @param keyValuePairs 键值对，奇数为 String 类型 key，偶数为 Object 类型 value
     * @return PropertyBuilder
     */
//    fun append(vararg keyValuePairs: Any): PropertyBuilder {
//        if (keyValuePairs == null || keyValuePairs.size <= 1) {
//            LogUtils.i(TAG, "The key value pair is incorrect.")
//            return this
//        }
//        var index = 0
//        while (index < keyValuePairs.size) {
//            val keyObj = keyValuePairs[index]
//            index += 1
//            if (index >= keyValuePairs.size) {
//                LogUtils.i(
//                    TAG, "this element key[index= " + index + "] will be ignored," +
//                            " because no element can pair with it. "
//                )
//                return this
//            }
//            val valueObj = keyValuePairs[index]
//            if (keyObj is String) {
//                innerPropertyMap[keyObj] = valueObj
//            } else {
//                LogUtils.i(
//                    TAG, "this element key[index= " + index + "] is not a String," +
//                            " the method will ignore the element and the next element. "
//                )
//            }
//            index++
//        }
//        return this
//    }

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
