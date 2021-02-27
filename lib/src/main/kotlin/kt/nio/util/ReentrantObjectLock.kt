package kt.nio.util

import java.util.concurrent.locks.ReentrantLock

class ReentrantObjectLock<T>(var obj: T):ReentrantLock() {

    inline fun waitLockObject(crossinline fu:(T, ReentrantObjectLock<T>)->Unit){
        lockInterruptibly()
        try {
            fu(obj, this)
        }finally {
            unlock()
        }

    }

    fun lockObject():T?{
        if (tryLock()){
            return obj
        }
        return null
    }


    fun waitLockObject():T{
        lockInterruptibly()
        return obj
    }


    inline fun unLockAweak(crossinline fu:() -> Unit){
        unlock()
        try {
            fu()
        }finally {
            lock()
        }
    }

    inline fun tryLock(crossinline success: (T, ReentrantObjectLock<T>)->Unit,crossinline fail:()->Unit={}){
        if (tryLock()){
            try {
                success(obj, this)
            }finally {
                unlock()
            }
        }else{
            fail()
        }
    }
}