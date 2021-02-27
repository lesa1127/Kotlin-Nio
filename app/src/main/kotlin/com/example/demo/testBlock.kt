package com.example.demo.block

import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class SocketHandle(val socket:Socket):Thread(){
    override fun run() {
        socket.soTimeout=4000
        try {
            while (true) {
                socket.getInputStream().copyTo(socket.getOutputStream())
            }
        }catch (t:Throwable){
            t.printStackTrace()
        }finally {
            socket.close()
        }
    }
}

fun main(){
    val server = ServerSocket()
    server.bind( InetSocketAddress(8881))
    while (true){
        val client = server.accept()
        val thre=SocketHandle(client)
        thre.start()
    }
}