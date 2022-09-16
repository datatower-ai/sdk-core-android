package com.roiquery.analytics_demo

import android.content.Context
import android.content.SharedPreferences


class SharedPreferencesUtils(mContext: Context, preferenceName: String?) {
    private val preferences: SharedPreferences
    private val editor: SharedPreferences.Editor

    companion object {
        /**
         * 保存在手机里面的文件名
         */
        private const val FILE_NAME = "share_date"
        /**
         * 示例
         * private void saveConfig(Config config) {
         * DataSave data = new DataSave(this, "ConfigData");
         * data.setData("config",config);
         * }
         *
         * private Config loadConfig() {
         * DataSave data = new DataSave(this, "ConfigData");
         * return data.getData("config", Config.class);
         * }
         */
        /**
         * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
         * @param context
         * @param key
         * @param object
         */
        fun setParam(context: Context, key: String?, `object`: Any) {
            val type = `object`.javaClass.simpleName
            val sp: SharedPreferences =
                context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            val editor = sp.edit()
            if ("String" == type) {
                editor.putString(key, `object` as String)
            } else if ("Integer" == type) {
                editor.putInt(key, (`object` as Int))
            } else if ("Boolean" == type) {
                editor.putBoolean(key, (`object` as Boolean))
            } else if ("Float" == type) {
                editor.putFloat(key, (`object` as Float))
            } else if ("Long" == type) {
                editor.putLong(key, (`object` as Long))
            }
            editor.commit()
        }

        /**
         * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
         * @param context
         * @param key
         * @param defaultObject
         * @return
         */
        fun getParam(context: Context, key: String?, defaultObject: Any): Any? {
            val type = defaultObject.javaClass.simpleName
            val sp: SharedPreferences =
                context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
            if ("String" == type) {
                return sp.getString(key, defaultObject as String)
            } else if ("Integer" == type) {
                return sp.getInt(key, (defaultObject as Int))
            } else if ("Boolean" == type) {
                return sp.getBoolean(key, (defaultObject as Boolean))
            } else if ("Float" == type) {
                return sp.getFloat(key, (defaultObject as Float))
            } else if ("Long" == type) {
                return sp.getLong(key, (defaultObject as Long))
            }
            return null
        }

        /**
         * 清除所有数据
         * @param context
         */
        fun clear(context: Context) {
            val sp: SharedPreferences = context.getSharedPreferences(
                FILE_NAME,
                Context.MODE_PRIVATE
            )
            val editor = sp.edit()
            editor.clear().commit()
        }

        /**
         * 清除指定数据
         * @param context
         */
        fun clearAll(context: Context, key: String?) {
            val sp: SharedPreferences = context.getSharedPreferences(
                FILE_NAME,
                Context.MODE_PRIVATE
            )
            val editor = sp.edit()
            editor.remove(key)
            editor.commit()
        }

        fun setSharedPreference(key: String?, values: Array<String?>?, context: Context) {
            val regularEx = "#"
            var str: String? = ""
            val sp: SharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
            if (values != null && values.size > 0) {
                for (value in values) {
                    str += value
                    str += regularEx
                }
                val et = sp.edit()
                et.putString(key, str)
                et.commit()
            }
        }

        fun getSharedPreference(key: String?, context: Context): Array<String> {
            val regularEx = "#"
            var str: Array<String>? = null
            val sp: SharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
            val values: String?
            values = sp.getString(key, "")
            str = values!!.split(regularEx).toTypedArray()
            return str
        }
    }

    init {
        preferences = mContext.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
        editor = preferences.edit()
    }
}

