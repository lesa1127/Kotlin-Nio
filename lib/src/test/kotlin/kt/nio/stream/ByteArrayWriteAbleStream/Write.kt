package kt.nio.stream.ByteArrayWriteAbleStream

import kotlinx.coroutines.runBlocking
import kt.nio.stream.ByteArrayWriteAbleStream
import org.junit.Test
import java.util.Arrays
import kotlin.Exception
import kotlin.math.min
import kotlin.random.Random

class Write {

    @Throws(Exception::class)
    private suspend fun doBoundsTest(
        b: ByteArray?, off: Int, len: Int,
        baos: ByteArrayWriteAbleStream
    ) {
        if (b != null) {
            println(
                "ByteArrayWriteAbleStream.write: b.length = " +
                        b.size + " off = " + off + " len = " + len
            )
        } else {
            println(
                ("ByteArrayWriteAbleStream.write: b is null off = " +
                        off + " len = " + len)
            )
        }
        try {
            baos.write(b!!, off, len)
        } catch (e: IndexOutOfBoundsException) {
            println("IndexOutOfBoundsException is thrown: OKAY")
        } catch (e: NullPointerException) {
            println("NullPointerException is thrown: OKAY")
        } catch (e: Throwable) {
            throw RuntimeException("Unexpected Exception is thrown", e)
        }
        b?.also {
            println(
                "ByteArrayWriteAbleStream.writeBytes: b.length = " +
                        it.size
            )
        }
        try {
            baos.write(b!!)
        } catch (e: NullPointerException) {
            println("NullPointerException is thrown: OKAY")
        } catch (e: Throwable) {
            throw RuntimeException("Unexpected Exception is thrown", e)
        }
    }

    @Test
    fun boundsTest() {
        runBlocking {
            val array1 = byteArrayOf(1, 2, 3, 4, 5) // Simple array

            //Create new ByteArrayWriteAbleStream object
            val y1 = ByteArrayWriteAbleStream(5)
            doBoundsTest(array1, 0, Int.MAX_VALUE, y1)
            doBoundsTest(array1, 0, array1.size + 100, y1)
            doBoundsTest(array1, -1, 2, y1)
            doBoundsTest(array1, 0, -1, y1)
            doBoundsTest(null, 0, 2, y1)
        }
    }

    @Test
    fun writeTest() {
        runBlocking {
            val baos = ByteArrayWriteAbleStream()
            val rnd: Random = Random.Default
            val size: Int = 17 + rnd.nextInt(128)
            val b = ByteArray(size)
            rnd.nextBytes(b)
            val off1: Int = rnd.nextInt(size / 4) + 1
            val len1 = min(rnd.nextInt(size / 4) + 1, size - off1)
            val off2: Int = rnd.nextInt(size / 2) + 1
            val len2 = min(rnd.nextInt(size / 2) + 1, size - off2)
            System.out.format(
                "size: %d, off1: %d, len1: %d, off2: %d, len2: %d%n",
                size, off1, len1, off2, len2
            )
            baos.write(b, off1, len1)
            val b1 = baos.toByteArray()
            assertEquals("Array length test 1 failed.",b1.size, len1)
            assertEquals(
                "Array equality test 1 failed.",
                b1, b.copyOfRange(off1, off1 + len1)
            )
            baos.write(b, off2, len2)
            val b2 = baos.toByteArray()
            assertEquals("Array length test 2 failed.",b2.size, len1 + len2)
            assertEquals(
                "Array equality test 2A failed.",
                Arrays.copyOfRange(b2, 0, len1),
                Arrays.copyOfRange(b, off1, off1 + len1)
            )
            assertEquals(
                "Array equality test 2B failed.",
                Arrays.copyOfRange(b2, len1, len1 + len2),
                Arrays.copyOfRange(b, off2, off2 + len2)
            )
            baos.write(b)
            val b3 = baos.toByteArray()
            val len3 = len1 + len2 + b.size
            if (b3.size != len1 + len2 + b.size) {
                throw RuntimeException("Array length test 3 failed.")
            }
            assertEquals( "Array length test 3 failed.",b3.size, len3)
            assertEquals(
                "Array equality test 3A failed.",
                Arrays.copyOfRange(b3, 0, len1),
                Arrays.copyOfRange(b, off1, off1 + len1)
            )
            assertEquals(
                "Array equality test 3B failed.",
                Arrays.copyOfRange(b3, len1, len1 + len2),
                Arrays.copyOfRange(b, off2, off2 + len2)
            )
            assertEquals(
                "Array equality test 3C failed.",
                Arrays.copyOfRange(b3, len1 + len2, len3), b
            )
        }
    }

    fun assertEquals(message:String,expected:Any,
                      actual:Any){
        if (expected is ByteArray && actual is ByteArray){
            if (expected.size==actual.size){
                for (i in 0 until expected.size){
                    org.junit.Assert.assertEquals(message,expected[i],actual[i])
                }
            }else{
                throw Exception(message)
            }
        }else{
            org.junit.Assert.assertEquals(message,expected,actual)
        }
    }

}