@file:Suppress("BlockingMethodInNonBlockingContext")

package kt.nio.singlethread.select

import kotlinx.coroutines.*
import kt.nio.*
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.*
import kotlin.coroutines.CoroutineContext


interface CoroutineSelectAble: BaseSelectAble {
    var interruptTime:Long
    var waitTime:Long
    var delayTime:Long
    var maxDelay:Long
}


abstract class AbsCoroutineSelectAble :CoroutineSelectAble{
    override var interruptTime: Long = System.currentTimeMillis()

    override var waitTime: Long=500

    override var delayTime: Long=1

    override var maxDelay: Long=500

    protected var closeStatus=false
    override val isClosed: Boolean
        get() = closeStatus || !channel.isOpen



    override fun close() {
        whenNotClosed {
            closeStatus=true
            channel.close()
        }
    }
}


/**
 * @param selector             A multiplexor of {@link SelectableChannel} objects.
 * @param coroutineDispatcher  only the CoroutineDispatcher can to select the selector
 */
open class CoroutineSelector(
    override val selector: Selector = Selector.open(),
    private var coroutineDispatcher: CoroutineDispatcher?=null
) : BaseSelector{
    private var exit = false

    override suspend fun select(selectAble: BaseSelectAble) {
        if (coroutineDispatcher==null){
            coroutineDispatcher=currentCoroutineContext().coroutineDispatcher
        }
        val context=currentCoroutineContext()
        if (context.coroutineDispatcher!=coroutineDispatcher){
            throw Exception("CoroutineDispatcher different: $context")
        }
        selectAble as CoroutineSelectAble
        selectAble.interruptTime=System.currentTimeMillis()
        selectAble.selectionKey.interestOps(selectAble.selectionKey.interestOps() or  selectAble.ops)
        var sleepTime=selectAble.delayTime
        var wait=false
        while (!exit){
            if (selectAble.isClosed)
                throw ClosedChannelException()
            var set = selector.selectedKeys()
            if (set.contains(selectAble.selectionKey)){
                set.remove(selectAble.selectionKey)
                if (selectAble.selectionKey.isValid)
                    break
            }
            selector.selectNow()
            set = selector.selectedKeys()
            if (set.contains(selectAble.selectionKey)) {
                set.remove(selectAble.selectionKey)
                if (selectAble.selectionKey.isValid)
                    break
            }
            wait=true
            if (selectAble.delayTime>0) {
                if (System.currentTimeMillis() - selectAble.interruptTime < selectAble.waitTime) {
                    yield()
                } else {
                    delay(sleepTime)
                    if (sleepTime<selectAble.maxDelay) {
                        sleepTime++
                    }
                }
            }else{
                yield()
            }
        }
        if (!wait){
            yield()
        }

        if (exit){
            throw ClosedSelectorException()
        }

        selectAble.selectionKey.interestOps(selectAble.selectionKey.interestOps() and selectAble.ops.inv())
    }

    override fun close() {
        exit=true
        selector.close()
    }
}

internal suspend fun CoroutineSelectAble.acceptImpl(): SocketChannel {

    selector.select(this)
    if (this.ops == SelectionKey.OP_ACCEPT) {
        return (this.channel as ServerSocketChannel).accept()
    } else {
        throw Exception("select type error!")
    }
}

internal suspend fun CoroutineSelectAble.connectImpl(socketAddress: SocketAddress){
    val channel = channel as SocketChannel
    if (channel.connect(socketAddress)) {
        return
    } else {
        while (true) {
            selector.select(this)
            if (this.ops == SelectionKey.OP_CONNECT) {
                if (channel.finishConnect())
                    break
            } else {
                throw Exception("select type error!")
            }
        }
    }
}

internal suspend fun CoroutineSelectAble.readBuffImpl(buff: ByteBuffer):Int{
    selector.select(this)
    if (this.ops == SelectionKey.OP_READ) {
        return (this.channel as ReadableByteChannel).read(buff)
    } else {
        throw Exception("select type error!")
    }
}

internal suspend fun CoroutineSelectAble.writeBuffImpl(buff: ByteBuffer):Int{
    selector.select(this)
    if (this.ops == SelectionKey.OP_WRITE) {
        return (this.channel as WritableByteChannel).write(buff)
    } else {
        throw Exception("select type error!")
    }
}

fun baseSelectHolder(baseSelector: CoroutineSelector,channel:SelectableChannel,ops: Int):CoroutineSelectAble{
    return object :AbsCoroutineSelectAble(){
        override val channel: SelectableChannel=channel
        override val selectionKey: SelectionKey=channel.register(baseSelector.selector,ops)
        override val ops: Int = ops
        override val selector: BaseSelector=baseSelector
        init {
            selectionKey.interestOps(selectionKey.interestOps() and  ops.inv())
        }
    }
}

interface CoroutineSelectAcceptAble:CoroutineSelectAble, AcceptAble {
    override suspend fun accept(): SocketChannel
}

interface CoroutineSelectConnectAble:CoroutineSelectAble, ConnectAble

interface CoroutineSelectReadAble:CoroutineSelectAble, ReadAble

interface CoroutineSelectWriteAble:CoroutineSelectAble,WriteAble

interface BaseSelectReadWriteAble:ReadWriteAble{
    val writeAble:CoroutineSelectWriteAble
    val readAble:CoroutineSelectReadAble
}

fun ServerSocketChannel.bindAccept(baseSelector: CoroutineSelector): CoroutineSelectAcceptAble {
    val selectAble = baseSelectHolder(baseSelector,this,SelectionKey.OP_ACCEPT)
    selectAble.maxDelay=2
    return object :CoroutineSelectAcceptAble ,CoroutineSelectAble by selectAble{
        override suspend fun accept(): SocketChannel {
            checkClosed("ClosedChannelException")
            return acceptImpl()
        }
    }
}

fun SocketChannel.bindConnect(baseSelector: CoroutineSelector):CoroutineSelectConnectAble{
    val selectAble = baseSelectHolder(baseSelector,this,SelectionKey.OP_CONNECT)
    return object :CoroutineSelectConnectAble ,CoroutineSelectAble by selectAble{
        override suspend fun connect(socketAddress: SocketAddress) {
            checkClosed("ClosedChannelException")
            connectImpl(socketAddress)
        }
    }
}

fun SelectableChannel.bindRead(baseSelector: CoroutineSelector): CoroutineSelectReadAble {
    if (this !is ReadableByteChannel){
        throw IllegalAccessException("type no was ReadableByteChannel!")
    }
    val selectAble =baseSelectHolder(baseSelector,this,SelectionKey.OP_READ)
    selectAble.waitTime=100
    return object :CoroutineSelectReadAble ,CoroutineSelectAble by selectAble{
        override suspend fun read(buff: ByteBuffer): Int {
            checkClosed("ClosedChannelException")
            return readBuffImpl(buff)
        }
    }
}

fun SelectableChannel.bindWrite(baseSelector: CoroutineSelector): CoroutineSelectWriteAble {
    if (this !is WritableByteChannel){
        throw IllegalAccessException("type no was WritableByteChannel!")
    }
    val selectAble = baseSelectHolder(baseSelector,this,SelectionKey.OP_WRITE)
    selectAble.waitTime=100
    return object :CoroutineSelectWriteAble ,CoroutineSelectAble by selectAble{
        override suspend fun write(buff: ByteBuffer): Int {
            checkClosed("ClosedChannelException")
            return writeBuffImpl(buff)
        }
    }
}

fun SelectableChannel.bindReadWriteAble(read:CoroutineSelector,
                                        write:CoroutineSelector):BaseSelectReadWriteAble{
    return buildReadWriteAble(bindRead(read),bindWrite(write))
}

fun buildReadWriteAble(read:CoroutineSelectReadAble,write:CoroutineSelectWriteAble):BaseSelectReadWriteAble{
    return object :BaseSelectReadWriteAble{
        override val isClosed: Boolean
            get() = closeStatus
        var closeStatus=false
        override val writeAble: CoroutineSelectWriteAble
            get() = write
        override val readAble: CoroutineSelectReadAble
            get() = read

        override suspend fun write(buff: ByteBuffer): Int {
            checkClosed("ClosedChannelException")
            return write.write(buff)
        }

        override suspend fun read(buff: ByteBuffer): Int {
            checkClosed("ClosedChannelException")
            return read.read(buff)
        }

        override fun close() {
            whenNotClosed {
                closeStatus=true
                write.use {
                    read.close()
                }
            }
        }
    }
}

open class IOCoroutineDispatcher(private val coroutineDispatcher: CoroutineDispatcher):CoroutineContext.Element{

    companion object Key : CoroutineContext.Key<IOCoroutineDispatcher>

    override val key: CoroutineContext.Key<*> = Key

    override operator fun <E : CoroutineContext.Element> get(key: CoroutineContext.Key<E>): E?{
        if (key==Key){
            @Suppress("UNCHECKED_CAST")
            return this as E
        }else{
            return coroutineDispatcher[key]
        }
    }

    private var _read: CoroutineSelector?=null
    private var _write: CoroutineSelector?=null
    private var _othre: CoroutineSelector?=null


    val Read: CoroutineSelector
        get() {
            _read?.also {
                return it
            }
            if (_read == null){
                _read = CoroutineSelector(coroutineDispatcher = coroutineDispatcher)
            }
            return _read!!
        }


    val Write: CoroutineSelector
        get() {
            _write?.also {
                return it
            }
            if (_write == null){
                _write = CoroutineSelector(coroutineDispatcher = coroutineDispatcher)
            }
            return _write!!
        }


    val Other: CoroutineSelector
        get() {
            _othre?.also {
                return it
            }
            if (_othre == null){
                _othre = CoroutineSelector(coroutineDispatcher = coroutineDispatcher)
            }
            return _othre!!
        }

    fun closeAll(){
        _write?.close()
        _read?.close()
        _othre?.close()
        _write=null
        _read=null
        _othre=null
    }

}

val CoroutineContext.ioCoroutineDispatcher: IOCoroutineDispatcher get() = get(IOCoroutineDispatcher.Key).let {
    if (it==null){
        throw Exception("hasn't extend IOCoroutineDispatcher by the class")
    }else{
        return@let it
    }
}


suspend fun getCoroutineSelectorHolder():IOCoroutineDispatcher{
    val context=currentCoroutineContext()
    return context.ioCoroutineDispatcher
}

suspend fun ServerSocketChannel.bindAccept(): CoroutineSelectAcceptAble {
    val holder = getCoroutineSelectorHolder()
    return bindAccept(holder.Other)
}

suspend fun SocketChannel.bindConnect():CoroutineSelectConnectAble {
    val holder = getCoroutineSelectorHolder()
    return bindConnect(holder.Other)
}

suspend fun SelectableChannel.bindRead(): CoroutineSelectReadAble {
    val holder = getCoroutineSelectorHolder()
    return bindRead(holder.Read)
}

suspend fun SelectableChannel.bindWrite(): CoroutineSelectWriteAble {
    val holder = getCoroutineSelectorHolder()
    return bindWrite(holder.Write)
}

suspend fun SelectableChannel.bindReadWriteAble():BaseSelectReadWriteAble{
    return buildReadWriteAble(bindRead(),bindWrite())
}

fun CoroutineDispatcher.asIOCoroutineDispatcher():CoroutineContext{
    return IOCoroutineDispatcher(this)
}