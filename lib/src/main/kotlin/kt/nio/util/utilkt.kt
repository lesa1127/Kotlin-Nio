package kt.nio.util

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

inline fun Closeable.useInTime(timeOut:Int, timer: Timer=Timer.getTimer(), block:()->Unit){
    val task = timer.schedule(System.currentTimeMillis()+timeOut){
        close()
    }
    try {
        block()
    }finally {
        task.cancle()
    }
}