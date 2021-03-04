package kt.nio.stream

open class FilterWriteAbleStream(
    protected open val writeAbleStream: WriteAbleStream):WriteAbleStream {
    override suspend fun write(byte: Int) {
        writeAbleStream.write(byte)
    }

    override suspend fun write(buff: ByteArray, offset: Int, length: Int) {
        return writeAbleStream.write(buff, offset, length)
    }

    override val isClosed: Boolean
        get() = writeAbleStream.isClosed

    override fun close() {
        writeAbleStream.close()
    }

    override suspend fun flush() {
        writeAbleStream.flush()
    }


}