package kt.nio.util

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.nio.ByteBuffer

fun ByteArrayOutputStream.toByteArrayInputStream(): ByteArrayInputStream {
    return ByteArrayInputStream(toByteArray())
}


fun ByteBuffer.putByteBuffer(src: ByteBuffer):Int{
    if (src === this) {
        throw IllegalArgumentException("The source buffer is this buffer")
    } else {
        var count=0
        while (hasRemaining() && src.hasRemaining()){
            put(src.get())
            count++
        }
        return count
    }
}

fun ByteArray.checkRange(offset:Int,length:Int){
    if (offset<0)
        throw IndexOutOfBoundsException("offset error: $offset")
    if (length<0)
        throw IndexOutOfBoundsException("length error: $length")
    if (offset+length>this.size)
        throw IndexOutOfBoundsException("out of range size:${this.size} offset:$offset,length:$length")
}

fun ByteBuffer.enlargeBuffer(size:Int):ByteBuffer{
    if (size > capacity()){
        val tmpBuff=ByteBuffer.allocate(size)
        tmpBuff.put(array())
        tmpBuff.position(position())
        tmpBuff.limit(limit())
        return tmpBuff
    }else{
        return this
    }
}

suspend fun Closeable.useInTime(timeOut:Int, block:suspend ()->Unit){
    val context=currentCoroutineContext()
    val job=GlobalScope.launch(context) {
        delay(timeOut.toLong())
        close()
    }
    try {
        block()
    }finally {
        job.cancel()
    }
}