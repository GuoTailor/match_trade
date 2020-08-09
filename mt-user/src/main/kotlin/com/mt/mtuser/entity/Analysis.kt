package com.mt.mtuser.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

/**
 * @apiDefine Analysis
 * @apiParam {Integer} id
 * @apiParam {Integer} userId 分析员id
 * @apiParam {Integer} companyId 公司id
 * @apiParam {String} title 标题
 * @apiParam {String} content 内容
 * @apiParam {String} type 报告类型 1：周报，2：月报
 * @apiParam {String} time 时间点
 * @apiParam {Date} createTime 报告提交时间
 */
@Table("mt_analysis")
class Analysis {
    @Id
    var id: Int? = null

    /*** 分析员id */
    var userId: Int? = null

    /*** 公司id */
    var companyId: Int? = null

    /*** 内容 */
    var content: String? = null

    /*** 报告类型 1：周报，2：月报 */
    var type: String? = null

    /*** 时间点 */
    var time: String? = null

    /*** 报告提交时间 */
    var createTime: LocalDateTime? = null

    /*** 标题 */
    var title: String? = null

    @Transient
    var companyName: String? = null
}