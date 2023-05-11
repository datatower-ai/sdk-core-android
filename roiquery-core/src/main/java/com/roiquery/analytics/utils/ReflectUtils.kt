package com.roiquery.analytics.utils

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

class ReflectUtils private constructor() {
    companion object{
        private const val TAG = "ReflectUtils"
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED)  {
            ReflectUtils()
        }
    }

    /**
     * < 获取类对象 Constructor >.
     *
     * @author bugliee
     * @create 2022/5/16
     * @param className 完整类名
     * @return [Object]
     */
    fun createObject(className: String?): Any? {
        var clazz: Class<*>? = null
        try {
            clazz = className?.let { Class.forName(it) }
            val constructor = clazz?.getDeclaredConstructor()
            return constructor?.newInstance()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return clazz
    }

    /**
     * < 获取类实例 getInstance >.
     *
     * @author bugliee
     * @create 2022/5/16
     * @param className 完整类名
     * @return [Object]
     */
    fun getObjectInstance(className: String?): Any? {
        var clazz: Class<*>? = null
        try {
            className?.let {
                clazz = Class.forName(className)
                return clazz?.getMethod("getInstance")?.invoke(clazz)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return clazz
    }

    /**
     * < 调用Getter方法 >.
     *
     * @author bugliee
     * @create 2022/5/16
     * @param obj 对象
     * @param propertyName 属性名
     * @return [Object]
     */
    fun invokeGetterMethod(obj: Any, propertyName: String): Any? {
        val getterMethodName = "get" + propertyName.trim { it <= ' ' }
        return invokeMethod(obj, getterMethodName, arrayOf())
    }

    /**
     * < 调用Setter方法.使用value的Class来查找Setter方法 >.
     *
     * @author bugliee
     * @create 2022/5/16
     * @param obj 对象
     * @param propertyName 属性名
     * @param value 传入值
     */
    fun invokeSetterMethod(obj: Any, propertyName: String, value: Any) {
        invokeSetterMethod(obj, propertyName, value, null)
    }

    /**
     * < 调用Setter方法 >.
     *
     * @author bugliee
     * @create 2022/5/16
     * @param obj 对象
     * @param propertyName 属性名
     * @param value 传入值
     * @param propertyType setter的参数类型，为空默认使用value的类型
     */
    fun invokeSetterMethod(obj: Any, propertyName: String, value: Any, propertyType: Class<*>?) {
        val type = propertyType ?: value.javaClass
        val setterMethodName = "set" + propertyName.trim { it <= ' ' }
        invokeMethod(obj, setterMethodName, arrayOf(value), type)
    }

    /**
     * < 直接读取对象属性值 >.
     *
     * @author bugliee
     * @create 2022/5/16
     * @param obj 对象
     * @param fieldName 属性名
     * @return [Object]
     */
    fun getFieldValue(obj: Any, fieldName: String): Any? {
        val field = getAccessibleField(obj, fieldName)
            ?: throw IllegalArgumentException("Could not find field [$fieldName] on target [$obj]")
        var result: Any? = null
        try {
            result = field[obj]
        } catch (e: IllegalAccessException) {
            LogUtils.e(TAG, e.message)
        }
        return result
    }

    /**
     * < 直接设置对象属性值 >.
     *
     * @author bugliee
     * @create 2022/5/16
     * @param obj 对象
     * @param fieldName 属性名
     * @param value 传入值
     */
    fun setFieldValue(obj: Any, fieldName: String, value: Any?) {
        val field = getAccessibleField(obj, fieldName)
            ?: throw IllegalArgumentException("Could not find field [$fieldName] on target [$obj]")
        try {
            field[obj] = value
        } catch (e: IllegalAccessException) {
            LogUtils.e(TAG, e.message)
        }
    }

    /**
     * < 循环向上转型, 获取对象的DeclaredField >.
     *
     * @author bugliee
     * @create 2022/5/16
     * @param obj 对象
     * @param fieldName 属性名
     * @return [Field]
     */
    fun getAccessibleField(obj: Any, fieldName: String?): Field? {
        var superClass: Class<*>? = obj.javaClass
        while (superClass != Any::class.java) {
            try {
                if (superClass != null) {
                    val field = superClass.getDeclaredField(fieldName)
                    field.isAccessible = true
                    return field
                }
            } catch (e: NoSuchFieldException) {
                // ignore
            }
            superClass = superClass!!.superclass
        }
        return null
    }

    /**
     * < 直接调用对象方法 >.
     *
     * @param obj            对象
     * @param methodName     方法名
     * @param parameterTypes 参数类型
     * @param args           参数列表
     * @return [Object]
     * @author bugliee
     * @create 2022/5/16
     */
    fun invokeMethod(
        obj: Any,
        methodName: String,
        args: Array<Any?>,
        vararg parameterTypes: Class<*>?
    ): Any? {
        val method = getAccessibleMethod(obj, methodName, *parameterTypes)
        if (method == null) {
            LogUtils.i(TAG,
                "Could not find method [$methodName] on target [$obj]"
            )
            return null
        }
        return try {
            method.invoke(obj, *args)
        } catch (e: Exception) {
            LogUtils.e(TAG, e.message)
            null
        }
    }

    /**
     * < 循环向上转型, 获取对象的DeclaredMethod >.
     *
     * @author bugliee
     * @create 2022/5/16
     * @param obj 对象
     * @param methodName 方法名
     * @param parameterTypes 参数类型
     * @return [Method]
     */
    fun getAccessibleMethod(
        obj: Any?, methodName: String?,
        vararg parameterTypes: Class<*>?
    ): Method? {
        if (obj == null) {
            LogUtils.i(TAG, "obj is null!")
            return null
        }
        var superClass: Class<*> = obj.javaClass
        while (superClass != Any::class.java) {
            try {
                val method = superClass.getDeclaredMethod(methodName, *parameterTypes)
                method.isAccessible = true
                return method
            } catch (e: NoSuchMethodException) {
                // ignore
            }
            superClass = superClass.superclass
        }
        return null
    }

    /**
     * < 获取父类泛型参数类型 >.
     *
     * @author bugliee
     * @create 2022/5/16
     * @param clazz 类
     * @return [Class]
     */
    fun <T> getSuperClassGenericType(clazz: Class<*>): Class<*> {
        return getSuperClassGenericType(clazz, 0)
    }

    /**
     * < description >.
     *
     * @author bugliee
     * @create 2022/5/16
     * @param clazz 类
     * @param index 索引，0..
     * @return [Class]
     */
    fun getSuperClassGenericType(clazz: Class<*>, index: Int): Class<*> {
        val genType = clazz.genericSuperclass
        if (genType !is ParameterizedType) {
            LogUtils.w(TAG,
                clazz.simpleName + "'s superclass not ParameterizedType"
            )
            return Any::class.java
        }
        val params = genType.actualTypeArguments
        if (index >= params.size || index < 0) {
            LogUtils.w(TAG,
                "Index: " + index + ", Size of " + clazz.simpleName + "'s Parameterized Type: "
                        + params.size
            )
            return Any::class.java
        }
        if (params[index] !is Class<*>) {
            LogUtils.w(TAG,
                clazz.simpleName + " not set the actual class on superclass generic parameter"
            )
            return Any::class.java
        }
        return params[index] as Class<*>
    }

    /**
     * 反射调用静态方法
     * @param className
     * @param methodName
     * @param args
     * @param parameterTypes
     */
    @Throws(Exception::class)
    fun invokeStaticMethod(
        className: String?,
        methodName: String?,
        args: Array<Any?>,
        vararg parameterTypes: Class<*>?
    ) {
        val clazz = Class.forName(className)
        val method = clazz.getDeclaredMethod(methodName, *parameterTypes)
        method.invoke(null, *args)
    }
}