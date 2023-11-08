package ai.datatower.analytics.taskqueue.thread

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper

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

internal fun runInBackground(runnable: Runnable, delayMillis: Long = 0) {
    AndroidExecutor.execute(runnable, delayMillis)
}

internal fun runInMain(delayMillis: Long = 0, func: () -> Unit) {
    AndroidExecutor.executeInMain(Runnable(func), delayMillis)
}

internal fun runInMain(runnable: Runnable, delayMillis: Long = 0) {
    AndroidExecutor.executeInMain(runnable, delayMillis)
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

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    /**
     * 提前触发线程的创建，否则懒加载
     */
    fun warnUp() {
        handler.toString()
    }

    fun execute(runnable: Runnable, delayMillis: Long = 0) {
        handler.postDelayed(runnable, delayMillis)
    }

    fun executeInMain(runnable: Runnable, delayMillis: Long = 0) {
        mainHandler.postDelayed(runnable, delayMillis)
    }
}