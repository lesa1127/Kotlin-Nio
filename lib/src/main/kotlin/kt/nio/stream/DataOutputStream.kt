package kt.nio.stream

import kt.nio.util.checkRange
import java.io.UTFDataFormatException

open class DataOutputStream(writeAbleStream: WriteAbleStream) :FilterWriteAbleStream(writeAbleStream),DataOutput {

    override suspend fun write(byte: Int) {
        super.write(byte)
    }

    override suspend fun write(b: ByteArray) {
        write(b,0,b.size)
    }

    override suspend fun write(b: ByteArray, off: Int, len: Int) {
        
        b.checkRange(off,len)
        super.write(b,off,len)
    }

    override suspend fun writeBoolean(v: Boolean) {
        write(if (v) 1 else 0)
    }

    override suspend fun writeByte(v: Int) {
        write(v)
    }

    override suspend fun writeShort(s: Int) {
        val v = s
        write(v ushr 8 and 0xFF)
        write(v ushr 0 and 0xFF)
    }
    

    override suspend fun writeChar(c: Int) {
        val v= c
        write(v ushr 8 and 0xFF)
        write(v ushr 0 and 0xFF)
    }

    override suspend fun writeInt(v: Int) {
        write(v ushr 24 and 0xFF)
        write(v ushr 16 and 0xFF)
        write(v ushr 8 and 0xFF)
        write(v ushr 0 and 0xFF)
    }

    private val writeBuffer = ByteArray(8)

    override suspend fun writeLong(v: Long) {
        writeBuffer[0] = (v ushr 56).toByte()
        writeBuffer[1] = (v ushr 48).toByte()
        writeBuffer[2] = (v ushr 40).toByte()
        writeBuffer[3] = (v ushr 32).toByte()
        writeBuffer[4] = (v ushr 24).toByte()
        writeBuffer[5] = (v ushr 16).toByte()
        writeBuffer[6] = (v ushr 8).toByte()
        writeBuffer[7] = (v ushr 0).toByte()
        write(writeBuffer, 0, 8)
    }

    override suspend fun writeFloat(v: Float) {
        writeInt(java.lang.Float.floatToIntBits(v))
    }

    override suspend fun writeDouble(v: Double) {
        writeLong(java.lang.Double.doubleToLongBits(v))
    }

    override suspend fun writeBytes(s: String) {
        val len = s.length
        for (i in 0 until len) {
            write(s[i].toInt())
        }
    }

    override suspend fun writeChars(s: String) {
        val len = s.length
        for (i in 0 until len) {
            val v = s[i].toInt()
            write(v ushr 8 and 0xFF)
            write(v ushr 0 and 0xFF)
        }
    }

    override suspend fun writeUTF(str: String) {
        val strlen: Int = str.length
        var utflen = 0
        var c: Int
        var count = 0


        /* use charAt instead of copying String to char array */
        for (i in 0 until strlen) {
            c = str[i].toInt()
            if (c >= 0x0001 && c <= 0x007F) {
                utflen++
            } else if (c > 0x07FF) {
                utflen += 3
            } else {
                utflen += 2
            }
        }

        if (utflen > 65535) throw UTFDataFormatException(
            "encoded string too long: $utflen bytes"
        )

        val bytearr = ByteArray(utflen + 2)

        bytearr[count++] = (utflen ushr 8 and 0xFF).toByte()
        bytearr[count++] = (utflen ushr 0 and 0xFF).toByte()

        var i = 0
        while (i < strlen) {
            c = str.get(i).toInt()
            if (!(c >= 0x0001 && c <= 0x007F))
                break
            bytearr[count++] = c.toByte()
            i++
        }

        while (i < strlen) {
            c = str.get(i).toInt()
            if (c >= 0x0001 && c <= 0x007F) {
                bytearr[count++] = c.toByte()
            } else if (c > 0x07FF) {
                bytearr[count++] = (0xE0 or (c shr 12 and 0x0F)).toByte()
                bytearr[count++] = (0x80 or (c shr 6 and 0x3F)).toByte()
                bytearr[count++] = (0x80 or (c shr 0 and 0x3F)).toByte()
            } else {
                bytearr[count++] = (0xC0 or (c shr 6 and 0x1F)).toByte()
                bytearr[count++] = (0x80 or (c shr 0 and 0x3F)).toByte()
            }
            i++
        }
        write(bytearr, 0, utflen + 2)
    }
}