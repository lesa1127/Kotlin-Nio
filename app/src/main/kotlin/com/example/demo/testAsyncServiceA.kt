package com.example.demo.test1a

import kotlinx.coroutines.*
import kt.nio.AsynchronousAcceptAbleServerSocketChannel
import kt.nio.AsynchronousReadWriteAbleSocketChannel
import kt.nio.stream.ReadAbleStream
import kt.nio.stream.WriteAbleStream
import kt.nio.stream.buildReadAbleStream
import kt.nio.stream.buildWriteAbleStream
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousServerSocketChannel
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashSet


fun main(){
    runBlocking {
        val connectPool= Array(
            (Runtime.getRuntime().availableProcessors()/2)
                .coerceAtLeast(1)
        ) {
            ConnectHandle()
        }


        launch {
            while (true){
                for (i in connectPool){
                    i.gcConnection()
                }
                delay(300)
            }
        }

        val serverChannel=AsynchronousServerSocketChannel.open()
        serverChannel.bind(InetSocketAddress(8881))

        val server = AsynchronousAcceptAbleServerSocketChannel(serverChannel)
        while (true){
            val client= AsynchronousReadWriteAbleSocketChannel(server.accept())
            connectPool.sortBy { it.size }
            connectPool[0].addConnect(client)
        }
    }
}



class ConnectHandle{
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val connectionSet=HashSet<AsynchronousReadWriteAbleSocketChannel>()
    private val tmp= LinkedList<AsynchronousReadWriteAbleSocketChannel>()

    fun addConnect(conn: AsynchronousReadWriteAbleSocketChannel){
        connectionSet.add(conn)

        read(conn.buildReadAbleStream())
        write(conn.buildWriteAbleStream())
    }

    val size get()  = connectionSet.size

    fun gcConnection(){
        tmp.clear()
        for (i in connectionSet){
            if (i.isClosed){
                tmp.add(i)
            }
        }
        for (i in tmp){
            connectionSet.remove(i)
        }
    }


    private fun read(reader: ReadAbleStream){
        GlobalScope.launch (dispatcher){
            try {
                while (true) {
                    val buff=ByteArray(4096)
                    reader.read(buff)
                }
            }catch (t:Throwable){
                t.printStackTrace()
            }finally {
                reader.close()
                System.err.println("退出读取!")
            }
        }
    }

    private fun write( writeAble: WriteAbleStream){
        GlobalScope.launch(dispatcher) {
            try {
                while (true) {
                    val buff=ByteArray(4096)
                    writeAble.write(buff, 0, buff.size)
                }
            }catch (t:Throwable){
                t.printStackTrace()
            }finally {
                writeAble.close()
                System.err.println("退出写入!")
            }
        }
    }

}



