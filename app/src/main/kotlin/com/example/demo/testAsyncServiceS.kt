package com.example.demo.tests

import kotlinx.coroutines.*
import kt.nio.NioDispatcher
import kt.nio.singlethread.select.CoroutineSelector
import kt.nio.singlethread.select.bindAccept
import kt.nio.singlethread.select.bindRead
import kt.nio.singlethread.select.bindWrite
import kt.nio.stream.ReadAbleStream
import kt.nio.stream.WriteAbleStream
import kt.nio.stream.buildReadAbleStream
import kt.nio.stream.buildWriteAbleStream
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashSet

fun main(){
    runBlocking(NioDispatcher.IO){

        val connectPool= Array(
            (Runtime.getRuntime().availableProcessors()/2)
            .coerceAtLeast(1)
        ) {
            ConnectHandle()
        }


        launch(currentCoroutineContext()) {
            while (true){
                for (i in connectPool){
                    i.gcConnection()
                }
                delay(300)
            }
        }

        val server= ServerSocketChannel.open()
        server.configureBlocking(false)
        server.bind( InetSocketAddress(8881))
        val serverHandle=server.bindAccept()
        while (true){
            try {
                val client = serverHandle.accept()
                client.configureBlocking(false)
                connectPool.sortBy { it.size }
                connectPool[0].addConnect(client)
            }catch (t:Throwable){
                t.printStackTrace()
            }
        }
    }
}




class ConnectHandle{
    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val connectionSet=HashSet<SocketChannel>()
    private val writeSelector=CoroutineSelector()
    private val readSelector=CoroutineSelector()
    private val tmp=LinkedList<SocketChannel>()

    fun addConnect(conn:SocketChannel){
        connectionSet.add(conn)
        val write=conn.bindWrite(writeSelector)
        val read=conn.bindRead(readSelector)
        read(read.buildReadAbleStream())
        write(write.buildWriteAbleStream())
    }

    val size get()  = connectionSet.size

    fun gcConnection(){
        tmp.clear()
        for (i in connectionSet){
            if (!i.isOpen){
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
