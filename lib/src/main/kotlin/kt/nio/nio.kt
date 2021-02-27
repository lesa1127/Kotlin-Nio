package kt.nio

import java.io.Closeable
import java.io.IOException
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.*

interface CloseAble: Closeable {
    val isClosed:Boolean
}

fun CloseAble.checkClosed(throwable: String){
    if (isClosed)
        throw IOException(throwable)
}
inline fun CloseAble.whenClosed(block: () -> Unit){
    if (isClosed)
        block()
}

inline fun CloseAble.whenNotClosed(block:()->Unit){
    if (!isClosed)
        block()
}

interface CoroutineChannel:CloseAble

interface AcceptAble:CoroutineChannel{
    suspend fun accept(): Channel
}

interface ConnectAble:CoroutineChannel{
    suspend fun connect(socketAddress: SocketAddress)
}

interface ReadAble: CoroutineChannel{
    suspend fun read(buff:ByteBuffer):Int
}

interface WriteAble: CoroutineChannel{
    suspend fun write(buff:ByteBuffer):Int
}

interface ReadWriteAble:WriteAble,ReadAble
