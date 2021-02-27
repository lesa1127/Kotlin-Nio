package kt.nio.stream.ByteArrayWriteAbleStream

import kotlinx.coroutines.runBlocking
import kt.nio.stream.ByteArrayWriteAbleStream


fun main()= runBlocking {
    val maxHeap = Runtime.getRuntime().maxMemory()
    if (maxHeap < 3L * Int.MAX_VALUE) {
        System.out.printf(
            "Skipping test; max memory %sM too small%n",
            maxHeap / (1024 * 1024)
        )
        return@runBlocking
    }
    val baos = ByteArrayWriteAbleStream()
    var n: Long = 0
    while (true) {
        try {
            baos.write('x' .toInt())
        } catch (t: Throwable) {
            // check data integrity while we're here
            val bytes = baos.toByteArray()
            if (bytes.size.toLong() != n) throw AssertionError("wrong length")
            if (bytes[0] != 'x'.toByte() ||
                bytes[bytes.size - 1] != 'x'.toByte()
            ) throw AssertionError("wrong contents")
            val gap = Int.MAX_VALUE - n
            System.out.printf("gap=%dM %d%n", gap / (1024 * 1024), gap)
            if (gap > 1024) throw t
            // t.printStackTrace();
            break
        }
        n++
    }
}