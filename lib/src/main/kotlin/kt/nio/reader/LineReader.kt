@file:Suppress("BlockingMethodInNonBlockingContext")

package kt.nio.reader

import kt.nio.CloseAble
import kt.nio.checkClosed
import kt.nio.stream.ReadAbleStream

import kt.nio.whenNotClosed
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset

class LineReader(val readAble: ReadAbleStream, val linetype: Type = Type.CR_LF,val charset: Charset= Charset.defaultCharset()):
    CloseAble {
    enum class Type{
        CR,
        LF,
        CR_LF
    }

    private var exit=false

    suspend fun readline(): String {
        checkClosed("Channel closed!")
        val tmpString = ByteArrayOutputStream()
        loop@while (!exit) {

            val one = readAble.read().toByte()
            if (linetype==Type.CR && one == '\r'.toByte()){
                return String(tmpString.toByteArray(),charset)
            }else if (linetype==Type.LF && one == '\n'.toByte()){
                return String(tmpString.toByteArray(),charset)
            }else if (linetype==Type.CR_LF && one == '\r'.toByte()){
                val two = readAble.read().toByte()
                if (two == '\n'.toByte()) {
                    return String(tmpString.toByteArray(), charset)
                } else {
                    tmpString.write(one.toInt())
                    tmpString.write(two.toInt())
                }
            }else if (one <= 4 .toByte()) {
                throw LineendException()
            } else {
                tmpString.write(one.toInt())
            }
        }
        throw IOException("reader has be closed!")
    }

    override val isClosed: Boolean
        get() = exit


    override fun close() {
        whenNotClosed {
            exit=true
            readAble.close()
        }

    }
    class LineendException:Exception("line end!")
}