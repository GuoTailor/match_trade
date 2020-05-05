package com.mt.mtengine

/**
 * Created by gyh on 2020/5/5.
 */
abstract class KotlinTest {
    abstract val flag: String

    fun testSync() {
        synchronized(this) {
            println("start $flag")
            Thread.sleep(1000)
            println("end $flag")
        }
    }
}

class ATest(override val flag: String = "A") : KotlinTest()
class BTest(override val flag: String = "B") : KotlinTest()

fun main() {
    val aTest = ATest()
    val ta2 = Thread{ aTest.testSync() }
    val ta1 = Thread{ aTest.testSync() }
    val tb = Thread{ BTest().testSync() }
    ta1.start()
    ta2.start()
    tb.start()
    ta1.join()
    ta2.join()
    tb.join()
}