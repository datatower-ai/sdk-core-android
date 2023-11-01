package com.roiquery.analytics.taskqueue.thread

import android.os.Handler
import android.os.HandlerThread

/**
 * 便捷函数，用法：
 * ```kotlin
 * runInBackground {
 *   // code runs in AndroidExecutor
 * }
 * ```
 */
internal fun runInBackground(delayMillis: Long = 0, func: () -> Unit) {
    AndroidExecutor.execute(Runnable(func), delayMillis)
}

internal object AndroidExecutor {
    private val handlerThread by lazy {
        HandlerThread("Android-Executor", Thread.MIN_PRIORITY).also {
            it.start()
        }
    }

    private val handler: Handler by lazy {
        Handler(handlerThread.looper)
    }

    /**
     * 提前触发线程的创建，否则懒加载
     */
    fun warnUp() {
        handler.toString()
    }

    fun execute(runnable: Runnable, delayMillis: Long = 0) {
        handler.postDelayed(runnable, delayMillis)
    }
}