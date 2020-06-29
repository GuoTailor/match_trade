package com.mt.mtuser.entity.room

import org.springframework.data.relational.core.mapping.Table
import java.time.LocalTime
import java.util.*
import com.mt.mtcommon.RoomEnum
import org.springframework.data.annotation.Id
import java.time.LocalDateTime

/**
 * Created by gyh on 2020/3/23.
 * 点选撮合
 * @apiDefine ClickMatch
 * @apiParam {Integer} companyId 公司id
 * @apiParam {Integer} stockId 股票id
 * @apiParam {String} name 房间名字
 * @apiParam {Date} quoteTime 报价和选择身份时间 格式：HH:mm:SS
 * @apiParam {Date} secondStage 第二阶段时间 格式：HH:mm:SS
 * @apiParam {String} time 时长 格式：HH:mm:SS
 * @apiParam {String} startTime: 房间开启时间 格式：HH:mm:SS
 * @apiParam {Int} numberTrades 单笔交易数量
 * @apiParam {Double} lowScope 报价最低值
 * @apiParam {Double} highScope 报价最高值
 * @apiParam {Integer} rival 选择的对手上限
 * @apiParam {String} [oldFlag]=C 旧房间的标识符
 */
@Table("mt_room_click")
class ClickMatch(
        @Id
        override var roomId: String? = null,    // 房间id，四张房间表唯一
        override var companyId: Int? = null,    // 公司id
        override var stockId: Int? = null,      // 股票id
        override var name: String? = null,      // 房间名字
        override var people: Int? = null,       // 人数
        var quoteTime: LocalTime? = null,            // 报价和选择身份时间
        var secondStage: LocalTime? = null,          // 第二阶段时间
        override var time: LocalTime? = null,    // 时长
        override var startTime: LocalTime? = null,     // 房间开启时间
        override var numberTrades: Int? = null, // 单笔交易数量
        override var lowScope: Double? = null,  // 报价最低值
        override var highScope: Double? = null, // 报价最高值
        override var enable: String? = null,    // 是否开启（0：关闭，1：开启）
        override var createTime: LocalDateTime? = null,  // 创建时间
        var rival: Int? = null                 // 选择的对手上限
) : BaseRoom {
    override val flag: String = RoomEnum.CLICK.mode
    @org.springframework.data.annotation.Transient
    var oldFlag: String? = null             // 标识符,更改房间时用

    override suspend fun validNull() {
        people = null
    }

    override fun toString(): String {
        return "ClickMatch(roomId=$roomId, companyId=$companyId, stockId=$stockId, name=$name, people=$people, " +
                "quoteTime=$quoteTime, secondStage=$secondStage, time=$time, numberTrades=$numberTrades, lowScope=$lowScope, " +
                "highScope=$highScope, enable=$enable, createTime=$createTime, rival=$rival, flag='$flag')"
    }
}