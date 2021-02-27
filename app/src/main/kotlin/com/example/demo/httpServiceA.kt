package com.example.demo.httpA

import kt.nio.reader.LineReader
import kotlinx.coroutines.*
import kt.nio.*
import kt.nio.stream.*
import kt.nio.util.useInTime
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousServerSocketChannel

fun main(){
    runBlocking {

        val serverChannel= AsynchronousServerSocketChannel.open()
        serverChannel.bind( InetSocketAddress(8881))
        val serverHandle = AsynchronousAcceptAbleServerSocketChannel(serverChannel)

        while (true){
            try {
                val client = AsynchronousReadWriteAbleSocketChannel(serverHandle.accept())
                read(client.buildReadAbleStream(),
                    client.buildWriteAbleStream())
            }catch (t:Throwable){
                t.printStackTrace()
            }
        }
    }
}


fun read(readAble: ReadAbleStream,writeAble: WriteAbleStream){
    val lineReader=LineReader(readAble.bufferedReadAbleStream())
    GlobalScope.launch() {
        try {
            var txt=""
            while (true) {
                readAble.useInTime(5000){
                    lineReader.readline().also { txt = it }
                }
                if (txt==""){
                    break
                }
                println("内容:$txt")
            }
            //println("开始发送")

            writeAble.write("HTTP/1.1 200 OK\r\n".toByteArray())
            writeAble.write("Content-type:text/html\r\n".toByteArray())

            writeAble.write("\r\n".toByteArray())

            writeAble.write("<h1>successful</h1>\r\n".toByteArray())

        }catch (t:Throwable){
            t.printStackTrace()
        } finally {
            readAble.close()
            writeAble.close()
            System.err.println("退出读取!")
        }
    }
}





