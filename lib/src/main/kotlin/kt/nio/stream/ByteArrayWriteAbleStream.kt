package kt.nio.stream

import kt.nio.checkClosed
import kt.nio.util.checkRange
import java.util.*

open class ByteArrayWriteAbleStream(size:Int=32) :WriteAbleStream {

    init {
        require(size >= 0) {
            ("Negative initial size: "
                    + size)
        }
    }
    protected var buf=ByteArray(size)
    protected var count = 0

    private val MAX_ARRAY_SIZE = Int.MAX_VALUE - 8

    protected var closeStatus=false


    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    private fun grow(minCapacity: Int) {
        // overflow-conscious code
        val oldCapacity = buf.size
        var newCapacity = oldCapacity shl 1
        if (newCapacity - minCapacity < 0) newCapacity = minCapacity
        if (newCapacity - MAX_ARRAY_SIZE > 0) newCapacity =
            hugeCapacity(minCapacity)
        buf = Arrays.copyOf(buf, newCapacity)
    }

    private fun hugeCapacity(minCapacity: Int): Int {
        if (minCapacity < 0) throw OutOfMemoryError()
        return if (minCapacity > MAX_ARRAY_SIZE) Int.MAX_VALUE else MAX_ARRAY_SIZE
    }

    /**
     * Increases the capacity if necessary to ensure that it can hold
     * at least the number of elements specified by the minimum
     * capacity argument.
     *
     * @param  minCapacity the desired minimum capacity
     * @throws OutOfMemoryError if `minCapacity < 0`.  This is
     * interpreted as a request for the unsatisfiably large capacity
     * `(long) Integer.MAX_VALUE + (minCapacity - Integer.MAX_VALUE)`.
     */
    private fun ensureCapacity(minCapacity: Int) {
        // overflow-conscious code
        if (minCapacity - buf.size > 0) grow(minCapacity)
    }

    override suspend fun write(byte: Int) {
        checkClosed("Stream Has be Closed!")
        ensureCapacity(count + 1)
        buf[count] = byte.toByte()
        count += 1
    }

    override suspend fun write(buff: ByteArray, offset: Int, length: Int) {
        checkClosed("Stream Has be Closed!")
        buff.checkRange(offset,length)
        ensureCapacity(count + length)
        System.arraycopy(buff, offset, buf, count, length)
        count += length
    }


    override val isClosed: Boolean
        get() = closeStatus

    override fun close() {
        closeStatus=true
    }

    suspend fun writeTo(out:WriteAbleStream){
        out.write(buf,0,count)
    }


    override suspend fun flush() {

    }

    fun reset() {
        count=0
    }

    fun toByteArray(): ByteArray {
        return buf.copyOf(count)
    }

    val size:Int
    get() {
        return count
    }

    override fun toString(): String = String(buf, 0, count)
}