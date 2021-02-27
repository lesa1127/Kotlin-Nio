package kt.nio

import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


interface BaseAsynchronousChannel:CoroutineChannel{
    val channel: AsynchronousChannel
}

interface AsynchronousAcceptAble:AcceptAble,BaseAsynchronousChannel

interface AsynchronousConnectAble:ConnectAble,BaseAsynchronousChannel

interface AsynchronousReadAble:ReadAble,BaseAsynchronousChannel

interface AsynchronousWriteAble:WriteAble,BaseAsynchronousChannel

interface AsynchronousReadWriteAble:ReadWriteAble,BaseAsynchronousChannel


abstract class AbsBaseAsynchronousChannel:BaseAsynchronousChannel{
    private var closeStatus=false
    override val isClosed: Boolean
        get() = closeStatus

    override fun close() {
        whenNotClosed {
            closeStatus=true
            channel.close()
        }
    }
}


open class AsynchronousAcceptAbleServerSocketChannel(override val channel: AsynchronousServerSocketChannel) :
    AbsBaseAsynchronousChannel(),AsynchronousAcceptAble{
    override suspend fun accept(): AsynchronousSocketChannel {
        return suspendCoroutine {
            val cont=it
            channel.accept<Any>(cont,object : CompletionHandler<AsynchronousSocketChannel,Any>{
                override fun completed(result: AsynchronousSocketChannel, attachment: Any) {
                    cont.resume(result)
                }

                override fun failed(exc: Throwable, attachment: Any) {
                    cont.resumeWithException(exc)
                }
            })

        }
    }

}

open class AsynchronousConnectAbleSocketChannel(override val channel: AsynchronousSocketChannel) :
    AbsBaseAsynchronousChannel(),AsynchronousConnectAble{
    override suspend fun connect(socketAddress: SocketAddress) {
        return suspendCoroutine {
            val cont = it
            channel.connect<Any>(socketAddress,cont,object : CompletionHandler<Void,Any>{
                override fun completed(result: Void, attachment: Any){
                    cont.resume(Unit)
                }

                override fun failed(exc: Throwable, attachment: Any) {
                    cont.resumeWithException(exc)
                }
            })
        }
    }
}

open class AsynchronousReadAbleSocketChannel(override val channel: AsynchronousSocketChannel) :
    AbsBaseAsynchronousChannel(),AsynchronousReadAble{
    override suspend fun read(buff: ByteBuffer): Int {
        return suspendCoroutine {
            val cont = it
            channel.read<Any>(buff,cont,object : CompletionHandler<Int, Any>{
                override fun completed(result: Int, attachment: Any) {
                    cont.resume(result)
                }

                override fun failed(exc: Throwable, attachment: Any) {
                    cont.resumeWithException(exc)
                }
            })
        }
    }
}

open class AsynchronousWriteAbleSocketChannel(override val channel: AsynchronousSocketChannel) :
    AbsBaseAsynchronousChannel(),AsynchronousWriteAble{
    override suspend fun write(buff: ByteBuffer): Int {
        return suspendCoroutine {
            val cont = it
            channel.write<Any>(buff,cont,object : CompletionHandler<Int, Any>{
                override fun completed(result: Int, attachment: Any) {
                    cont.resume(result)
                }

                override fun failed(exc: Throwable, attachment: Any) {
                    cont.resumeWithException(exc)
                }

            })
        }
    }

}

open class AsynchronousReadWriteAbleSocketChannel(override val channel: AsynchronousSocketChannel):
    AbsBaseAsynchronousChannel(),AsynchronousReadWriteAble{
    override suspend fun write(buff: ByteBuffer): Int {
        return suspendCoroutine {
            val cont = it
            channel.write<Any>(buff,cont,object : CompletionHandler<Int, Any>{
                override fun completed(result: Int, attachment: Any) {
                    cont.resume(result)
                }

                override fun failed(exc: Throwable, attachment: Any) {
                    cont.resumeWithException(exc)
                }

            })
        }
    }

    override suspend fun read(buff: ByteBuffer): Int {
        return suspendCoroutine {
            val cont = it
            channel.read<Any>(buff,cont,object : CompletionHandler<Int, Any>{
                override fun completed(result: Int, attachment: Any) {
                    cont.resume(result)
                }

                override fun failed(exc: Throwable, attachment: Any) {
                    cont.resumeWithException(exc)
                }
            })
        }
    }
}

open class AsynchronousReadWriteAbleFileChannel(override val channel: AsynchronousFileChannel):
        AbsBaseAsynchronousChannel(),AsynchronousReadWriteAble{

    var offset:Long=0

    override suspend fun write(buff: ByteBuffer): Int {

        return suspendCoroutine {
            val cont = it
            channel.write<Any>(buff,offset,cont,object : CompletionHandler<Int, Any>{
                override fun completed(result: Int, attachment: Any) {
                    if (result>=0) {
                        offset += result
                    }
                    cont.resume(result)
                }

                override fun failed(exc: Throwable, attachment: Any) {
                    cont.resumeWithException(exc)
                }

            })
        }
    }

    override suspend fun read(buff: ByteBuffer): Int {
        return suspendCoroutine {
            val cont = it
            channel.read<Any>(buff,offset,cont,object : CompletionHandler<Int, Any>{
                override fun completed(result: Int, attachment: Any) {
                    if (result>=0) {
                        offset += result
                    }
                    cont.resume(result)
                }

                override fun failed(exc: Throwable, attachment: Any) {
                    cont.resumeWithException(exc)
                }
            })
        }
    }

    suspend fun lock(position:Long=0, size:Long= Long.MAX_VALUE, shared:Boolean=false):FileLock{
        return suspendCoroutine {
            val cont = it
            channel.lock<Any>(position,size,shared,cont,object : CompletionHandler<FileLock,  Any>{
                override fun completed(result: FileLock, attachment: Any) {
                    cont.resume(result)
                }

                override fun failed(exc: Throwable, attachment: Any) {
                    cont.resumeWithException(exc)
                }

            })
        }
    }

}