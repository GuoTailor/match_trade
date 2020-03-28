package com.mt.mtuser.entity.room

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.format.annotation.DateTimeFormat
import java.time.Duration
import java.util.*
import javax.validation.constraints.Null

/**
 * Created by gyh on 2020/3/23.
 * 点选撮合
 * @apiDefine ClickMatch
 * @apiParam {Integer} companyId 公司id
 * @apiParam {Integer} stockId 股票id
 * @apiParam {String} name 房间名字
 * @apiParam {Date} quoteTime 报价和选择身份时间 格式：yyyy-MM-dd HH:mm:SS
 * @apiParam {Date} secondStage 第二阶段时间 格式：yyyy-MM-dd HH:mm:SS
 * @apiParam {String} time 时长 格式：HH:mm:SS
 * @apiParam {Int} numberTrades 单笔交易数量
 * @apiParam {Int} count 撮合次数
 * @apiParam {Double} lowScope 报价最低值
 * @apiParam {Double} highScope 报价最高值
 * @apiParam {Integer} rival 选择的对手上限
 * @apiParam {String} enable 是否开启（0：关闭，1：开启）
 */
@Table("mt_room_click")
class ClickMatch(
        @Id
        override var id: Int? = null,
        override var roomNumber: String? = null,     // 房间号
        override var companyId: Int? = null,         // 公司id
        override var stockId: Int? = null,           // 股票id
        override var name: String? = null,           // 房间名字
        @Null(message = "不能设置房间的当前人数")
        override var people: Int? = null,            // 人数
        var quoteTime: Date? = null,        // 报价和选择身份时间
        var secondStage: Date? = null,      // 第二阶段时间
        @DateTimeFormat(pattern = "HH:mm:SS")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "GMT+8")
        override var time: Duration? = null,          // 时长
        override var numberTrades: Int? = null,      // 单笔交易数量
        var count: Int? = null,             // 撮合次数
        @Null(message = "不能设置当前撮合次数")
        var currentCount: Int? = null,      // 当前撮合次数
        override var lowScope: Double? = null,       // 报价最低值
        override var highScope: Double? = null,      // 报价最高值
        override var enable: String? = null,         // 是否开启（0：关闭，1：开启）
        override var createTime: Date? = null,       // 创建时间
        var rival: Int? = null,                 // 选择的对手上限
        override var flag: String = "C"
) : BaseRoom