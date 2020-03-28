package com.mt.mtuser.common

import java.sql.Time
import java.time.Duration
import java.util.*

/**
 * Created by gyh on 2020/3/25.
 */


/**
 * 把long转Date，long是毫秒值
 */
fun Long.toDate(): Date = Date(this)

fun Time.toDuration(): Duration = Duration.ofMillis(this.time)