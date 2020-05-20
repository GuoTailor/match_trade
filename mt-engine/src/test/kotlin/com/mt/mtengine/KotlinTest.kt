package com.mt.mtengine

/**
 * Created by gyh on 2020/5/5.
 */

fun main() {
    val t = testNull(3)?.let { "$it---" } ?: "nu"
    println(t)
}

fun testNull(i: Int): String? {
    println(add(2)(3))
    return if (i > 2) {
        null
    } else i.toString()
}

fun add(x: Int): (Int) -> Int {
    return fun(y): Int { return x + y }
}
