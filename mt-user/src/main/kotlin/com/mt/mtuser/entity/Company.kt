package com.mt.mtuser.entity

import org.springframework.data.annotation.Transient
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.format.annotation.DateTimeFormat
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Created by gyh on 2020/3/18.
 * @apiDefine Company
 * @apiParam {Integer} id 公司id
 * @apiParam {String} name 公司名字
 * @apiParam {Integer} roomCount 房间数量
 * @apiParam {List} modes 房间模式 列子：["C","B","D","E","I"] 分布对应 点选，抬杠，两两，连续，定时
 * @apiParam {String} licenseUrl 营业执照图片地址
 * @apiParam {String} creditUnionCode 统一信用社代码
 * @apiParam {String} legalPerson 企业法人
 * @apiParam {String} unitAddress 单位地址
 * @apiParam {String} unitContactName 单位联系人姓名
 * @apiParam {String} unitContactPhone 单位联系人电话
 * @apiParam {String} [brief] 公司简介
 */
@Table("mt_company")
data class Company(
        @Id
        var id: Int? = null,
        /*** 公司名*/
        var name: String? = null,
        /*** 房间数量*/
        var roomCount: Int? = null,
        /*** 竞价模式{2：点选、3： 点选+定时、4：及时 +点选+定时}*/
        var mode: String? = null,
        /*** 注册时间*/
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:SS")
        var createTime: LocalDateTime? = null,
        /*** 营业执照图片地址*/
        var licenseUrl: String? = null,
        /*** 统一信用社代码*/
        var creditUnionCode: String? = null,
        /*** 企业法人*/
        var legalPerson: String? = null,
        /*** 单位地址*/
        var unitAddress: String? = null,
        /*** 单位联系人姓名*/
        var unitContactName: String? = null,
        /*** 单位联系人电话*/
        var unitContactPhone: String? = null
) {
    @Transient
    var stock: Long? = null
    @Transient
    var money: BigDecimal? = null
    @Transient
    var brief: String? = null
    @Transient
    var analystId: Int? = null
    @Transient
    var analystName: String? = null
    @Transient
    var analystPhone: String? = null

    fun getModes(): List<String> {
        return ObjectMapper().readValue(mode ?: "[]")
    }

    fun setModes(modes: List<String>) {
        mode = ObjectMapper().writeValueAsString(modes)
    }
}