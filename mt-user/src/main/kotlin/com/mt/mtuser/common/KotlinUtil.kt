package com.mt.mtuser.common

import java.sql.Time
import java.time.Duration
import java.util.*

/**
 * Created by gyh on 2020/3/25.
 * 所有的扩展函数和运算符的重载都统一放到该文件
 */


/**
 * 把long转Date，long是毫秒值
 */
fun Long.toDate(): Date = Date(this)

fun Time.toDuration(): Duration = Duration.ofMillis(this.time)

/**
 * 重载Date的[减]运算符
 */
operator fun Date.minus(startTime: Date): Long {
    return this.time - startTime.time
}