package kt.nio.stream


interface FlushAble {
    /**
     * Flushes this stream by writing any buffered output to the underlying
     * stream.
     *
     * @throws IOException If an I/O error occurs
     */
    suspend fun flush()
}