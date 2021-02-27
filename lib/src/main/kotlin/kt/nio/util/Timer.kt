package kt.nio.util

import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.*
import kotlin.coroutines.CoroutineContext

open class Timer(var cycle:Long=300,
                 private val coroutineContext: CoroutineContext = Dispatchers.Default){

    protected var job: Job?=null
    protected var exit = false
    protected val listTask = ReentrantObjectLock(LinkedList<TimerTask>())

    companion object{
        private var timer:Timer?=null
        fun getTimer():Timer{
            timer?.also {
                return it
            }
            synchronized(Timer::class){
                if (timer==null)
                    timer=Timer()
                return timer!!
            }
        }
    }

    fun start(){
        if (job==null) {
            job = GlobalScope.launch(coroutineContext) {
                loop()
            }
        }
    }

    protected suspend fun loop(){
        val removelist=LinkedList<TimerTask>()
        while (!exit){

            listTask.waitLockObject { linkedList, _ ->
                val now = System.currentTimeMillis()
                for (i in linkedList){
                    if (i.cancel){
                        removelist.add(i)
                        continue
                    }
                    if (i.state==TimerTask.SCHEDULED) {
                        if (now > i.ontime) {
                            try {
                                i.run()
                            }catch (t:Throwable){
                                t.printStackTrace()
                            }finally {
                                i.state=TimerTask.EXECUTED
                                removelist.add(i)
                            }
                        }
                    }
                }

                for (i in removelist){
                    linkedList.remove(i)
                }
                removelist.clear()
            }
            delay(cycle)
        }
    }

    fun schedule(task: TimerTask){
        listTask.waitLockObject { linkedList, _ ->
            task.state=TimerTask.SCHEDULED
            linkedList.add(task)
        }
    }

    fun schedule(ontime: Long, runIt: () -> Unit): TimerTask {
        return object : TimerTask(ontime) {
            init {
                state=SCHEDULED
            }
            override fun run() {
                runIt()
            }
        }
    }

    fun close(){
        exit=true
    }
}

abstract class TimerTask(var ontime:Long):Runnable{
    /**
     * The state of this task, chosen from the constants below.
     */
    var state = VIRGIN

    companion object{
        /**
         * This task has not yet been scheduled.
         */
        val VIRGIN = 0

        /**
         * This task is scheduled for execution.  If it is a non-repeating task,
         * it has not yet been executed.
         */
        val SCHEDULED = 1

        /**
         * This non-repeating task has already executed (or is currently
         * executing) and has not been cancelled.
         */
        val EXECUTED = 2


    }
    @Volatile
    var cancel=false

    fun cancle(){
        cancel=true
    }

}