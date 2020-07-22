package com.mt.mtuser.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

/**
 * @apiDefine Notify
 * @apiParam {Integer} id
 * @apiParam {String} content 内容
 * @apiParam {String} title 标题
 * @apiParam {String} sendType 发送类型 mass：群发或 assign：指定
 * @apiParam {String} msgType 消息类型 notify：通知或 announce：公告
 * @apiParam {String} [status] 消息状态 progress：发送中或 cancel：取消发送
 * @apiParam {List} idList 指定发送的接收者id
 */
@Table("mt_notify")
class Notify {
    @Id
    var id: Int? = null

    /*** 消息内容*/
    var content: String? = null

    /*** 消息标题*/
    var title: String? = null

    /*** 发送者id*/
    var sendId: Int? = null

    /*** 发送类型 mass：群发或 assign：指定*/
    var sendType: String? = null

    /*** 消息状态 progress：发送中或 cancel：取消发送*/
    var status: String? = null

    /** 消息类型 notify：通知 announce：公告 */
    var msgType: String? = null

    /*** 创建时间*/
    var createTime: LocalDateTime? = null

    @Transient
    var idList: List<Int>? = null

    @Transient
    var readStatus: String? = null

    companion object {
        const val statusProgress = "progress"
        const val statusCancel = "cancel"
        const val sendTypeMass = "mass"
        const val sendTypeAssign = "assign"
    }
}