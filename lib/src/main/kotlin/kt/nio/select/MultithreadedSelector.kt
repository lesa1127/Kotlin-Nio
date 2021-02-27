/**
 * the file has be deprecated , please to use CoroutineSelector.kt file
 */




package kt.nio.multithreaded.select

import kotlinx.coroutines.*
import kt.nio.*
import kt.nio.util.ReentrantObjectLock
import java.io.Closeable
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.*
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal const val MIN_SIZE=1
@Deprecated("please to use CoroutineSelector.kt")
interface MassageAble{
    suspend fun getMessage():CoroutineChannel
}
@Deprecated("please to use CoroutineSelector.kt", ReplaceWith("getMessage() as K"))
suspend inline fun <reified K:CoroutineChannel> MassageAble.getMessageAs():K{
    return getMessage() as K
}
@Deprecated("please to use CoroutineSelector.kt")
interface MultithreadedSelectAble:BaseSelectAble, MassageAble {
    var cont: Continuation<MultithreadedSelectAble>?
    var error:Throwable?
}


internal suspend inline fun MultithreadedSelectAble.getMessageImpl(): BaseSelectAble {
    if (isClosed){
        throw ClosedChannelException()
    }
    cont?.also {
        throw Exception("SuspendCoroutine blocked!")
    }
    error?.also {
        throw it
    }
    selector.select(this)
    error?.also {
        throw it
    }
    return this
}

internal fun MultithreadedSelectAble.resume(){
    val tmpCont = cont
    error?.also {
        resumeWithException(it)
        return
    }

    if (tmpCont != null) {
        cont = null
        tmpCont.resume(this)
    }
}

internal fun MultithreadedSelectAble.resumeWithException(t:Throwable){
    error = t
    val tmpCont = cont
    if (tmpCont != null) {
        cont = null
        tmpCont.resumeWithException(t)
    }
}

@Deprecated("please to use CoroutineSelector.kt")
abstract class AbsMultithreadedSelectAble :MultithreadedSelectAble{
    @Volatile
    override var cont: Continuation<MultithreadedSelectAble>?=null
    @Volatile
    override var error: Throwable?=null
    protected var closeStatus=false
    override val isClosed: Boolean
        get() = closeStatus

    override suspend fun getMessage(): CoroutineChannel {
        return getMessageImpl()
    }

    override fun close() {
        whenNotClosed {
            closeStatus=true
            channel.close()
        }
    }
}

@Deprecated("please to use CoroutineSelector.kt")
abstract class MultithreadedSelector:BaseSelector, Closeable{
    var waitTime=2L
    var delayTime=1L
}


internal suspend fun MultithreadedSelectAble.acceptImpl(): SocketChannel {
    val message = getMessageAs<BaseSelectAble>()
    if (message.ops == SelectionKey.OP_ACCEPT) {
        return (message.channel as ServerSocketChannel).accept()
    } else {
        throw Exception("select type error!")
    }
}

internal suspend fun MultithreadedSelectAble.connectImpl(socketAddress: SocketAddress){
    val channel = channel as SocketChannel
    if (channel.connect(socketAddress)) {
        return
    } else {
        while (true) {
            val message = getMessageAs<BaseSelectAble>()
            if (message.ops == SelectionKey.OP_CONNECT) {
                if (channel.finishConnect())
                    break
            } else {
                throw Exception("select type error!")
            }
        }
    }
}

internal suspend fun MultithreadedSelectAble.readBuffImpl(buff: ByteBuffer):Int{
    val message = getMessageAs<BaseSelectAble>()
    if (message.ops == SelectionKey.OP_READ) {
        return (message.channel as ReadableByteChannel).read(buff)
    } else {
        throw Exception("select type error!")
    }
}

internal suspend fun MultithreadedSelectAble.writeBuffImpl(buff: ByteBuffer):Int{
    val message = getMessageAs<BaseSelectAble>()
    if (message.ops == SelectionKey.OP_WRITE) {
        return (message.channel as WritableByteChannel).write(buff)
    } else {
        throw Exception("select type error!")
    }
}

@Deprecated("please to use CoroutineSelector.kt")
fun baseSelectHolder(baseSelector: BaseSelector, channel: SelectableChannel, ops: Int):MultithreadedSelectAble{
    return object :AbsMultithreadedSelectAble(){
        override val channel: SelectableChannel =channel
        override val selectionKey: SelectionKey=channel.register(baseSelector.selector,ops)
        override val ops: Int =ops
        override val selector: BaseSelector=baseSelector
    }
}
@Deprecated("please to use CoroutineSelector.kt")
interface MultithreadedSelectAcceptAble:BaseSelectAble,AcceptAble{
    override suspend fun accept(): SocketChannel
}
@Deprecated("please to use CoroutineSelector.kt")
interface MultithreadedSelectConnectAble:BaseSelectAble,ConnectAble

@Deprecated("please to use CoroutineSelector.kt")
interface MultithreadedSelectReadAble:BaseSelectAble,ReadAble

@Deprecated("please to use CoroutineSelector.kt")
interface MultithreadedSelectWriteAble:BaseSelectAble,WriteAble

@Deprecated("please to use CoroutineSelector.kt")
interface MultithreadedSelectReadWriteAble:ReadWriteAble{
    val writeAble:MultithreadedSelectWriteAble
    val readAble:MultithreadedSelectReadAble
}
@Deprecated("please to use CoroutineSelector.kt")
fun ServerSocketChannel.bindAccept(baseSelector: MultithreadedCoroutineSelector= MultithreadedSelectors.Other): MultithreadedSelectAcceptAble {
    val selectAble = baseSelectHolder(baseSelector,this, SelectionKey.OP_ACCEPT)
    return object :MultithreadedSelectAcceptAble ,MultithreadedSelectAble by selectAble{
        override suspend fun accept(): SocketChannel {
            checkClosed("ClosedChannelException")
            return acceptImpl()
        }
    }
}
@Deprecated("please to use CoroutineSelector.kt")
fun SocketChannel.bindConnect(baseSelector: MultithreadedCoroutineSelector= MultithreadedSelectors.Other):MultithreadedSelectConnectAble{
    val selectAble = baseSelectHolder(baseSelector,this, SelectionKey.OP_CONNECT)
    return object :MultithreadedSelectConnectAble ,MultithreadedSelectAble by selectAble{
        override suspend fun connect(socketAddress: SocketAddress) {
            checkClosed("ClosedChannelException")
            connectImpl(socketAddress)
        }
    }
}

@Deprecated("please to use CoroutineSelector.kt")
fun SelectableChannel.bindRead(baseSelector: MultithreadedCoroutineSelector= MultithreadedSelectors.Read): MultithreadedSelectReadAble {
    if (this !is ReadableByteChannel){
        throw IllegalAccessException("type no was ReadableByteChannel!")
    }
    val selectAble =baseSelectHolder(baseSelector,this, SelectionKey.OP_READ)
    return object :MultithreadedSelectReadAble ,MultithreadedSelectAble by selectAble{
        override suspend fun read(buff: ByteBuffer): Int {
            checkClosed("ClosedChannelException")
            return readBuffImpl(buff)
        }
    }
}

@Deprecated("please to use CoroutineSelector.kt")
fun SelectableChannel.bindWrite(baseSelector: MultithreadedCoroutineSelector= MultithreadedSelectors.Write): MultithreadedSelectWriteAble {
    if (this !is WritableByteChannel){
        throw IllegalAccessException("type no was WritableByteChannel!")
    }
    val selectAble = baseSelectHolder(baseSelector,this, SelectionKey.OP_WRITE)
    return object :MultithreadedSelectWriteAble ,MultithreadedSelectAble by selectAble{
        override suspend fun write(buff: ByteBuffer): Int {
            checkClosed("ClosedChannelException")
            return writeBuffImpl(buff)
        }
    }
}

@Deprecated("please to use CoroutineSelector.kt")
fun SelectableChannel.bindReadWriteAble(read: MultithreadedCoroutineSelector= MultithreadedSelectors.Read,
                                        write:MultithreadedCoroutineSelector= MultithreadedSelectors.Write):MultithreadedSelectReadWriteAble{
    return buildReadWriteAble(bindRead(read),bindWrite(write))
}

@Deprecated("please to use CoroutineSelector.kt")
fun buildReadWriteAble(read:MultithreadedSelectReadAble,write:MultithreadedSelectWriteAble):MultithreadedSelectReadWriteAble{
    return object :MultithreadedSelectReadWriteAble{
        override val isClosed: Boolean
            get() = closeStatus
        var closeStatus=false
        override val writeAble: MultithreadedSelectWriteAble
            get() = write
        override val readAble: MultithreadedSelectReadAble
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

@Deprecated("please to use CoroutineSelector.kt")
object MultithreadedSelectors{
    @Volatile
    private var _read:MultithreadedCoroutineSelector?=null
    @Volatile
    private var _write:MultithreadedCoroutineSelector?=null
    @Volatile
    private var _othre:MultithreadedCoroutineSelector?=null

    var Read:MultithreadedCoroutineSelector
    get() {
        _read?.also {
            return it
        }
        synchronized(MultithreadedSelectors){
            if (_read == null){
                _read = MultithreadedCoroutineSelector()
            }
            return _read!!
        }
    }
    set(value) {
        _read=value
    }

    var Write:MultithreadedCoroutineSelector
        get() {
            _write?.also {
                return it
            }
            synchronized(MultithreadedSelectors){
                if (_write == null){
                    _write = MultithreadedCoroutineSelector()
                }
                return _write!!
            }
        }
        set(value) {
            _write=value
        }


    var Other:MultithreadedCoroutineSelector
        get() {
            _othre?.also {
                return it
            }
            synchronized(MultithreadedSelectors){
                if (_othre == null){
                    _othre = MultithreadedCoroutineSelector()
                }
                return _othre!!
            }
        }
        set(value) {
            _othre=value
        }
}
@Deprecated("please to use CoroutineSelector.kt")
open class MultithreadedCoroutineSelector(
    override val selector: Selector = Selector.open(),
    private val coroutineContext: CoroutineContext = NioDispatcher.IO,
    val size:Int=AVAILABLE_PROCESSORS.coerceAtLeast(MIN_SIZE)
) : MultithreadedSelector() {

    var checkTime: Long = 500L
    private var exit = false
    private var hsaStarted = false
    private var lastCheckTime = 0L
    private var selectTime=0L

    private val channelPool = Array(size){
        ReentrantObjectLock(HashMap<SelectionKey, MultithreadedSelectAble>())
    }

    private val poolSize:Int
        get(){
            var size=0
            for (i in channelPool){
                size+=i.obj.size
            }
            return size
        }



    private val clearTmp = LinkedList<SelectionKey>()

    private val threadLocal = ThreadLocal<ReentrantObjectLock<HashMap<SelectionKey, MultithreadedSelectAble>>?>()


    private fun start() {
        if (hsaStarted)
            throw Exception()
        hsaStarted=true
        GlobalScope.launch(coroutineContext) {
            try {
                while (!exit&&selector.isOpen){
                    check()
                    if (poolSize>0){
                        select()
                        sendMessage()
                    }
                    if (System.currentTimeMillis()-selectTime<waitTime){
                        yield()
                    }else{
                        delay(delayTime)
                    }
                }
            }catch (t:Throwable){
                t.printStackTrace()
            }finally {
                clear()
            }
        }
    }

    override fun close() {
        selector.close()
        exit=true
    }

    private fun registerOnTheLess(selectAble: MultithreadedSelectAble):Boolean{
        var minObj:ReentrantObjectLock<HashMap<SelectionKey, MultithreadedSelectAble>> = channelPool[MIN_SIZE-1]
        for (i in channelPool){
            if (i.obj.size<minObj.obj.size){
                minObj=i
            }
        }
        val r=minObj.tryLock()
        if (r){
            threadLocal.set(minObj)
            minObj.obj[selectAble.selectionKey] = selectAble
            minObj.unlock()
        }
        return r
    }

    override suspend fun select(selectAble: BaseSelectAble) {
        selectAble as MultithreadedSelectAble

        suspendCancellableCoroutine<MultithreadedSelectAble> sc@{
            selectAble.cont=it
            val tmp = threadLocal.get()
            tmp?.also {
                var needReturn=false
                it.tryLock({map,_->
                    map[selectAble.selectionKey]=selectAble
                    needReturn=true
                },{
                    threadLocal.set(null)
                })
                if (needReturn) {
                    return@sc
                }
            }
            if (registerOnTheLess(selectAble)){
                return@sc
            }else{
                loop@while (true){
                    for (channelMap in channelPool){
                        val locked = channelMap.tryLock()
                        if (locked){
                            threadLocal.set(channelMap)
                            channelMap.obj[selectAble.selectionKey]=selectAble
                            channelMap.unlock()
                            break@loop
                        }
                    }
                }
            }

        }
        yield()
    }

    private fun check(){
        isTimeToCheck {
            clearTmp.clear()
            for (channelMap in channelPool){
                val locked = channelMap.tryLock()
                if (locked){
                    val map = channelMap.obj
                    for ((key,value) in map){
                        if (value.isClosed) {
                            clearTmp.add(key)
                        }
                    }
                    for (i in clearTmp){
                        map[i]?.also {
                            map.remove(i)
                            it.resumeWithException(ClosedChannelException())
                        }
                    }
                    channelMap.unlock()
                }
            }
        }
    }

    private inline fun isTimeToCheck(v: () -> Unit) {
        val time = System.currentTimeMillis()
        if (time - lastCheckTime > checkTime) {
            v()
            lastCheckTime = time
        }
    }

    private fun clear(){
        for (channelMap in channelPool){
            channelMap.waitLockObject { map, _ ->
                for (i in map.values){
                    i.resumeWithException(ClosedSelectorException())
                }
                map.clear()
            }
        }
        selector.close()
    }

    private fun select(){
        selector.selectNow()
    }



    private fun sendMessage() {
        val set = selector.selectedKeys()
        clearTmp.clear()
        for (channelMap in channelPool){
            val now = System.currentTimeMillis()
            channelMap.tryLock({map,_->
                for (key in map.keys){
                    if (set.contains(key)){
                        map[key]?.also {
                            if (key.isValid){
                                clearTmp.add(key)
                            }else{
                                set.remove(key)
                            }
                        }
                    }
                }
                for (i in clearTmp){
                    map[i]?.also {
                        selectTime=now
                        map.remove(i)
                        set.remove(i)
                        it.resume()
                    }
                }
            })
        }
    }

    init {
        if (size<1){
            throw Exception("length size can't less than 1")
        }
        start()
    }

}