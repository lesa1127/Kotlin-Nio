package com.example.demo.testsslc

import kotlinx.coroutines.runBlocking
import kt.nio.singlethread.select.*
import kt.nio.ssl.SSLSocketChannel
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import javax.net.ssl.SSLContext


fun main()= runBlocking {

    //获取ssl 用秘钥
    val sslContext = SSLContext.getDefault()

    val client= SocketChannel.open()
    client.configureBlocking(false)
    val clientHandle=client.bindConnect()
    try {
        clientHandle.connect(InetSocketAddress("www.sogou.com",443))
        val readAble=client.bindRead()
        val writeAble=client.bindWrite()
        val sslSocketChannel = SSLSocketChannel(sslContext, buildReadWriteAble(readAble,writeAble))
        sslSocketChannel.useClientMode=true
        getIt(sslSocketChannel)
    }catch (t:Throwable){
        t.printStackTrace()
    }
}

suspend fun getIt(sslSocketChannel: SSLSocketChannel){
    try {
        sslSocketChannel.beginHandshake()
        val buff=ByteBuffer.allocate(8192)
        buff.put("GET / HTTP/1.1\r\n".toByteArray())
        buff.put("User-Agent: PostmanRuntime/7.26.8\r\n".toByteArray())
        buff.put("Accept: */*\r\n".toByteArray())
        buff.put("Cache-Control: no-cache\r\n".toByteArray())
        buff.put("Accept-Encoding: gzip, deflate, br\r\n".toByteArray())
        buff.put("\r\n".toByteArray())
        buff.flip()
        while (buff.hasRemaining()){
            sslSocketChannel.write(buff)
        }
        println("写完成")


        while (true) {
            buff.clear()
            val len = sslSocketChannel.read(buff)
            if (len < 0)
                break
            else if (len ==0 ){
                continue
            }else{
                buff.flip()
                while (buff.hasRemaining()) {
                    System.out.write(buff.get().toInt())
                }
            }
        }
        println("读完成")
    }catch (t:Throwable){
        t.printStackTrace()
    }finally {
        sslSocketChannel.close()
    }
}
