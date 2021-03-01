package kt.nio.stream

import kt.nio.checkClosed
import kt.nio.util.checkRange
import kt.nio.whenNotClosed
import java.nio.ByteBuffer
import kotlin.math.min


/**
 * The class implements a buffered output stream. By setting up such
 * an output stream, an application can write bytes to the underlying
 * output stream without necessarily causing a call to the underlying
 * system for each byte written.
 *
 */
open class BufferedWriteAbleStream(override val writeAbleStream: WriteAbleStream, buffSize:Int=4096):
    FilterWriteAbleStream(writeAbleStream){

    private val byteArrayBuff=ByteArray(buffSize)
    private val byteBuff= ByteBuffer.wrap(byteArrayBuff)

    override val isClosed: Boolean
        get() = closeStatus
    private var closeStatus=false

    override suspend fun write(byte: Int) {
        while (true) {
            checkClosed("Stream Closed!")

            if (byteBuff.hasRemaining()) {
                byteBuff.put(byte.toByte())
                return
            } else {
                flush()
            }
        }
    }

    override suspend fun write(buff: ByteArray, offset: Int, length: Int): Int {
        checkClosed("Stream Closed!")

        buff.checkRange(offset,length)

        if (byteBuff.hasRemaining()) {
            val writeLen = min(length,byteBuff.remaining())
            for (i in 0 until writeLen){
                byteBuff.put(buff[offset+i])
            }
            return writeLen
        } else {
            flush()
        }
        return 0
    }

    override suspend fun writeFully(byteArray: ByteArray, off: Int, len: Int) {
        writeBytes(byteArray,off,len)
    }

    override suspend fun flush() {
        checkClosed("Stream Closed!")

        byteBuff.flip()
        while (byteBuff.hasRemaining()){
            checkClosed("Stream Closed!")

            val len=writeAbleStream.write(byteArrayBuff,byteBuff.position(),byteBuff.remaining())
            if(len >=0){
                byteBuff.position(byteBuff.position()+len)
            }else{
                close()
            }
        }
        byteBuff.clear()
        super.flush()
    }

    override fun close() {
        whenNotClosed {
            closeStatus=true
            writeAbleStream.close()
        }
    }
}