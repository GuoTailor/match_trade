package com.mt.mtuser.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

@Table("mt_notify_user")
class NotifyUser() {
    @Id
    var id: Int? = null

    /*** 用户id*/
    var userId: Int? = null

    /*** 读取时间*/
    var readTime: LocalDateTime? = null

    /*** 消息id*/
    var msgId: Int? = null

    /*** 状态，read已读| unread未读*/
    var status: String? = null

    constructor(userId: Int, msgId: Int) : this() {
        this.userId = userId
        this.msgId = msgId
        status = unread
    }

    companion object {
        const val read = "read"
        const val unread = "unread"
    }
}