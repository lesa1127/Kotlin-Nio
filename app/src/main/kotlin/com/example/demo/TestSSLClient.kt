package com.example.demo.testsslc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kt.nio.*
import kt.nio.singlethread.select.*
import kt.nio.ssl.SSLSocketChannel
import kt.nio.ssl.createKeyManagers
import kt.nio.ssl.createTrustManagers
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.security.SecureRandom
import javax.net.ssl.SSLContext


fun main()= runBlockingIOScope {

    //加载秘钥
    val sslContext = SSLContext.getInstance("TLSv1.3")
    sslContext.init(
        createKeyManagers("D:\\downlaod\\kotlin-nio\\app\\src\\main\\resources\\client.jks", "storepass", "keypass"),
        createTrustManagers("D:\\downlaod\\kotlin-nio\\app\\src\\main\\resources\\trustedCerts.jks", "storepass"),
        SecureRandom()
    )

    val client= SocketChannel.open()
    client.configureBlocking(false)
    val clientHandle=client.bindConnect()
    try {
        clientHandle.connect(InetSocketAddress("localhost", 9222))
        val readAble=client.bindRead()
        val writeAble=client.bindWrite()
        val sslSocketChannel = SSLSocketChannel(sslContext,"localhost", 9222, buildReadWriteAble(readAble,writeAble))
        sslSocketChannel.useClientMode=true
        runIt(sslSocketChannel)
    }catch (t:Throwable){
        t.printStackTrace()
    }
}

suspend fun runIt(sslSocketChannel: SSLSocketChannel){
    try {
        sslSocketChannel.beginHandshake()
        while (true) {
            sslSocketChannel.write(ByteBuffer.wrap("ok\n".toByteArray()))
        }
    }catch (t:Throwable){
        t.printStackTrace()
    }finally {
        sslSocketChannel.close()
        println("退出完成")
    }
}
