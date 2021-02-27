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


fun GlobalScope.launchIOScope(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    return GlobalScope.launch(NioDispatcher.IO,start,block)
}


fun<T> GlobalScope.asyncIOScope(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): Deferred<T> {
    return GlobalScope.async(NioDispatcher.IO,start,block)
}

fun<T> runBlockingIOScope(block: suspend CoroutineScope.() -> T): T{
    return runBlocking(NioDispatcher.IO,block)
}



fun CoroutineScope.launchIOScope(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
): Job {
    return launch(NioDispatcher.IO,start,block)
}


fun<T> CoroutineScope.asyncIOScope(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): Deferred<T> {
    return async(NioDispatcher.IO,start,block)
}
