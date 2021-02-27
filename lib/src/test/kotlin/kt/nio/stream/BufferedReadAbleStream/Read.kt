package kt.nio.stream.BufferedReadAbleStream

import kotlinx.coroutines.runBlocking
import kt.nio.stream.BufferedReadAbleStream
import kt.nio.stream.ByteArrayReadAbleStream
import org.junit.Assert
import org.junit.Test

class Read {

    @Test
    fun testRead(){
        runBlocking {
            val byteArray = byteArrayOf(1,2,3,4,5,6,7,8)
            val byteArrayReadAbleStream = ByteArrayReadAbleStream(byteArray)
            val bufferedReadAbleStream = BufferedReadAbleStream(byteArrayReadAbleStream)

            Assert.assertEquals("available not was zero",bufferedReadAbleStream.available,0)

            var one = bufferedReadAbleStream.read()
            Assert.assertEquals("byte value not was expected",one,1)

            one = bufferedReadAbleStream.read()
            Assert.assertEquals("byte value not was expected",one,2)

            one = bufferedReadAbleStream.read()
            Assert.assertEquals("byte value not was expected",one,3)

            one = bufferedReadAbleStream.read()
            Assert.assertEquals("byte value not was expected",one,4)

            one = bufferedReadAbleStream.read()
            Assert.assertEquals("byte value not was expected",one,5)

            one = bufferedReadAbleStream.read()
            Assert.assertEquals("byte value not was expected",one,6)

            one = bufferedReadAbleStream.read()
            Assert.assertEquals("byte value not was expected",one,7)

            one = bufferedReadAbleStream.read()
            Assert.assertEquals("byte value not was expected",one,8)

            Assert.assertEquals("available not was zero,after read",bufferedReadAbleStream.available,0)

            one = bufferedReadAbleStream.read()
            Assert.assertEquals("byte value not was expected",one,-1)

            Assert.assertEquals("stream should be closed",bufferedReadAbleStream.isClosed,true)


        }
    }

    @Test
    fun testMark(){
        runBlocking {
            val byteArray = byteArrayOf(1,2,3,4,5,6,7,8)
            val byteArrayReadAbleStream = ByteArrayReadAbleStream(byteArray)
            val bufferedReadAbleStream = BufferedReadAbleStream(byteArrayReadAbleStream)

            Assert.assertEquals("available value not was zero",bufferedReadAbleStream.available,0)

            bufferedReadAbleStream.fill()

            Assert.assertEquals("available value not was zero expected",bufferedReadAbleStream.available,8)

            bufferedReadAbleStream.mark(0)

            for (i in 0 until  bufferedReadAbleStream.available){
                bufferedReadAbleStream.read()
            }
            bufferedReadAbleStream.reset()

            Assert.assertEquals("available value not was zero expected",bufferedReadAbleStream.available,8)

            for (i in 0 until  bufferedReadAbleStream.available){
                bufferedReadAbleStream.read()
            }

            Assert.assertEquals("available value not was zero",bufferedReadAbleStream.available,0)

        }
    }
}