package kt.nio.stream.BufferedWriteAbleStream

import kotlinx.coroutines.runBlocking
import kt.nio.stream.BufferedWriteAbleStream
import kt.nio.stream.ByteArrayWriteAbleStream
import kt.nio.stream.writeBytes
import org.junit.Assert.assertEquals
import org.junit.Test

class Write {

    @Test
    fun testWrite(){
        runBlocking {
            val baws= ByteArrayWriteAbleStream()
            val bufferedWriteAbleStream = BufferedWriteAbleStream(baws)
            bufferedWriteAbleStream.write(1)
            bufferedWriteAbleStream.write(2)
            bufferedWriteAbleStream.write(3)
            bufferedWriteAbleStream.flush()

            var tmp =baws.toByteArray()
            assertEquals("num error",tmp[0].toInt(),1)
            assertEquals("num error",tmp[1].toInt(),2)
            assertEquals("num error",tmp[2].toInt(),3)

            baws.reset()
            bufferedWriteAbleStream.writeBytes(ByteArray(8))
            bufferedWriteAbleStream.flush()
            tmp =baws.toByteArray()
            assertEquals("length check error",tmp.size,8)

        }
    }

    @Test
    fun closeTest(){
        runBlocking {
            val baws = ByteArrayWriteAbleStream()
            val bufferedWriteAbleStream = BufferedWriteAbleStream(baws)
            bufferedWriteAbleStream.close()
            assertEquals("length check error",bufferedWriteAbleStream.isClosed,baws.isClosed)
        }
    }
}