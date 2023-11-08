package ai.datatower.analytics.taskqueue

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun <T> AsyncTaskQueue.postTaskSuspended(
    timeoutMs: Long? = 10000,
    task: suspend CoroutineScope.() -> T?,
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

fun <T> AsyncTaskQueue.launchSequential(
    task: suspend CoroutineScope.() -> T,
) = this.launch { runBlocking { task() } }

fun <T> AsyncTaskQueue.asyncSequential(
    task: suspend CoroutineScope.() -> T,
) = this.async { runBlocking { task() } }

fun <T> AsyncTaskQueue.asyncSequentialCatching(
    task: suspend CoroutineScope.() -> T,
): Deferred<Result<T>> = this.asyncSequential {
    Result.runCatching { task() }
}

class AsyncTaskRescheduled<T>(
    private val deferred: Deferred<T>,
    private val queue: AsyncTaskQueue,
) {
    fun <O> onSameQueueThen(block: suspend CoroutineScope.(T) -> O): AsyncTaskRescheduled<O> =
        AsyncTaskRescheduled(queue.async { block(deferred.await()) }, queue)

    suspend fun await(): T = deferred.await()
}

fun <T> AsyncTaskQueue.asyncSequentialChained(
    block: suspend CoroutineScope.() -> T,
): AsyncTaskRescheduled<T> =
    AsyncTaskRescheduled(asyncSequential(block), this)
