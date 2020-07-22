package com.mt.mtcommon

import java.time.*
import java.time.temporal.TemporalAdjusters

/**
 * Created by gyh on 2020/3/25.
 * 所有的扩展函数和运算符的重载都统一放到该文件
 */

fun Long.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

fun LocalDateTime.toEpochMilli(): Long = this.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun LocalTime.toLocalDateTime(): LocalDateTime = LocalDateTime.of(LocalDate.now(), this)

/**
 * 返回此LocalDateTime的副本，并减去指定的[localTime]。
 * 此实例是不可变的，不受此方法调用的影响。
 */
fun LocalDateTime.minusLocalTime(localTime: LocalTime): LocalDateTime =
        this.minusHours(localTime.hour.toLong())
                .minusMinutes(localTime.minute.toLong())
                .minusSeconds(localTime.second.toLong())
                .minusNanos(localTime.nano.toLong())


/**
 * 返回此LocalDateTime的副本，并加上指定的[localTime]。
 * 此实例是不可变的，不受此方法调用的影响。
 */
fun LocalDateTime.plusLocalTime(localTime: LocalTime): LocalDateTime =
        this.plusHours(localTime.hour.toLong())
                .plusMinutes(localTime.minute.toLong())
                .plusSeconds(localTime.second.toLong())
                .plusNanos(localTime.nano.toLong())


/**
 * 返回此LocalDateTime的副本，并减去指定的毫秒数。
 * 此实例是不可变的，不受此方法调用的影响。
 */
fun LocalDateTime.minusMilli(milli: Long): LocalDateTime = this.minusNanos(milli * 1000_000)

/**
 * 获取本月的第一天
 */
fun firstDay(): LocalDateTime = LocalDate.now().withDayOfMonth(1).atStartOfDay()

/**
 * 获取本月的最后一天
 */
fun lastDay(): LocalDateTime = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX)

/**
 * 返回LocalTime表示的毫秒值,注意可能的精度损失
 */
fun LocalTime.toMillisOfDay(): Long = this.toNanoOfDay() / 1000_000

/**
 * 把LocalTime转换为Duration
 */
fun LocalTime?.toDuration(): Duration = if (this == null) Duration.ZERO else Duration.ofNanos(this.toNanoOfDay())

/**
 * LocalTime 加 运算符
 */
operator fun LocalTime.plus(other: LocalTime): LocalTime {
    return if (this.toNanoOfDay() + other.toNanoOfDay() > LocalTime.MAX.toNanoOfDay()) {
        LocalTime.MAX
    } else this.plusNanos(other.toNanoOfDay())
}
