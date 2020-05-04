package com.mt.mtcommon

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
fun LocalTime.toMillisOfDay(): Long = this.toNanoOfDay() / 1000_000

/**
 * 吧LocalTime转换为Duration
 */
fun LocalTime.toDuration(): Duration = Duration.ofNanos(this.toNanoOfDay())

/**
 * 判断现在加上指定时间之后是否超过今天的最大时间。
 * 今天最大时间定义：'23:59:59.999999999'这是一天结束前的午夜时间。
 * @return Boolean true:超过今天最大时间； false: 没有超过
 */
fun isAfterToday(time: LocalTime) = time.toNanoOfDay() + LocalTime.now().toNanoOfDay() > LocalTime.MAX.toNanoOfDay()

/**
 * LocalTime 加 运算符
 */
operator fun LocalTime.plus(other: LocalTime): LocalTime {
    return this.plusNanos(other.toNanoOfDay())
}

/**
 * 重载Date的[减]运算符
 */
operator fun Date.minus(startTime: Date): Long {
    return this.time - startTime.time
}