package com.mt.mtgateway.bean

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.springframework.data.relational.core.mapping.Table
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

/**
 * Created by gyh on 2020/3/15.
 */
@Table("mt_user")
open class User(
        var id: Int? = null,
        var phone: String? = null,
        var nickName: String? = null,
        var idNum: String? = null,
        var password: String? = null,
        var userPhoto: String? = null,
        var roles: Collection<Role> = mutableListOf(),
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:SS")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        @JsonDeserialize(using = LocalDateTimeDeserializer::class)
        @JsonSerialize(using = LocalDateTimeSerializer::class)
        var createTime: LocalDateTime? = null,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        @JsonDeserialize(using = LocalDateTimeDeserializer::class)
        @JsonSerialize(using = LocalDateTimeSerializer::class)
        var lastTime: LocalDateTime? = null,
        var toke: String? = null
)