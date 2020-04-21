package com.mt.mtgateway.bean

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.format.annotation.DateTimeFormat
import java.util.*

/**
 * Created by gyh on 2020/3/15.
 */
data class User(
        var id: Int? = null,
        var phone: String? = null,
        var nickName: String? = null,
        var idNum: String? = null,
        var password: String? = null,
        var userPhoto: String? = null,
        var roles: Collection<Role> = mutableListOf(),
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:SS")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        var createTime: Date? = null,
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        var lastTime: Date? = null,
        var toke: String? = null
)