package com.mt.mtengine

import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/5/5.
 */

fun testMono() {
    val mo = Mono.just("")
            .filter { it.length > 2 }
            .map { "123" }
            .thenReturn("---")
            .doOnSuccess { println(it) }
            .doOnEach { println(it) }
    println(mo.block())
}

fun main() {
    println(add(2)(3))
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