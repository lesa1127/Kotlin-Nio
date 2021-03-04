package com.example.demo

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kt.nio.*
import kt.nio.singlethread.select.*
import kt.nio.stream.buildReadAbleStream
import kt.nio.stream.buildWriteAbleStream
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

fun main(){
    runBlocking(NioDispatcher.IO) {

        val server= ServerSocketChannel.open()
        server.configureBlocking(false)
        server.bind( InetSocketAddress(8881))
        val serverHandle=server.bindAccept()
        while (true){
            try {
                val client = serverHandle.accept()
                client.configureBlocking(false)
                read(client )
                write(client)
            }catch (t:Throwable){
                t.printStackTrace()
            }
        }
    }
}


fun read(client: SocketChannel){
    GlobalScope.launch(NioDispatcher.IO) {
        val reader= client.bindRead().buildReadAbleStream()
        try {
            while (true) {
                val buff=ByteArray(1024)
                val readlen = reader.read(buff)
                System.out.write(buff,0,readlen)
            }
        }catch (t:Throwable){
            t.printStackTrace()
        }finally {
            reader.close()
            System.err.println("退出读取!")
        }
    }
}

fun write(client: SocketChannel ){
    GlobalScope.launch(NioDispatcher.IO) {
        val writer=client.bindWrite().buildWriteAbleStream()
        try {
            while (true) {
                writer.write("hello!\n".toByteArray())
                delay(5000)
            }
        }catch (t:Throwable){
            t.printStackTrace()
        }finally {
            writer.close()
            System.err.println("退出写入!")
        }
    }
}


