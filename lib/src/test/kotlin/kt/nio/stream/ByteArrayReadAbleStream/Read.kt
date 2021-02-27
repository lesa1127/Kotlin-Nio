package kt.nio.stream.ByteArrayReadAbleStream

import kotlinx.coroutines.runBlocking
import kt.nio.stream.ByteArrayReadAbleStream
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class Read {
    @Test
    fun doBoundsTest(){
        runBlocking {
            val byteArray =ByteArray(8)

            try {
                ByteArrayReadAbleStream(byteArray,0, Int.MAX_VALUE)
            } catch (e: IndexOutOfBoundsException) {
                println("IndexOutOfBoundsException is thrown: OKAY")
            } catch (e: NullPointerException) {
                println("NullPointerException is thrown: OKAY")
            } catch (e: Throwable) {
                throw RuntimeException("Unexpected Exception is thrown", e)
            }

            try {
                ByteArrayReadAbleStream(byteArray,0, byteArray.size+100)
            } catch (e: IndexOutOfBoundsException) {
                println("IndexOutOfBoundsException is thrown: OKAY")
            } catch (e: NullPointerException) {
                println("NullPointerException is thrown: OKAY")
            } catch (e: Throwable) {
                throw RuntimeException("Unexpected Exception is thrown", e)
            }

            try {
                ByteArrayReadAbleStream(byteArray,-1, 2)
            } catch (e: IndexOutOfBoundsException) {
                println("IndexOutOfBoundsException is thrown: OKAY")
            } catch (e: NullPointerException) {
                println("NullPointerException is thrown: OKAY")
            } catch (e: Throwable) {
                throw RuntimeException("Unexpected Exception is thrown", e)
            }

            try {
                ByteArrayReadAbleStream(byteArray,0, -1)
            } catch (e: IndexOutOfBoundsException) {
                println("IndexOutOfBoundsException is thrown: OKAY")
            } catch (e: NullPointerException) {
                println("NullPointerException is thrown: OKAY")
            } catch (e: Throwable) {
                throw RuntimeException("Unexpected Exception is thrown", e)
            }
            try {
                val byteArrayReadAbleStream = ByteArrayReadAbleStream(byteArray)
                for (i in byteArray.indices){
                    byteArrayReadAbleStream.read()
                }
                assertEquals("return not was -1",byteArrayReadAbleStream.read(),-1)
                byteArrayReadAbleStream.read()
                assert(false){
                    "oom"
                }
            }catch (e:IOException){
                println("IOException is thrown: OKAY")
            }

        }
    }

    @Test
    fun readTest(){
        runBlocking {

            val byteArray =ByteArray(8)
            byteArray[0]=1
            byteArray[1]=2
            byteArray[2]=3
            byteArray[3]=4
            val bars = ByteArrayReadAbleStream(byteArray)
            assertEquals("size check fail",byteArray.size,bars.available)

            for (i in byteArray.indices){
                assertEquals("byte read error",bars.read().toByte() ,byteArray[i])
            }

            assertEquals("the number of read available not was zero",bars.available,0)

            bars.reset()
            assertEquals("the number of read available not was zero",bars.available,byteArray.size)
            val tmp =ByteArray(byteArray.size)
            bars.read(tmp)
            for (i in byteArray.indices){
                assertEquals("byte read error",tmp[i] ,byteArray[i])
            }

            bars.reset()
            bars.skip(byteArray.size.toLong())
            assertEquals("the number of read available not was zero",bars.available,0)

            bars.reset()
            bars.read()
            bars.mark(0)
            bars.read()
            bars.reset()

            assertEquals("the number of read available not was one",bars.available,byteArray.size-1)

        }

    }


}