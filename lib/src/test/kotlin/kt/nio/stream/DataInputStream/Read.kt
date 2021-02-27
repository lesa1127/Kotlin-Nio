package kt.nio.stream.DataInputStream

import kotlinx.coroutines.runBlocking
import kt.nio.stream.ByteArrayReadAbleStream
import kt.nio.stream.DataInputStream
import org.junit.Assert
import org.junit.Test

class Read {

    @Test
    fun readTest(){
        runBlocking {
            val byteArray = byteArrayOf(1,2,3,4,5,6,7,8)
            val bars = ByteArrayReadAbleStream(byteArray)
            val inputStream = DataInputStream(bars)

            Assert.assertEquals("available not was zero",inputStream.available,8)

            inputStream.mark(0)

            var one = inputStream.read()
            Assert.assertEquals("byte value not was expected",one,1)

            one = inputStream.read()
            Assert.assertEquals("byte value not was expected",one,2)

            one = inputStream.read()
            Assert.assertEquals("byte value not was expected",one,3)

            one = inputStream.read()
            Assert.assertEquals("byte value not was expected",one,4)

            one = inputStream.read()
            Assert.assertEquals("byte value not was expected",one,5)

            one = inputStream.read()
            Assert.assertEquals("byte value not was expected",one,6)

            one = inputStream.read()
            Assert.assertEquals("byte value not was expected",one,7)

            one = inputStream.read()
            Assert.assertEquals("byte value not was expected",one,8)

            inputStream.reset()

            var oneb = inputStream.readByte()
            Assert.assertEquals("byte value not was expected",oneb,1 .toByte())

            oneb = inputStream.readByte()
            Assert.assertEquals("byte value not was expected",oneb,2 .toByte())

            oneb = inputStream.readByte()
            Assert.assertEquals("byte value not was expected",oneb,3 .toByte())

            oneb = inputStream.readByte()
            Assert.assertEquals("byte value not was expected",oneb,4 .toByte())

            oneb = inputStream.readByte()
            Assert.assertEquals("byte value not was expected",oneb,5 .toByte())

            oneb = inputStream.readByte()
            Assert.assertEquals("byte value not was expected",oneb,6 .toByte())

            oneb = inputStream.readByte()
            Assert.assertEquals("byte value not was expected",oneb,7 .toByte())

            oneb = inputStream.readByte()
            Assert.assertEquals("byte value not was expected",oneb,8 .toByte())

            inputStream.reset()

            val short = inputStream.readShort()
            Assert.assertEquals("available not was 6",inputStream.available,6)
            Assert.assertEquals("value not was expected",short,258 .toShort())


            val ushort = inputStream.readUnsignedShort()
            Assert.assertEquals("available not was 4",inputStream.available,4)
            Assert.assertEquals("value not was expected",ushort,772 )


            val int = inputStream.readInt()
            Assert.assertEquals("available not was 0",inputStream.available,0)
            Assert.assertEquals("value not was expected ",int,84281096)

            inputStream.reset()
            val long = inputStream.readLong()
            Assert.assertEquals("available not was 0",inputStream.available,0)
            Assert.assertEquals("value not was expected ",long,72623859790382856L)

            inputStream.reset()
            val boolean = inputStream.readBoolean()
            Assert.assertEquals("available not was 7",inputStream.available,7)
            Assert.assertEquals("value not was expected ",boolean,true)

            inputStream.reset()
            inputStream.readFully(ByteArray(8))
            Assert.assertEquals("available not was 0",inputStream.available,0)

            inputStream.reset()
            inputStream.skipBytes(8)
            Assert.assertEquals("available not was 0",inputStream.available,0)

        }
    }

    @Test
    fun readUTF(){
        runBlocking {
            val txt ="KHioUHo8a0239s-dfw4nj0i8u80NLKNovb0q34--1389u9238i23kaiudfg8yHiU(dq23ou90"
            val tmp = txt.toByteArray(Charsets.UTF_8)
            val buff = ByteArray(tmp.size+2)
            buff[0] = (tmp.size ushr 8 and  0xFF ).toByte()
            buff[1] = (tmp.size ushr 0 and  0xFF ).toByte()
            System.arraycopy(tmp,0,buff,2,tmp.size)

            val bars = ByteArrayReadAbleStream(buff)
            val inputStream = DataInputStream(bars)

            val str = inputStream.readUTF()

            Assert.assertEquals("value not was expected ",str,txt)
            Assert.assertEquals("available not was 0",inputStream.available,0)

        }
    }
    @Test
    fun readFloat(){
        runBlocking {
            val f=3.14159F
            val intvalue = f.toBits()

            val buff = ByteArray(4)
            buff[0] = (intvalue ushr 24 and  0xFF ).toByte()
            buff[1] = (intvalue ushr 16 and  0xFF ).toByte()
            buff[2] = (intvalue ushr 8 and  0xFF ).toByte()
            buff[3] = (intvalue ushr 0 and  0xFF ).toByte()

            val bars = ByteArrayReadAbleStream(buff)
            val inputStream = DataInputStream(bars)

            val float=inputStream.readFloat()

            Assert.assertEquals("value not was expected ",f,float)
            Assert.assertEquals("available not was 0",inputStream.available,0)

        }
    }

    @Test
    fun readDouble(){
        runBlocking {
            val d:Double=3.1415926
            val intvalue = d.toBits()

            val buff = ByteArray(8)
            buff[0] = (intvalue ushr 56 and  0xFF ).toByte()
            buff[1] = (intvalue ushr 48 and  0xFF ).toByte()
            buff[2] = (intvalue ushr 40 and  0xFF ).toByte()
            buff[3] = (intvalue ushr 32 and  0xFF ).toByte()

            buff[4] = (intvalue ushr 24 and  0xFF ).toByte()
            buff[5] = (intvalue ushr 16 and  0xFF ).toByte()
            buff[6] = (intvalue ushr 8 and  0xFF ).toByte()
            buff[7] = (intvalue ushr 0 and  0xFF ).toByte()

            val bars = ByteArrayReadAbleStream(buff)
            val inputStream = DataInputStream(bars)

            val double=inputStream.readDouble()

            Assert.assertEquals("value not was expected ",d,double,0.0)
            Assert.assertEquals("available not was 0",inputStream.available,0)

        }
    }
}