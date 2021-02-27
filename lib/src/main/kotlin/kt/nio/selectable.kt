package kt.nio

import java.io.Closeable
import java.nio.channels.*

interface BaseSelectAble:CoroutineChannel {
    val channel: SelectableChannel
    val ops:Int
    val selectionKey:SelectionKey
    val selector:BaseSelector
}


interface BaseSelector: Closeable {
    val selector: Selector

    suspend fun select(selectAble: BaseSelectAble)
}