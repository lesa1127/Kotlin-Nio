package kt.nio.stream

open class FilterReadAbleStream(
    protected open val readAbleStream: ReadAbleStream
) :ReadAbleStream {

    override suspend fun read(): Int {
        return readAbleStream.read()
    }

    override suspend fun read(buff: ByteArray, offset: Int, length: Int): Int {
        return readAbleStream.read(buff, offset, length)
    }

    override suspend fun skip(n: Long): Long {
        return readAbleStream.skip(n)
    }

    override val isClosed: Boolean
        get() = readAbleStream.isClosed

    override fun close() {
        readAbleStream.close()
    }

    override val markSupported: Boolean
        get() = readAbleStream.markSupported

    override fun mark(len: Int) {
        readAbleStream.mark(len)
    }

    override fun reset() {
        readAbleStream.reset()
    }

    override val available: Int
        get() = readAbleStream.available

    override suspend fun fill() {
        readAbleStream.fill()
    }
}