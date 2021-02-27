package kt.nio.stream

interface FillAble {
    val available:Int

    /**
     * Fills the buffer with more data
     */
    suspend fun fill()
}