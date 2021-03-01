# Kotlin-Nio
[![](https://www.jitpack.io/v/lesa1127/Kotlin-Nio.svg)](https://www.jitpack.io/#lesa1127/Kotlin-Nio)

**Kotlin-Nio it was an IO library base on kotlin coroutine**

​	The lib only support JVM-Kotlin



#### Demo


```kotlin
fun main(){
    runBlockingIOScope  {
        val server= ServerSocketChannel.open()
        server.configureBlocking(false)
        server.bind( InetSocketAddress(80))
        val serverHandle=server.bindAccept()
        while (true){
            try {
                val client = serverHandle.accept()
                client.configureBlocking(false)
                read(client.bindRead().buildReadAbleStream(),
                    client.bindWrite().buildWriteAbleStream())
            }catch (t:Throwable){
                t.printStackTrace()
            }
        }
    }
}


fun read(readAble: ReadAbleStream,writeAble: WriteAbleStream){
    val lineReader=LineReader(readAble.bufferedReadAbleStream())
    val out = writeAble.bufferedWriteAbleStream()
    GlobalScope.launchIOScope {
        try {
            var txt=""
            while (true) {
                lineReader.readline().also { txt = it }
                if (txt==""){
                    break
                }
                println("Content:$txt")
            }

            out.writeFully("HTTP/1.1 200 OK\r\n".toByteArray())
            out.writeFully("Content-type:text/html\r\n".toByteArray())

            out.writeFully("\r\n".toByteArray())

            out.writeFully("<h1>successful</h1>\r\n".toByteArray())
            out.flush()

        }catch (t:Throwable){
            t.printStackTrace()
        } finally {
            readAble.close()
            writeAble.close()
        }
    }
}
```

### How to use it
#### Step 1. Add the JitPack repository to your build file

```groovy
allprojects {
    repositories {
        //other ...
        maven { url 'https://www.jitpack.io' }
    }
}
```

#### Step 2. Add the dependency

```groovy
dependencies {
    implementation 'com.github.lesa1127:Kotlin-Nio:0.0.3'
}
```

