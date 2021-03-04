package kt.nio

import kotlinx.coroutines.*
import kt.nio.singlethread.select.asIOCoroutineDispatcher
import java.util.concurrent.Executors
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

internal val AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors()
internal const val MIN_PROCESSORS=1

internal val CoroutineContext.coroutineDispatcher: CoroutineDispatcher get() = get(ContinuationInterceptor) as CoroutineDispatcher

object NioDispatcher{
    @Volatile
    private var io:CoroutineContext?=null
    val IO:CoroutineContext get() {
        io?.also {
            return it
        }
        synchronized(NioDispatcher){
            if (io==null){
                io = Executors.newSingleThreadExecutor().asCoroutineDispatcher().asIOCoroutineDispatcher()
            }
            return io!!
        }
    }
}
