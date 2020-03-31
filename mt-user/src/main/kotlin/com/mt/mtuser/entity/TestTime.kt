package com.mt.mtuser.entity

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

/**
 * Created by gyh on 2020/3/31.
 */
@Table("test_time")
class TestTime(
        @Id
        var id: Int? = null,
        var time: LocalTime? = null,
        var timestamp: LocalDate? = null
) {
}