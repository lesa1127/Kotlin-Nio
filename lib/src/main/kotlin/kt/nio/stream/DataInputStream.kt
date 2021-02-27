package kt.nio.stream

import java.io.EOFException
import java.io.UTFDataFormatException
import java.nio.ByteBuffer
import kotlin.math.min

open class DataInputStream(readAbleStream: ReadAbleStream) : FilterReadAbleStream(readAbleStream),DataInput{
    private val MAX_SKIP_BUFFER_SIZE=2048

    override suspend fun readFully(b: ByteArray) {
        return readFully(b,0,b.size)
    }

    override suspend fun readFully(b: ByteArray, off: Int, len: Int) {
        val buff = ByteBuffer.wrap(b,off,len)
        while (buff.hasRemaining()) {
            val readLen = super.read(b, buff.position(), buff.remaining())
            if (readLen > 0) {
                buff.position(buff.position() + readLen)
            }else if(readLen==0){
                continue
            } else {
                throw EOFException()
            }
        }
    }

    override suspend fun skipBytes(n: Int): Int {
        var remaining = n
        var nr: Int

        if (n <= 0) {
            return 0
        }

        val size = min(MAX_SKIP_BUFFER_SIZE, remaining)
        val skipBuffer = ByteArray(size)
        while (remaining > 0) {
            nr = read(skipBuffer, 0, min(size, remaining))
            if (nr < 0) {
                break
            }
            remaining -= nr
        }

        return n - remaining
    }

    override suspend fun readBoolean(): Boolean {
        val num=readUnsignedByte()
        return num != 0
    }

    override suspend fun readByte(): Byte {
        return readUnsignedByte().toByte()
    }

    override suspend fun readUnsignedByte(): Int {
        return readWithEOFCheck()
    }

    override suspend fun readShort(): Short {
        return readUnsignedShort().toShort()
    }

    override suspend fun readUnsignedShort(): Int {
        val num1=readUnsignedByte()
        val num2=readUnsignedByte()
        return ((num1 shl 8) + (num2 shl 0))
    }

    override suspend fun readChar(): Char {
        return readShort().toChar()
    }


    override suspend fun readInt(): Int {
        val ch1: Int = readUnsignedByte()
        val ch2: Int = readUnsignedByte()
        val ch3: Int = readUnsignedByte()
        val ch4: Int = readUnsignedByte()
        return (ch1 shl 24) + (ch2 shl 16) + (ch3 shl 8) + (ch4 shl 0)
    }


    private val readBuffer = ByteArray(8)

    override suspend fun readLong(): Long {
        readFully(readBuffer, 0, 8)
        return (readBuffer[0].toLong() shl 56) +
                ((readBuffer[1].toLong() and 255) shl 48) +
                ((readBuffer[2].toLong() and 255) shl 40) +
                ((readBuffer[3].toLong() and 255) shl 32) +
                ((readBuffer[4].toLong() and 255) shl 24) +
                (readBuffer[5].toLong() and 255 shl 16) +
                (readBuffer[6].toLong() and 255 shl 8) +
                (readBuffer[7].toLong() and 255 shl 0)
    }

    override suspend fun readFloat(): Float {
        return java.lang.Float.intBitsToFloat(readInt())
    }

    override suspend fun readDouble(): Double {
        return java.lang.Double.longBitsToDouble(readLong())
    }

    override suspend fun readUTF(): String {
        val utflen: Int = readUnsignedShort()

        val bytearr = ByteArray(utflen)
        val chararr = CharArray(utflen)

        var c: Int
        var char2: Int
        var char3: Int
        var count = 0
        var chararr_count = 0

        readFully(bytearr, 0, utflen)

        while (count < utflen) {
            c = bytearr[count].toInt() and 0xff
            if (c > 127) break
            count++
            chararr[chararr_count++] = c.toChar()
        }

        while (count < utflen) {
            c = bytearr[count].toInt() and 0xff
            when (c shr 4) {
                0, 1, 2, 3, 4, 5, 6, 7 -> {
                    /* 0xxxxxxx*/count++
                    chararr[chararr_count++] = c.toChar()
                }
                12, 13 -> {
                    /* 110x xxxx   10xx xxxx*/count += 2
                    if (count > utflen) throw UTFDataFormatException(
                        "malformed input: partial character at end"
                    )
                    char2 = bytearr[count - 1].toInt()
                    if (char2 and 0xC0 != 0x80) throw UTFDataFormatException(
                        "malformed input around byte $count"
                    )
                    chararr[chararr_count++] = (c and 0x1F shl 6 or
                            (char2 and 0x3F)).toChar()
                }
                14 -> {
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */count += 3
                    if (count > utflen) throw UTFDataFormatException(
                        "malformed input: partial character at end"
                    )
                    char2 = bytearr[count - 2].toInt()
                    char3 = bytearr[count - 1].toInt()
                    if (char2 and 0xC0 != 0x80 || char3 and 0xC0 != 0x80) throw UTFDataFormatException(
                        "malformed input around byte " + (count - 1)
                    )
                    chararr[chararr_count++] = (c and 0x0F shl 12 or
                            (char2 and 0x3F shl 6) or
                            (char3 and 0x3F shl 0)).toChar()
                }
                else -> throw UTFDataFormatException(
                    "malformed input around byte $count"
                )
            }
        }
        // The number of chars produced may be less than utflen
        return String(chararr, 0, chararr_count)
    }
}