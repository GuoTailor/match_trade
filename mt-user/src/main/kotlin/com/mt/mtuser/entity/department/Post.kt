package com.mt.mtuser.entity.department

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("mt_post")
class Post(
        /**
         *
         */
        @Id
        var id: Int? = null,

        /**
         * 职位名
         */
        var name: String? = null,

        /**
         * 创建时间
         */
        var createTime: LocalDateTime? = null

)