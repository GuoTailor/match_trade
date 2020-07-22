package com.mt.mtuser.entity.department

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

@Table("mt_department")
class Department(

        @Id
        var id: Int? = null,

        /**
         * 部门名
         */
        var name: String? = null,

        /**
         * 创建时间
         */
        var createTime: LocalDateTime? = null

) {

    @Transient
    var postList: MutableList<Post> = ArrayList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Department

        if (id != other.id) return false
        if (name != other.name) return false
        if (createTime != other.createTime) return false
        if (postList != other.postList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (createTime?.hashCode() ?: 0)
        result = 31 * result + postList.hashCode()
        return result
    }
}