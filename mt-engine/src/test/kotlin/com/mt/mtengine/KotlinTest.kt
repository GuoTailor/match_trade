package com.mt.mtengine

import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Created by gyh on 2020/5/5.
 */
abstract class KotlinTest {
    abstract val flag: String

    fun testSync(lock: String) {
        synchronized(lock) {
            println("start $flag $lock")
            Thread.sleep(1000)
            println("end $flag  $lock")
        }
    }
}

class ATest(override val flag: String = "A", val nmka: String = "nmka") : KotlinTest() {
    fun persion(): KotlinTest {
        return this
    }
}

class BTest(override val flag: String = "B") : KotlinTest()

fun main() {
    val aTest = ATest()
    val om = ObjectMapper().writeValueAsString(aTest.persion())
    println(om)
    val ta1 = Thread { aTest.testSync(121.toString()) }
    val tb = Thread { BTest().testSync(121.toString()) }
    ta1.start()
    tb.start()
    ta1.join()
    tb.join()
}