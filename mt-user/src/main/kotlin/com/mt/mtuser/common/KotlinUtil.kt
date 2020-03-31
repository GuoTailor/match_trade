package com.mt.mtuser.common

import java.sql.Time
import java.time.Duration
import java.time.LocalTime
import java.util.*

/**
 * Created by gyh on 2020/3/25.
 * 所有的扩展函数和运算符的重载都统一放到该文件
 */


/**
 * 把long转Date，long是毫秒值
 */
fun Long.toDate(): Date = Date(this)

/**
 * 返回LocalTime表示的毫秒值,注意可能的精度损失
 */
fun LocalTime.toMillis(): Long = this.toNanoOfDay() / 1000_000

/**
 * 吧LocalTime转换为Duration
 */
fun LocalTime.toDuration(): Duration = Duration.ofNanos(this.toNanoOfDay())

/**
 * 重载Date的[减]运算符
 */
operator fun Date.minus(startTime: Date): Long {
    return this.time - startTime.time
}