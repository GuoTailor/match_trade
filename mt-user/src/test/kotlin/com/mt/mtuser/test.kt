package com.mt.mtuser

/**
 * Created by gyh on 2020/3/26.
 */

import com.mt.mtcommon.firstDay
import com.mt.mtcommon.lastDay
import com.mt.mtcommon.toEpochMilli
import com.mt.mtcommon.toLocalDateTime
import com.mt.mtuser.common.Util
import com.mt.mtuser.entity.page.PageQuery
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.FileOutputStream
import java.lang.StringBuilder
import java.math.BigDecimal
import java.security.MessageDigest
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.system.measureTimeMillis


fun main1() = runBlocking<Unit> {
    val time = measureTimeMillis {
        println("The answer is ${concurrentSum()}")
    }
    println("Completed in $time ms")
    println(Thread.currentThread().name)
}

suspend fun concurrentSum(): Int = coroutineScope {
    val one = async { doSomethingUsefulOne() }
    val two = async { doSomethingUsefulTwo() }
    println(one.await())
    one.await() + two.await()
}

suspend fun doSomethingUsefulOne(): Int {
    delay(1000L) // 假设我们在这里做了些有用的事
    println(Thread.currentThread().name)
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1000L) // 假设我们在这里也做了些有用的事
    println(Thread.currentThread().name)
    return 29
}

fun main2() = runBlocking<Unit> {
    launch { // 运行在父协程的上下文中，即 runBlocking 主协程
        println("main runBlocking      : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(Dispatchers.Unconfined) { // 不受限的——将工作在主线程中
        println("Unconfined            : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(Dispatchers.Default) { // 将会获取默认调度器
        println("Default               : I'm working in thread ${Thread.currentThread().name}")
    }
    launch(newSingleThreadContext("MyOwnThread")) { // 将使它获得一个新的线程
        println("newSingleThreadContext: I'm working in thread ${Thread.currentThread().name}")
    }
    GlobalScope.launch {
        println("GlobalScope               : I'm working in thread ${Thread.currentThread().name}")
    }
}

fun foo(): Flow<Int> = flow { // 流构建器
    for (i in 1..3) {
        delay(100) // 假装我们在这里做了一些有用的事情
        println("nmka")
        emit(i) // 发送下一个值
    }
}

fun main3() = runBlocking<Unit> {
    val time = measureTimeMillis {
        val buf = foo().buffer() // 缓冲发射项，无需等待
        buf.collect { value ->
            delay(300) // 假装我们花费 300 毫秒来处理它
            println(value)
        }
        buf.collect { value ->
            delay(300) // 假装我们花费 300 毫秒来处理它
            println(value)
        }
    }
    println("Collected in $time ms")
}

fun main4() {
    val time = System.currentTimeMillis()
    val c = Calendar.getInstance()
    c.timeInMillis = Util.encoderDate("2020-06-9 15:00:00")
    c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) / 15 * 15)    // 15的倍数
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)

    println(c.timeInMillis)
    println(c.get(Calendar.MINUTE) % 15 == 0)
    println(Date(c.timeInMillis))

    for (i in 0..5 step 2) {
        println(i)
    }

    println()
    val date = Date(1591340280000)
    println(date)
    println(Date(1589504123171))
}

// 模仿事件流
fun events(): Flow<Int> = (1..3).asFlow().onEach { delay(100) }

fun main5() = runBlocking {
    events().collect { event ->
        launch(Dispatchers.Default) {
            delay(1000)
            println("Event: $event ${Thread.currentThread().name}")
        }
    }
    println("Done")
}

fun main6() = runBlocking {
    val list = events().toList()
    val count = CountDownLatch(list.size)
    list.forEach { event ->
        launch(Dispatchers.Default) {
            delay(1000)
            println("Event: $event ${Thread.currentThread().name}")
            count.countDown()
        }
    }
    println("Done")
    count.await()
    println("Done")
}

fun main7() {
    val page = PageQuery(pageNum = 10, pageSize = 10)
    println(page.where().toString())
    println(page.toPageSql())
    println(BigDecimal(5.50000001).divide(BigDecimal(45.5000000003), 4, BigDecimal.ROUND_HALF_EVEN).toPlainString())
    println(BigDecimal("0.0").compareTo(BigDecimal(0)) == 0)
    println(Date(1594288965561))
    println(Date(1594288965561).before(Date(1594288965570)))
}

fun main8() {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")
    val formattedDateTime = LocalDateTime.parse("2020-07-17 00:00:00:008", formatter)
    println(formattedDateTime)
    println(LocalDate.now().atStartOfDay())

    val c = LocalDateTime.now()
    println(c)
    val m = c.withMinute(0)
    println(m)
    println(m.withMinute(0)
            .withSecond(0)
            .withNano(0))
    println(Date(1595394000000))
}

fun testTime() {
    println(firstDay())
    println(lastDay())

}

fun main9() {
    println(BigDecimal(0) == BigDecimal.ZERO)
    println(BigDecimal("0.0") == BigDecimal.ZERO)
    println(BigDecimal("0.0") == BigDecimal(0))
}

fun main10() {
    val m = MessageDigest.getInstance("MD5")
    val m2 = MessageDigest.getInstance("MD5")
    m.update("nmka2".toByteArray())
    m2.update("nmka".toByteArray())
    val sb = StringBuilder()
    m.digest().forEach { sb.append(it) }
    println(sb)
    sb.clear()
    m2.digest().forEach { sb.append(it) }
    println(sb)
}

fun main() = main8()
