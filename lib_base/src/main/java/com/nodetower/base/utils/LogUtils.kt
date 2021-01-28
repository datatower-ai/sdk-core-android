package com.nodetower.base.utils

import android.util.Log

object LogUtils {
    private var enableLog = false
    private val CHUNK_SIZE = 4000

    @JvmStatic
    fun d(tag: String?, msg: String?) {
        if (enableLog) {
            info(tag, msg, null)
        }
    }

    @JvmStatic
    fun d(tag: String?, msg: String?, tr: Throwable?) {
        if (enableLog) {
            info(tag, msg, tr)
        }
    }

    @JvmStatic
    fun i(tag: String?, msg: String?) {
        if (enableLog) {
            info(tag, msg, null)
        }
    }

    @JvmStatic
    fun i(tag: String?, tr: Throwable?) {
        if (enableLog) {
            info(tag, "", tr)
        }
    }

    @JvmStatic
    fun i(tag: String?, msg: String?, tr: Throwable?) {
        if (enableLog) {
            info(tag, msg, tr)
        }
    }

    /**
     *
     * @param tag String 标志
     * @param msg String 信息
     * @param tr Throwable 异常
     */
    fun info(tag: String?, msg: String?, tr: Throwable?) {
        try {
            if (msg != null) {
                val bytes = msg.toByteArray()
                val length = bytes.size
                if (length <= CHUNK_SIZE) {
                    Log.i(tag, msg, tr)
                } else {
                    var index = 0
                    var lastIndexOfLF = 0
                    //当最后一次剩余值小于 CHUNK_SIZE 时，不需要再截断
                    while (index < length - CHUNK_SIZE) {
                        lastIndexOfLF = lastIndexOfLF(bytes, index)
                        val chunkLength = lastIndexOfLF - index
                        Log.i(tag, String(bytes, index, chunkLength), null)
                        index = if (chunkLength < CHUNK_SIZE) {
                            //跳过换行符
                            lastIndexOfLF + 1
                        } else {
                            lastIndexOfLF
                        }
                    }
                    if (length > index) {
                        Log.i(tag, String(bytes, index, length - index), tr)
                    }
                }
            } else {
                Log.i(tag, null, tr)
            }
        } catch (e: Exception) {
            printStackTrace(e)
        }
    }

    /**
     * 获取从 fromIndex 开始，最靠近尾部的换行符
     *
     * @param bytes 日志转化的 bytes 数组
     * @param fromIndex 从 bytes 开始的下标
     * @return 换行符的下标
     */
    private fun lastIndexOfLF(bytes: ByteArray, fromIndex: Int): Int {
        val index = Math.min(fromIndex + CHUNK_SIZE, bytes.size - 1)
        for (i in index downTo index - CHUNK_SIZE + 1) {
            //返回换行符的位置
            if (bytes[i] == 10.toByte()) {
                return i
            }
        }
        return index
    }

    /**
     * 此方法谨慎修改
     * 插件配置 disableLog 会修改此方法
     *
     * @param e Exception
     */
    @JvmStatic
    fun printStackTrace(e: Exception?) {
        if (enableLog && e != null) {
            e.printStackTrace()
        }
    }

    /**
     * 设置 Debug 状态
     *
     * @param isDebug Debug 状态
     */
    fun setDebug(isDebug: Boolean) {
        enableLog = isDebug
    }

    /**
     * 设置是否打印 Log
     *
     * @param isEnableLog Log 状态
     */
    fun setEnableLog(isEnableLog: Boolean) {
        enableLog = isEnableLog
    }

    fun isLogEnabled(): Boolean {
        return enableLog
    }
}