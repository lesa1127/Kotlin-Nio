package kt.nio.stream

import kt.nio.checkClosed
import kt.nio.util.checkRange
import kotlin.math.min

open class ByteArrayReadAbleStream(
    protected val buff: ByteArray, offset: Int=0, length: Int=buff.size-offset
) : ReadAbleStream {

    private var clostatus = false

    init {
        buff.checkRange(offset,length)
    }

    protected var pos=offset
    protected var count= min(offset+length,buff.size)
    protected var mark=offset

    override suspend fun read(): Int {
        checkClosed("Stream Has be Closed!")

        return if (pos < count) buff[pos++].toInt() and 0xff else {
            close()
            -1
        }
    }

    override suspend fun read(buff: ByteArray, offset: Int, length: Int): Int {
        checkClosed("Stream Has be Closed!")

        buff.checkRange(offset,length)
        if (pos >= count) {
            close()
            return -1
        }
        val avail = count - pos
        val len = min(avail,length)
        if (len <= 0) {
            return 0
        }
        System.arraycopy(this.buff, pos, buff, offset, len)
        pos+=len
        return len
    }

    override val isClosed: Boolean
        get() = clostatus

    override fun close() {
        clostatus=true
    }

    override val markSupported: Boolean = true

    override fun mark(len: Int) {
        mark = pos
    }

    override fun reset() {
        pos = mark
    }

    override val available: Int
        get() = count - pos

    override suspend fun fill() {

    }

    override suspend fun skip(n: Long): Long {
        var k = (count - pos).toLong()
        if (n < k) {
            k = if (n < 0) 0 else n
        }

        pos += k.toInt()
        return k
    }
}