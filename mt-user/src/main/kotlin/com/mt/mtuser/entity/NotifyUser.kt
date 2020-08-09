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

    /*** 1:真消息，2:分析报告*/
    var msgType: String? = null

    constructor(userId: Int, msgId: Int, msgType: String = typeMsg) : this() {
        this.userId = userId
        this.msgId = msgId
        this.msgType = msgType
        status = unread
    }

    companion object {
        const val read = "read"
        const val unread = "unread"

        const val typeMsg = "1"
        const val typeAnalysis = "2"
    }
}