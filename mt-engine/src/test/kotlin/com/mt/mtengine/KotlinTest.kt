package com.mt.mtengine

/**
 * Created by gyh on 2020/5/5.
 */

fun main() {
    val t = testNull(3)?.let { "$it---" } ?: "nu"
    println(t)
}

fun testNull(i : Int): String? {
    return if (i > 2) {
        null
    } else i.toString()
}