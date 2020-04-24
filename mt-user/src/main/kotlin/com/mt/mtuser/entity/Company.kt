package com.mt.mtuser.entity

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.format.annotation.DateTimeFormat
import java.util.*

/**
 * Created by gyh on 2020/3/18.
 * @apiDefine Company
 * @apiParam {Integer} id 公司id
 * @apiParam {String} name 公司名字
 * @apiParam {Integer} roomCount 房间数量
 * @apiParam {String} mode 竞价模式{1：点选、2： 点选+定时、3：定时 +点选+两两撮合、4：全部}
 * @apiParam {String} licenseUrl 营业执照图片地址
 * @apiParam {String} creditUnionCode 统一信用社代码
 * @apiParam {String} legalPerson 企业法人
 * @apiParam {String} unitAddress 单位地址
 * @apiParam {String} unitContactName 单位联系人姓名
 * @apiParam {String} unitContactPhone 单位联系人电话
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
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        var createTime: Date? = null,
        /*** 营业执照图片地址*/
        val licenseUrl: String? = null,
        /*** 统一信用社代码*/
        val creditUnionCode: String? = null,
        /*** 企业法人*/
        val legalPerson: String? = null,
        /*** 单位地址*/
        val unitAddress: String? = null,
        /*** 单位联系人姓名*/
        val unitContactName: String? = null,
        /*** 单位联系人电话*/
        val unitContactPhone: String? = null
)