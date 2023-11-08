package ai.datatower.analytics



/**
 * author: xiaosailing
 * date: 2022-08-16
 * description:
 * version：1.0
 */
//open class ROIQueryCoroutineScope internal constructor(){
//
//    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
//    private var mInternalScopeRef = AtomicReference<Any>()
//
//    val scope: CoroutineScope
//        get() {
//            val existing = mInternalScopeRef.get() as CoroutineScope?
//            if (existing != null) {
//                return existing
//            }
//            val newScope = CoroutineScope(
//                SupervisorJob() + Dispatchers.Main.immediate
//            )
//            if (mInternalScopeRef.compareAndSet(null, newScope)) {
//                return newScope
//            }
//            return newScope
//        }
//
//    internal class CloseableCoroutineScope(context: CoroutineContext) : Closeable, CoroutineScope {
//        override val coroutineContext: CoroutineContext = context
//
//        override fun close() {
//            coroutineContext.cancel()
//        }
//    }
//
//}