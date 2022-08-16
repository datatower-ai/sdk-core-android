package com.roiquery.analytics

import androidx.annotation.RestrictTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.io.Closeable
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext

/**
 * author: xiaosailing
 * date: 2022-08-16
 * description:
 * version：1.0
 */
open class ROIQueryCoroutineScope internal constructor(){

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    private var mInternalScopeRef = AtomicReference<Any>()

    val scope: CoroutineScope
        get() {
            while (true) {
                val existing = mInternalScopeRef.get() as CoroutineScope?
                if (existing != null) {
                    return existing
                }
                val newScope = CoroutineScope(
                    SupervisorJob() + Dispatchers.Main.immediate
                )
                if (mInternalScopeRef.compareAndSet(null, newScope)) {
                    return newScope
                }
            }
        }

    internal class CloseableCoroutineScope(context: CoroutineContext) : Closeable, CoroutineScope {
        override val coroutineContext: CoroutineContext = context

        override fun close() {
            coroutineContext.cancel()
        }
    }

}