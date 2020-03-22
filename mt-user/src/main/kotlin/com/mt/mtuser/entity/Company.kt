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
 * @apiParam {String} mode 竞价模式{2：点选、3： 点选+定时、4：及时 +点选+定时}
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
        var createTime: Date? = null

)