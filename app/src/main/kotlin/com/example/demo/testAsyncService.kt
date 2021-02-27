/**
 * Negative example
 */

package com.example.demo.test

import kotlinx.coroutines.*
import kt.nio.multithreaded.select.bindAccept
import kt.nio.multithreaded.select.bindRead
import kt.nio.multithreaded.select.bindWrite
import kt.nio.stream.ReadAbleStream
import kt.nio.stream.WriteAbleStream
import kt.nio.stream.buildReadAbleStream
import kt.nio.stream.buildWriteAbleStream
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel

fun main(){
    runBlocking(Dispatchers.IO) {


        val server= ServerSocketChannel.open()
        server.configureBlocking(false)
        server.bind( InetSocketAddress(8881))
        val serverHandle=server.bindAccept()
        while (true){
            try {
                val client = serverHandle.accept()
                client.configureBlocking(false)
                read(client.bindRead().buildReadAbleStream(),
                    client.bindWrite().buildWriteAbleStream())
                write(client.bindRead().buildReadAbleStream(),
                    client.bindWrite().buildWriteAbleStream())
            }catch (t:Throwable){
                t.printStackTrace()
            }
        }
    }
}


fun read(reader: ReadAbleStream, writeAble: WriteAbleStream){
    GlobalScope.launch(Dispatchers.IO) {
        try {
            while (true) {
                val buff=ByteArray(4096)
                val readlen = reader.read(buff)
            }
        }catch (t:Throwable){
            t.printStackTrace()
        }finally {
            reader.close()
            writeAble.close()
            System.err.println("退出读取!")
        }
    }
}

fun write(reader: ReadAbleStream, writeAble: WriteAbleStream){
    GlobalScope.launch(Dispatchers.IO) {
        try {
            while (true) {
                val buff=ByteArray(4096)
                writeAble.write(buff, 0, buff.size)
            }
        }catch (t:Throwable){
            t.printStackTrace()
        }finally {
            reader.close()
            writeAble.close()
            System.err.println("退出读取!")
        }
    }
}




