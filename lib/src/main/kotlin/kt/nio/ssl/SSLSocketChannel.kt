@file:Suppress("BlockingMethodInNonBlockingContext")

package kt.nio.ssl

import kotlinx.coroutines.runBlocking
import kt.nio.ReadWriteAble
import kt.nio.checkClosed
import kt.nio.util.enlargeBuffer
import kt.nio.util.putByteBuffer
import kt.nio.whenNotClosed
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.net.ssl.*
import javax.net.ssl.SSLEngineResult.HandshakeStatus

open class SSLSocketChannel(
    sslContext: SSLContext,
    engineType: Boolean,
    val hostName: String, val port: Int,
    private val channl: ReadWriteAble
) : ReadWriteAble {

    constructor(
        sslContext: SSLContext,
        channl: ReadWriteAble
    ) : this(sslContext, false, "", 0, channl)

    constructor(
        sslContext: SSLContext,
        hostname: String, port: Int,
        channl: ReadWriteAble
    ) : this(sslContext, true, hostname, port, channl)

    protected var active = false
    protected var closeStatus = false
    override val isClosed: Boolean
        get() = closeStatus

    val sslEngine: SSLEngine = if (engineType) {
        sslContext.createSSLEngine(hostName, port)
    } else {
        sslContext.createSSLEngine()
    }

    var useClientMode: Boolean
        get() {
            return sslEngine.useClientMode
        }
        set(value) {
            sslEngine.useClientMode = value
        }

    val session: SSLSession
        get() {
            return sslEngine.session!!
        }


    //被加密的写
    private var writeNetData: ByteBuffer = ByteBuffer.allocate(session.packetBufferSize)

    private var readAppData: ByteBuffer = ByteBuffer.allocate(session.applicationBufferSize)

    //被加密的读
    private var readNetData: ByteBuffer = ByteBuffer.allocate(session.packetBufferSize)


    suspend fun beginHandshake() {
        checkClosed("ClosedChannelException")
        sslEngine.beginHandshake()
        if (doHandshake(channl, sslEngine)) {
            active = true
        } else {
            closeStatus = true
            channl.close()
            throw HandShakeException()
        }
    }

    private suspend fun doHandshake(socketChannel: ReadWriteAble, engine: SSLEngine): Boolean {
        var result: SSLEngineResult
        var handshakeStatus: HandshakeStatus

        val appBufferSize = engine.session.applicationBufferSize
        val myAppData = ByteBuffer.allocate(appBufferSize)
        var peerAppData = ByteBuffer.allocate(appBufferSize)

        writeNetData.clear()
        writeNetData.flip()
        readNetData.clear()
        readNetData.flip()


        var needReadHandshake = false
        handshakeStatus = engine.handshakeStatus

        while (handshakeStatus != HandshakeStatus.FINISHED &&
            handshakeStatus != HandshakeStatus.NOT_HANDSHAKING
        ) {
            when (handshakeStatus) {
                HandshakeStatus.NEED_UNWRAP -> {
                    //检查是否需要读取网络数据
                    if (!readNetData.hasRemaining() || needReadHandshake) {
                        if (needReadHandshake) {
                            needReadHandshake = false
                            readNetData.compact()
                        } else {
                            readNetData.clear()
                        }
                        val readlen = socketChannel.read(readNetData)
                        //清理退出
                        if (readlen < 0) {
                            endOfStream()
                            return false
                        } else {
                            readNetData.flip()
                        }
                    } else {
                        try {
                            result = engine.unwrap(readNetData, peerAppData)

                            handshakeStatus = result.handshakeStatus

                            when (result.status) {
                                SSLEngineResult.Status.OK -> {
                                    //continue
                                }
                                SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                                    //更大的解码缓冲区
                                    if (peerAppData.capacity() >= engine.session.applicationBufferSize) {
                                        throw IOException("BUFFER_OVERFLOW")
                                    }
                                    peerAppData = peerAppData.enlargeBuffer(engine.session.applicationBufferSize)
                                    peerAppData.limit(peerAppData.capacity())
                                }

                                SSLEngineResult.Status.BUFFER_UNDERFLOW -> {

                                    //需要更多的内容进行解码 有16kb buff 的上限 防止客户端的恶意缓冲区攻击

                                    if (readNetData.capacity() < engine.session.packetBufferSize) {
                                        //新建一个更大的缓冲区
                                        readNetData = readNetData.enlargeBuffer(engine.session.packetBufferSize)
                                        needReadHandshake = true
                                    } else {
                                        if (readNetData.hasRemaining()) {
                                            needReadHandshake = true
                                        } else {
                                            throw SSLException("BUFFER_UNDERFLOW")
                                        }
                                    }
                                }
                                SSLEngineResult.Status.CLOSED -> {
                                    //解码失败
                                    return if (engine.isOutboundDone) {
                                        false
                                    } else {
                                        throw SSLException("SSLEngineResult.Status.CLOSED")
                                    }
                                }
                                else -> throw IllegalStateException("Invalid SSL status: " + result.status)
                            }
                        } catch (t: Throwable) {
                            t.printStackTrace()
                            engine.closeOutbound()
                            //改变引擎的状态 并尝试安全关闭
                            handshakeStatus = engine.handshakeStatus
                        }
                    }
                }

                HandshakeStatus.NEED_WRAP -> {
                    writeNetData.clear()
                    result = engine.wrap(myAppData, writeNetData)
                    handshakeStatus = result.handshakeStatus
                    when (result.status) {
                        SSLEngineResult.Status.OK -> {
                            writeNetData.flip()
                            if (!writeNetData.hasRemaining()) {
                                return false
                            }
                            //完整写出 握手内容
                            while (writeNetData.hasRemaining()) {
                                socketChannel.write(writeNetData)
                            }
                        }

                        SSLEngineResult.Status.BUFFER_OVERFLOW -> {

                            //扩大写出缓冲区
                            if (writeNetData.capacity() >= engine.session.packetBufferSize) {
                                throw IOException("BUFFER_OVERFLOW")
                            }
                            writeNetData = writeNetData.enlargeBuffer(engine.session.packetBufferSize)
                            writeNetData.limit(writeNetData.capacity())

                        }
                        SSLEngineResult.Status.BUFFER_UNDERFLOW ->
                            throw SSLException("Buffer underflow occured after a wrap.")

                        SSLEngineResult.Status.CLOSED -> {
                            //发送关闭通知
                            writeNetData.flip()
                            if (!writeNetData.hasRemaining()) {
                                return false
                            }
                            while (writeNetData.hasRemaining()) {
                                socketChannel.write(writeNetData)
                            }
                            readNetData.clear()
                        }

                        else -> throw IllegalStateException("Invalid SSL status: " + result.status)
                    }
                }

                HandshakeStatus.NEED_TASK -> {
                    var task: Runnable?
                    while (engine.delegatedTask.also { task = it } != null) {
                        task?.run()
                    }
                    handshakeStatus = engine.handshakeStatus
                }

                HandshakeStatus.FINISHED -> {

                }

                HandshakeStatus.NOT_HANDSHAKING -> {

                }

                else -> {
                    throw IllegalStateException("Invalid SSL status: $handshakeStatus")
                }
            }
        }
        if (handshakeStatus == HandshakeStatus.FINISHED) {
            return true
        }
        return false
    }


    private fun endOfStream() {
        whenNotClosed {
            closeStatus = true
            try {
                sslEngine.closeInbound()
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                sslEngine.closeOutbound()
                channl.close()
            }
        }

    }

    private suspend fun closeOutbound() {
        whenNotClosed {
            closeStatus = true
            try {
                sslEngine.closeOutbound()
                doHandshake(channl, sslEngine)
            } catch (t: Throwable) {
                t.printStackTrace()
            } finally {
                channl.close()
            }
        }
    }


    var needRead = false
    private suspend fun readImpl(buff: ByteBuffer): Int {

        //检查已有的解码内容
        if (readAppData.hasRemaining()) {
            return buff.putByteBuffer(readAppData)
        } else {
            //检查被读取但没被解码的
            if (readNetData.hasRemaining() && !needRead) {
                readAppData.clear()
                while (readNetData.hasRemaining() && readAppData.hasRemaining() && !needRead) {

                    val result: SSLEngineResult = sslEngine.unwrap(readNetData, readAppData)
                    when (result.status) {
                        SSLEngineResult.Status.OK -> {
                        }

                        SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                            //待读取缓冲区爆满
                            if (readAppData.capacity() >= session.applicationBufferSize) {
                                break
                            } else {
                                readAppData = readAppData.enlargeBuffer(session.applicationBufferSize)
                                readAppData.limit(readAppData.capacity())
                            }
                        }

                        SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                            //有16kb buff 的上限
                            if (readNetData.capacity() < session.packetBufferSize) {
                                //新建一个更大的缓冲区
                                readNetData = readNetData.enlargeBuffer(session.packetBufferSize)
                                needRead = true
                            } else {
                                if (readNetData.hasRemaining()) {
                                    needRead = true
                                } else {
                                    throw SSLException("BUFFER_UNDERFLOW")
                                }
                            }

                        }
                        SSLEngineResult.Status.CLOSED -> {
                            close()
                            return -1
                        }
                        else -> {
                            throw IllegalStateException("Invalid SSL status: " + result.status)
                        }
                    }
                }
                readAppData.flip()
            } else {
                if (needRead) {
                    needRead = false
                    readNetData.compact()
                } else {
                    readNetData.clear()
                }

                val len = channl.read(readNetData)
                readNetData.flip()
                if (len < 0) {
                    endOfStream()
                    return -1
                }
            }
        }

        return 0
    }


    private suspend fun writeImpl(buff: ByteBuffer): Int {

        val outLenMark = buff.remaining()

        writeNetData.clear()
        while (buff.hasRemaining() && writeNetData.hasRemaining()) {
            val result: SSLEngineResult = sslEngine.wrap(buff, writeNetData)
            when (result.status) {
                SSLEngineResult.Status.OK -> {
                }
                SSLEngineResult.Status.BUFFER_OVERFLOW -> {
                    if (writeNetData.capacity() >= session.packetBufferSize) {
                        //待发送的缓冲区爆满 故退出 并发送
                        break
                    } else {
                        writeNetData = writeNetData.enlargeBuffer(session.packetBufferSize)
                        writeNetData.limit(writeNetData.capacity())
                    }
                }
                SSLEngineResult.Status.BUFFER_UNDERFLOW -> {
                    //不应该在这里触发需要更多的字节
                    throw SSLException("Buffer underflow occured after a wrap.")
                }

                SSLEngineResult.Status.CLOSED -> {
                    close()
                    return -1
                }
                else -> {
                    throw IllegalStateException("Invalid SSL status: " + result.status)
                }
            }
        }
        writeNetData.flip()

        while (writeNetData.hasRemaining()) {
            channl.write(writeNetData)
        }

        return buff.remaining() - outLenMark
    }

    override suspend fun read(buff: ByteBuffer): Int {
        checkClosed("ClosedChannelException")

        if (!active)
            throw HandShakeException()

        return readImpl(buff)
    }

    override suspend fun write(buff: ByteBuffer): Int {
        checkClosed("ClosedChannelException")

        if (!active)
            throw HandShakeException()

        return writeImpl(buff)
    }


    override fun close() {
        runBlocking {
            closeOutbound()
        }
    }

    class HandShakeException(str: String = "HandShakeException") : Exception(str)
}

/**
 * Creates the key managers required to initiate the {@link SSLContext}, using a JKS keystore as an input.
 *
 * @param filepath - the path to the JKS keystore.
 * @param keystorePassword - the keystore's password.
 * @param keyPassword - the key's passsword.
 * @return {@link KeyManager} array that will be used to initiate the {@link SSLContext}.
 * @throws Exception
 */
fun createKeyManagers(filepath: String, keystorePassword: String, keyPassword: String): Array<KeyManager> {
    val keyStore = KeyStore.getInstance("JKS")
    val keyStoreIS: InputStream = FileInputStream(filepath)
    try {
        keyStore.load(keyStoreIS, keystorePassword.toCharArray())
    } finally {
        keyStoreIS.close()
    }
    val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
    kmf.init(keyStore, keyPassword.toCharArray())
    return kmf.keyManagers
}

/**
 * Creates the trust managers required to initiate the [SSLContext], using a JKS keystore as an input.
 *
 * @param filepath - the path to the JKS keystore.
 * @param keystorePassword - the keystore's password.
 * @return [TrustManager] array, that will be used to initiate the [SSLContext].
 * @throws Exception
 */
fun createTrustManagers(filepath: String, keystorePassword: String): Array<TrustManager> {
    val trustStore = KeyStore.getInstance("JKS")
    val trustStoreIS: InputStream = FileInputStream(filepath)
    try {
        trustStore.load(trustStoreIS, keystorePassword.toCharArray())
    } finally {
        trustStoreIS.close()
    }
    val trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    trustFactory.init(trustStore)
    return trustFactory.trustManagers
}
