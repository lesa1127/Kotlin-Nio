package kt.nio.stream

import kt.nio.*
import kt.nio.util.checkRange
import java.io.EOFException
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.min

interface ReadAbleStream: CloseAble,ResetAble,FillAble {

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * <p> A subclass must provide an implementation of this method.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
     */
    suspend fun read():Int

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes.  An attempt is made to read as many as
     * <code>len</code> bytes, but a smaller number may be read.
     * The number of bytes actually read is returned as an integer.
     *
     * <p> This method blocks until input data is available, end of file is
     * detected, or an exception is thrown.
     *
     * <p> If <code>len</code> is zero, then no bytes are read and
     * <code>0</code> is returned; otherwise, there is an attempt to read at
     * least one byte. If no byte is available because the stream is at end of
     * file, the value <code>-1</code> is returned; otherwise, at least one
     * byte is read and stored into <code>b</code>.
     *
     * <p> The first byte read is stored into element <code>b[offset]</code>, the
     * next one into <code>b[off+1]</code>, and so on. The number of bytes read
     * is, at most, equal to <code>len</code>. Let <i>k</i> be the number of
     * bytes actually read; these bytes will be stored in elements
     * <code>b[offset]</code> through <code>b[off+</code><i>k</i><code>-1]</code>,
     * leaving elements <code>b[off+</code><i>k</i><code>]</code> through
     * <code>b[off+len-1]</code> unaffected.
     *
     * <p> In every case, elements <code>b[0]</code> through
     * <code>b[offset]</code> and elements <code>b[off+len]</code> through
     * <code>b[b.length-1]</code> are unaffected.
     *
     * <p> The <code>read(b,</code> <code>off,</code> <code>len)</code> method
     * for class <code>InputStream</code> simply calls the method
     * <code>read()</code> repeatedly. If the first such call results in an
     * <code>IOException</code>, that exception is returned from the call to
     * the <code>read(b,</code> <code>off,</code> <code>len)</code> method.  If
     * any subsequent call to <code>read()</code> results in a
     * <code>IOException</code>, the exception is caught and treated as if it
     * were end of file; the bytes read up to that point are stored into
     * <code>b</code> and the number of bytes read before the exception
     * occurred is returned. The default implementation of this method blocks
     * until the requested amount of input data <code>len</code> has been read,
     * end of file is detected, or an exception is thrown. Subclasses are
     * encouraged to provide a more efficient implementation of this method.
     *
     * @param      buff     the buffer into which the data is read.
     * @param      offset   the start offset in array <code>b</code>
     *                   at which the data is written.
     * @param      length   the maximum number of bytes to read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException If the first byte cannot be read for any reason
     * other than end of file, or if the input stream has been closed, or if
     * some other I/O error occurs.
     * @exception  NullPointerException If <code>b</code> is <code>null</code>.
     * @exception  IndexOutOfBoundsException If <code>off</code> is negative,
     * <code>len</code> is negative, or <code>len</code> is greater than
     * <code>b.length - off</code>
     * @see        java.io.InputStream#read()
     */
    suspend fun read(buff:ByteArray, offset:Int=0, length:Int=buff.size-offset):Int

    /**
     * Skips over and discards <code>n</code> bytes of data from this input
     * stream. The <code>skip</code> method may, for a variety of reasons, end
     * up skipping over some smaller number of bytes, possibly <code>0</code>.
     * This may result from any of a number of conditions; reaching end of file
     * before <code>n</code> bytes have been skipped is only one possibility.
     * The actual number of bytes skipped is returned. If {@code n} is
     * negative, the {@code skip} method for class {@code InputStream} always
     * returns 0, and no bytes are skipped. Subclasses may handle the negative
     * value differently.
     *
     * <p> The <code>skip</code> method implementation of this class creates a
     * byte array and then repeatedly reads into it until <code>n</code> bytes
     * have been read or the end of the stream has been reached. Subclasses are
     * encouraged to provide a more efficient implementation of this method.
     * For instance, the implementation may depend on the ability to seek.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @throws     IOException  if an I/O error occurs.
     */
    suspend fun skip(n:Long):Long
}
suspend fun ReadAbleStream.readFully(buf: ByteArray, off: Int=0, len: Int=buf.size-off) {
    buf.checkRange(off,len)
    var offset=off
    var length=len
    while (length>0) {
        val readLen = read(buf, offset, length)
        if (readLen > 0) {
            offset+=readLen
            length-=readLen
        }else if(readLen==0){
            continue
        } else {
            throw EOFException()
        }
    }
}

suspend inline fun ReadAbleStream.readWithEOFCheck():Int{
    val tmp = read()
    if (tmp<0)
        throw EOFException()
    else
        return tmp
}

interface WriteAbleStream: CloseAble,FlushAble{

    /**
     * Writes the specified byte to this output stream. The general
     * contract for <code>write</code> is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument <code>b</code>. The 24
     * high-order bits of <code>b</code> are ignored.
     * <p>
     * Subclasses of <code>OutputStream</code> must provide an
     * implementation for this method.
     *
     * @param      byte   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> may be thrown if the
     *             output stream has been closed.
     */
    suspend fun write(byte:Int)

    /**
     * Writes <code>len</code> bytes from array
     * <code>b</code>, in order,  to
     * the output stream.  If <code>b</code>
     * is <code>null</code>, a <code>NullPointerException</code>
     * is thrown.  If <code>off</code> is negative,
     * or <code>len</code> is negative, or <code>off+len</code>
     * is greater than the length of the array
     * <code>b</code>, then an <code>IndexOutOfBoundsException</code>
     * is thrown.  If <code>len</code> is zero,
     * then no bytes are written. Otherwise, the
     * byte <code>b[buff]</code> is written first,
     * then <code>b[off+1]</code>, and so on; the
     * last byte written is <code>b[off+len-1]</code>.
     *
     * @param      buff     the data.
     * @param      offset   the start offset in the data.
     * @param      length   the number of bytes to write.
     * @return     the total number of bytes write into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been send.
     * @throws     IOException  if an I/O error occurs.
     */
    suspend fun write(buff: ByteArray, offset: Int=0, length: Int=buff.size-offset):Int

    /**
     * Writes <code>len</code> bytes from array
     * <code>b</code>, in order,  to
     * the output stream.  If <code>b</code>
     * is <code>null</code>, a <code>NullPointerException</code>
     * is thrown.  If <code>off</code> is negative,
     * or <code>len</code> is negative, or <code>off+len</code>
     * is greater than the length of the array
     * <code>b</code>, then an <code>IndexOutOfBoundsException</code>
     * is thrown.  If <code>len</code> is zero,
     * then no bytes are written. Otherwise, the
     * byte <code>b[off]</code> is written first,
     * then <code>b[off+1]</code>, and so on; the
     * last byte written is <code>b[off+len-1]</code>.
     *
     * @param      byteArray     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @throws     IOException  if an I/O error occurs.
     */
    suspend fun writeFully(byteArray: ByteArray, off: Int=0, len: Int = byteArray.size-off)
}

suspend fun WriteAbleStream.writeBytes(byteArray: ByteArray, off: Int=0, len: Int = byteArray.size-off){
    byteArray.checkRange(off,len)
    var offset=off
    var length=len
    while (length>0) {
        val writeLen = write(byteArray, offset, length)
        if (writeLen>=0) {
            offset += writeLen
            length -= writeLen
        }else{
            throw IOException("Stream has be closed")
        }
    }
}

abstract class AbsReadAbleStream(val readAble: ReadAble):ReadAbleStream{
    val MAX_SKIP_BUFFER_SIZE = 2048

    override val isClosed: Boolean
        get() = closeStatus
    private var closeStatus=false
    private val oneByte: ByteBuffer =ByteBuffer.allocate(1)

    override suspend fun read(): Int {
        while (true) {
            checkClosed("Stream Has be Closed!")

            oneByte.clear()
            val len = readAble.read(oneByte)
            if (len == 1 ){
                oneByte.flip()
                return oneByte.get() .toInt() and 0xFF
            }else if (len == 0){
                continue
            }else{
                close()
                return -1
            }
        }
    }

    override suspend fun read(buff: ByteArray, offset: Int, length: Int): Int {
        try {
            checkClosed("Stream Has be Closed!")
            buff.checkRange(offset, length)
            val byteBuffer = ByteBuffer.wrap(buff, offset, length)
            return readAble.read(byteBuffer)
        }catch (t:Throwable){
            close()
            throw t
        }
    }

    override suspend fun skip(n: Long):Long {
        var remaining = n
        var nr: Int

        if (n <= 0) {
            return 0
        }

        val size = min(MAX_SKIP_BUFFER_SIZE.toLong(), remaining).toInt()
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

    override fun close() {
        whenNotClosed {
            closeStatus=false
            readAble.close()
        }
    }

    override val markSupported: Boolean = false

    override fun mark(len: Int) {
        throw IOException("mark unsupported")
    }

    override fun reset() {

    }

    override val available: Int
        get() = 0

    override suspend fun fill() {

    }
}

abstract class AbsWriteAbleStream( val writeAble: WriteAble):WriteAbleStream{
    override val isClosed: Boolean
        get() = closeStatus
    private var closeStatus: Boolean=false
    private val oneByte: ByteBuffer =ByteBuffer.allocate(1)

    override suspend fun write(byte: Int) {
        oneByte.clear()
        oneByte.put(byte.toByte())
        oneByte.flip()
        while (true) {
            checkClosed("Stream Has be Closed!")
            val len = writeAble.write(oneByte)
            if (len == 1 ){
                return
            }else if (len == 0){
                continue
            }else{
                close()
            }
        }
    }

    override suspend fun write(buff: ByteArray, offset: Int, length: Int): Int {
        checkClosed("Stream Closed!")

        buff.checkRange(offset, length)

        val byteBuffer=ByteBuffer.wrap(buff,offset, length)
        return writeAble.write(byteBuffer)
    }

    override suspend fun writeFully(byteArray: ByteArray, off: Int, len: Int) {
        writeBytes(byteArray,off,len)
    }

    override suspend fun flush() {

    }

    override fun close() {
        whenNotClosed {
            closeStatus=false
            writeAble.close()
        }
    }
}


fun ReadAble.buildReadAbleStream():ReadAbleStream{
    return object :AbsReadAbleStream(this){}
}

fun WriteAble.buildWriteAbleStream():WriteAbleStream{
    return object :AbsWriteAbleStream(this){}
}

fun ReadAbleStream.bufferedReadAbleStream(buffSize:Int=4096):BufferedReadAbleStream{
    return BufferedReadAbleStream(this,buffSize)
}

fun WriteAbleStream.bufferedWriteAbleStream(buffSize: Int=4096):BufferedWriteAbleStream{
    return BufferedWriteAbleStream(this,buffSize)
}