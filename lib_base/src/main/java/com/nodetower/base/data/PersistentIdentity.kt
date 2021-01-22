package com.nodetower.base.data

import android.annotation.SuppressLint
import android.content.SharedPreferences
import com.nodetower.base.utils.LogUtils
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future


@SuppressLint("CommitPrefEdits")
abstract class PersistentIdentity<T>  constructor(
    private val loadStoredPreferences: Future<SharedPreferences>,
    private val persistentKey: String,
    private val serializer: PersistentSerializer<T>
) {

    private var item: T? = null

    /**
     * 获取存储的值
     *
     * @return 存储的值
     */
    fun get(): T? {
        if (item == null) {
            var data: String? = null
            synchronized(loadStoredPreferences) {
                try {
                    val sharedPreferences: SharedPreferences = loadStoredPreferences.get()
                    data = sharedPreferences.getString(persistentKey, null)
                } catch (e: ExecutionException) {
                    LogUtils.d(
                        TAG,
                        "Cannot read distinct ids from sharedPreferences.",
                        e.cause
                    )
                } catch (e: InterruptedException) {
                    LogUtils.d(
                        TAG,
                        "Cannot read distinct ids from sharedPreferences.",
                        e.cause
                    )
                }
                if (data == null) {
                    item = serializer.create() as T
                    commit(item)
                } else {
                    item = serializer.load(data) as T
                }
            }
        }
        return item
    }

    /**
     * 保存数据值
     *
     * @param item 数据值
     */
    fun commit(item: T?) {
        this.item = item
        synchronized(loadStoredPreferences) {
            var sharedPreferences: SharedPreferences? = null
            try {
                sharedPreferences = loadStoredPreferences.get()
            } catch (e: ExecutionException) {
                LogUtils.d(
                    TAG,
                    "Cannot read distinct ids from sharedPreferences.",
                    e.cause
                )
            } catch (e: InterruptedException) {
                LogUtils.d(
                    TAG,
                    "Cannot read distinct ids from sharedPreferences.",
                    e.cause
                )
            }
            if (sharedPreferences == null) {
                return
            }
            val editor = sharedPreferences.edit()
            if (this.item == null) {
                this.item = serializer.create() as T
            }
            editor.putString(persistentKey, serializer.save(this.item!!))
            editor.apply()
        }
    }

    /**
     * Persistent 序列化接口
     *
     * @param <T> 数据类型
    </T> */
    interface PersistentSerializer<T> {
        /**
         * 读取数据
         *
         * @param value，Value 值
         * @return 返回值
         */
        fun load(value: String?): T

        /**
         * 保存数据
         *
         * @param item 数据值
         * @return 返回存储的值
         */
        fun save(item: T): String?

        /**
         * 创建默认值
         *
         * @return 默认值
         */
        fun create(): T
    }

    companion object {
        private const val TAG = "NT.PersistentIdentity"
    }

}