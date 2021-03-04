package com.example.demo.http

import kt.nio.reader.LineReader
import kotlinx.coroutines.*
import kt.nio.*
import kt.nio.singlethread.select.bindAccept
import kt.nio.singlethread.select.bindRead
import kt.nio.singlethread.select.bindWrite
import kt.nio.stream.*
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel

fun main(){
    runBlocking (NioDispatcher.IO) {
        val server= ServerSocketChannel.open()
        server.configureBlocking(false)
        server.bind( InetSocketAddress(8881))
        val serverHandle=server.bindAccept()
        while (true){
            try {
                val client = serverHandle.accept()
                println("accept")
                client.configureBlocking(false)
                read(client.bindRead().buildReadAbleStream(),
                    client.bindWrite().buildWriteAbleStream())
            }catch (t:Throwable){
                t.printStackTrace()
            }
        }
    }
}


fun read(readAble: ReadAbleStream,writeAble: WriteAbleStream){
    val lineReader=LineReader(readAble.bufferedReadAbleStream())
    val out = writeAble.bufferedWriteAbleStream()
    GlobalScope.launch(NioDispatcher.IO) {
        try {
            var txt=""
            while (true) {
                lineReader.readline().also { txt = it }
                if (txt==""){
                    break
                }
                println("内容:$txt")
            }

            out.write("HTTP/1.1 200 OK\r\n".toByteArray())
            out.write("Content-type:text/html\r\n".toByteArray())

            out.write("\r\n".toByteArray())

            out.write("<h1>successful</h1>\r\n".toByteArray())
            out.flush()

        }catch (t:Throwable){
            t.printStackTrace()
        } finally {
            readAble.close()
            writeAble.close()
            System.err.println("退出读取!")
        }
    }
}





