package com.mt.mtengine.entity.room

import com.mt.mtcommon.RoomEnum
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import javax.validation.constraints.Null

/**
 * Created by gyh on 2020/3/23.
 * 抬杠撮合
 * @apiDefine BickerMatch
 * @apiParam {Integer} companyId 公司id
 * @apiParam {Integer} stockId 股票id
 * @apiParam {String} name 房间名字
 * @apiParam {Date} quoteTime 报价和选择身份时间 格式：HH:mm:SS
 * @apiParam {Date} secondStage 第二阶段时间 格式：HH:mm:SS
 * @apiParam {String} time 时长 格式：HH:mm:SS
 * @apiParam {String} startTime: 房间开启时间 格式：HH:mm:SS
 * @apiParam {Int} numberTrades 单笔交易数量
 * @apiParam {Int} count 撮合次数
 * @apiParam {Double} lowScope 报价最低值
 * @apiParam {Double} highScope 报价最高值
 * @apiParam {Integer} rival 选择的对手上限
 * @apiParam {String} enable 是否开启（0：关闭，1：开启）
 */
@Table("mt_room_bicker")
class BickerMatch(
        @Id
        override var roomId: String? = null,    // 房间id，四张房间表唯一
        override var companyId: Int? = null,    // 公司id
        override var stockId: Int? = null,      // 股票id
        override var name: String? = null,      // 房间名字
        @get:Null(message = "不能设置房间的当前人数")
        @set:Null(message = "不能设置房间的当前人数")
        override var people: Int? = null,       // 人数
        override var time: LocalTime? = null,    // 时长
        override var startTime: LocalTime? = null,     // 房间开启时间
        override var numberTrades: Int? = null, // 单笔交易数量
        override var lowScope: Double? = null,  // 报价最低值
        override var highScope: Double? = null, // 报价最高值
        override var enable: String? = null,    // 是否开启（0：关闭，1：开启）
        override var createTime: LocalDateTime? = null   // 创建时间
) : BaseRoom {
    override val flag: String = RoomEnum.BICKER.mode

    override fun validNull() {
        people = null
    }

    override fun toString(): String {
        return "ClickMatch(roomId=$roomId, companyId=$companyId, stockId=$stockId, name=$name, people=$people, time=$time, " +
                "numberTrades=$numberTrades, lowScope=$lowScope, highScope=$highScope, enable=$enable, createTime=$createTime, flag='$flag')"
    }
}