package com.example.demo.testssls

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kt.nio.*
import kt.nio.singlethread.select.*
import kt.nio.ssl.SSLSocketChannel
import kt.nio.ssl.createKeyManagers
import kt.nio.ssl.createTrustManagers
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.security.SecureRandom
import javax.net.ssl.SSLContext


fun main()= runBlocking(NioDispatcher.IO) {
    //获取ssl 用秘钥
    val sslContext = SSLContext.getInstance("TLSv1.3")

    sslContext.init(
        createKeyManagers("D:\\downlaod\\kotlin-nio\\app\\src\\main\\resources\\server.jks", "storepass", "keypass"),
        createTrustManagers("D:\\downlaod\\kotlin-nio\\app\\src\\main\\resources\\trustedCerts.jks", "storepass"),
        SecureRandom()
    )

    val server= ServerSocketChannel.open()
    server.configureBlocking(false)
    server.bind( InetSocketAddress("localhost", 9222))
    val serverHandle=server.bindAccept()
    while (true){
        try {
            val client = serverHandle.accept()
            client.configureBlocking(false)
            val readAble=client.bindRead()
            val writeAble=client.bindWrite()
            val sslSocketChannel = SSLSocketChannel(sslContext, buildReadWriteAble(readAble,writeAble))
            sslSocketChannel.useClientMode=false
            runIt(sslSocketChannel)
        }catch (t:Throwable){
            t.printStackTrace()
        }
    }
}

fun runIt(sslSocketChannel: SSLSocketChannel){
    GlobalScope.launch(NioDispatcher.IO) {
        try {
            sslSocketChannel.beginHandshake()
            val buff=ByteBuffer.allocate(4096)
            while (true) {
                val len=sslSocketChannel.read(buff)
                if (len>0){
                    buff.flip()
                    while (buff.hasRemaining()){
                        System.out.write(buff.get().toInt())
                    }
                    buff.clear()
                    //delay(500)
                }else if (len==0){
                    continue
                }else{
                    println("break")
                    break
                }
            }
        }catch (t:Throwable){
            t.printStackTrace()
        }finally {
            sslSocketChannel.close()
        }
    }
}
