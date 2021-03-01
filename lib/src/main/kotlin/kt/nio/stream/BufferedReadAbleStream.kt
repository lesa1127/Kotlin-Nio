package kt.nio.stream

import kt.nio.checkClosed
import kt.nio.util.checkRange
import kt.nio.whenClosed
import kt.nio.whenNotClosed
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.min


/**
 * A <code>BufferedReadAbleStream</code> adds
 * functionality to another input stream-namely,
 * the ability to buffer the input and to
 * support the <code>mark</code> and <code>reset</code>
 * methods. When  the <code>BufferedReadAbleStream</code>
 * is created, an internal buffer array is
 * created. As bytes  from the stream are read
 * or skipped, the internal buffer is refilled
 * as necessary  from the contained input stream,
 * many bytes at a time. The <code>mark</code>
 * operation  remembers a point in the input
 * stream and the <code>reset</code> operation
 * causes all the  bytes read since the most
 * recent <code>mark</code> operation to be
 * reread before new bytes are  taken from
 * the contained input stream.
 *
 */
open class BufferedReadAbleStream(override val readAbleStream: ReadAbleStream, buffSize:Int=4096):
    FilterReadAbleStream(readAbleStream) {

    private val byteArrayBuff=ByteArray(buffSize)
    private val byteBuff= ByteBuffer.wrap(byteArrayBuff).apply {
        position(0)
        limit(0)
    }
    private val MAX_SKIP_BUFFER_SIZE=2048L

    override val isClosed: Boolean
        get() = closeStatus
    private var closeStatus=false
    override val markSupported: Boolean = true

    override suspend fun read(): Int {
        while (true){
            checkClosed("Stream Closed!")

            if (byteBuff.hasRemaining()){
                return byteBuff.get().toInt() and 0xFF
            }else{
                fill()
                whenClosed {
                    return -1
                }
            }
        }
    }

    override suspend fun read(buff: ByteArray, offset: Int, length: Int): Int {
        checkClosed("Stream Closed!")

        buff.checkRange(offset, length)

        if (byteBuff.hasRemaining()){
            val readLen = min(byteBuff.remaining(),length)
            for (i in 0 until readLen){
                buff[i+offset]=byteBuff.get()
            }
            return readLen
        }else{
            fill()
        }

        return 0
    }

    override val available: Int
        get() = byteBuff.remaining()

    override suspend fun fill(){
        checkClosed("Stream Closed!")

        if (byteBuff.hasRemaining()){
            byteBuff.compact()
        }else {
            byteBuff.clear()
        }
        val len = readAbleStream.read(byteArrayBuff,byteBuff.position(),byteBuff.remaining())

        if (len>=0){
            val position=byteBuff.position()
            //让mark无效化
            byteBuff.flip()
            byteBuff.limit(position+len)
            byteBuff.position(0)
        }else{
            close()
        }
    }

    override fun close() {
        whenNotClosed{
            closeStatus=true
            readAbleStream.close()
        }
    }

    /**
     * Marks the current position in this input stream. A subsequent call to
     * the <code>reset</code> method repositions this stream at the last marked
     * position so that subsequent reads re-read the same bytes.
     *
     * @param   len   the maximum limit of bytes that can be read before
     *                the mark position becomes invalid.at the same time the number of limit can't over available length size
     */
    override fun mark(len: Int) {
        if (len>available)
            throw IOException("mark length over size")
        byteBuff.mark()
    }

    override fun reset() {
        byteBuff.reset()
    }

    override suspend fun skip(n: Long): Long {
        var remaining = n
        var nr: Int

        if (n <= 0) {
            return 0
        }

        val size = min(MAX_SKIP_BUFFER_SIZE, remaining).toInt()
        val skipBuffer = ByteArray(size)
        while (remaining > 0) {
            nr = read(skipBuffer, 0, min(size.toLong(), remaining).toInt())
            if (nr < 0) {
                break
            }
            remaining -= nr.toLong()
        }

        return n - remaining
    }
}