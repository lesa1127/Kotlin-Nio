package kt.nio.stream.DataOutputStream

import kotlinx.coroutines.runBlocking
import kt.nio.stream.ByteArrayWriteAbleStream
import kt.nio.stream.DataOutputStream
import kt.nio.stream.writeBytes
import org.junit.Assert
import org.junit.Test

class Write {

    @Test
    fun testWrite(){
        runBlocking {
            val byteArrayWriteAbleStream= ByteArrayWriteAbleStream()
            val dataOutputStream= DataOutputStream(byteArrayWriteAbleStream)

            dataOutputStream.writeBoolean(false)
            Assert.assertEquals(byteArrayWriteAbleStream.toByteArray()[0],0 .toByte())

            byteArrayWriteAbleStream.reset()
            dataOutputStream.writeByte(5)
            Assert.assertEquals(byteArrayWriteAbleStream.toByteArray()[0],5 .toByte())

            byteArrayWriteAbleStream.reset()
            dataOutputStream.writeChar(5 .toChar())
            Assert.assertEquals(byteArrayWriteAbleStream.toByteArray()[1],5 .toByte())

            byteArrayWriteAbleStream.reset()
            dataOutputStream.writeShort(0xFF .toShort())
            Assert.assertEquals(byteArrayWriteAbleStream.toByteArray()[1],0xFF .toByte())

            byteArrayWriteAbleStream.reset()
            dataOutputStream.writeInt(0xFF )
            Assert.assertEquals(byteArrayWriteAbleStream.toByteArray()[3],0xFF .toByte())

            byteArrayWriteAbleStream.reset()
            dataOutputStream.writeLong(0xFF )
            Assert.assertEquals(byteArrayWriteAbleStream.toByteArray()[7],0xFF .toByte())

            byteArrayWriteAbleStream.reset()
            val double= 3.1415926
            dataOutputStream.writeDouble( double)
            val l= double.toBits()
            val arr=byteArrayWriteAbleStream.toByteArray()
            for (i in 0 until 8) {
                Assert.assertEquals(arr[i],(l ushr (64 - ((i+1)*8))).toByte())
            }


            byteArrayWriteAbleStream.reset()
            val float= 3.1415926F
            dataOutputStream.writeFloat(float)
            val int= float.toBits()
            val arrf=byteArrayWriteAbleStream.toByteArray()
            for (i in 0 until 4) {
                Assert.assertEquals(arrf[i],(int ushr (32 - ((i+1)*8))).toByte())
            }
        }
    }

    @Test
    fun testString(){
        runBlocking {
            val txt  = "adjfalsdjfoadojowp"
            val byteArrayWriteAbleStream= ByteArrayWriteAbleStream()
            val dataOutputStream= DataOutputStream(byteArrayWriteAbleStream)
            dataOutputStream.writeBytes(txt)
            val tmps = String(byteArrayWriteAbleStream.toByteArray())
            Assert.assertEquals(tmps,txt)
        }
    }

    @Test
    fun testUtf(){
        runBlocking {
            val txt  = "adjfaaldlsdjfoadojowp"
            val byteArrayWriteAbleStream= ByteArrayWriteAbleStream()
            val dataOutputStream= DataOutputStream(byteArrayWriteAbleStream)
            dataOutputStream.writeUTF(txt)
            val tmpb=byteArrayWriteAbleStream.toByteArray()
            val intlen= ((tmpb[0].toInt() and 0xFF ) shr 8)+(tmpb[1].toInt() and 0xFF)
            val tmps = String(tmpb,2,intlen)
            Assert.assertEquals(tmps,txt)
        }
    }

}