package com.example.demo

import kt.nio.runBlockingIOScope
import kt.nio.singlethread.select.*
import kt.nio.stream.buildReadAbleStream
import kt.nio.stream.buildWriteAbleStream
import kt.nio.stream.writeBytes
import java.net.InetSocketAddress
import java.nio.channels.SocketChannel

fun main()= runBlockingIOScope {


    val socketChannel=SocketChannel.open()
    socketChannel.configureBlocking(false)
    val connectAble=socketChannel.bindConnect()
    connectAble.connect(InetSocketAddress("127.0.0.1",8881))

    val reader= socketChannel.bindRead().buildReadAbleStream()
    val wirter=socketChannel.bindWrite().buildWriteAbleStream()
    while (true){
        val buff=ByteArray(1024)
        val len=reader.read(buff)
        System.out.write(buff,0,len)
        wirter.writeBytes("ok\n".toByteArray())
    }

}