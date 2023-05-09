package com.roiquery.analytics.taskqueue

import kotlinx.coroutines.runBlocking
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun <T> AsyncTaskQueue.postTaskSuspended(
    timeoutMs: Long? = 10000,
    task: suspend () -> T?
): Result<T?> = suspendCoroutine { continuation ->
    val latch = CountDownLatch(1)
    var result: Result<T?> = Result.failure(InterruptedException())
    this.postTask {
        result = Result.runCatching { runBlocking { task() } }
        latch.countDown()
    }
    try {
        if (timeoutMs != null) {
            latch.await(timeoutMs, TimeUnit.MILLISECONDS)
        } else {
            latch.await()
        }
    } catch (ignored: InterruptedException) {
    }
    continuation.resume(result)
}
