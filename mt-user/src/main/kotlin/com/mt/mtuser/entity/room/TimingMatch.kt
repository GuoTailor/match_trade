package com.mt.mtuser.entity.room

import com.mt.mtuser.service.room.RoomEnum
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalTime
import java.util.*

/**
 * Created by gyh on 2020/3/23.
 * 定时撮合
 * @apiDefine TimingMatch
 * @apiParam {Integer} companyId 公司id
 * @apiParam {Integer} stockId 股票id
 * @apiParam {String} name 房间名字
 * @apiParam {String} time 时长 格式：HH:mm:SS
 * @apiParam {String} matchTime 撮合时间 格式：HH:mm:SS
 * @apiParam {String} quoteTime 报价时间 格式：HH:mm:SS
 * @apiParam {Integer} numberTrades 单笔交易数量
 * @apiParam {Integer} count 撮合次数
 * @apiParam {Double} lowScope 报价最低值
 * @apiParam {Double} highScope 报价最高值
 * @apiParam {String} enable 是否开启（0：关闭，1：开启）
 */
@Table("mt_room_timing")
class TimingMatch(
        @Id
        override var roomId: String? = null,    // 房间id，四张房间表唯一
        override var companyId: Int? = null,    // 公司id
        override var stockId: Int? = null,      // 股票id
        override var name: String? = null,      // 房间名字
        override var people: Int? = null,       // 人数
        override var time: LocalTime? = null,     // 时长
        var matchTime: LocalTime? = null,       // 撮合时间
        override var numberTrades: Int? = null,  // 单笔交易数量
        var count: Int? = null,                 // 撮合次数
        override var lowScope: Double? = null,  // 最低报价
        override var highScope: Double? = null, // 最高报价
        override var enable: String? = null,    // 是否启用（0：关闭，1：开启）
        override var createTime: Date? = null,  // 创建时间
        var quoteTime: LocalTime? = null,    // 报价时间
        var currentCount: Int? = null        // 当前撮合次数
) : BaseRoom {
    override val flag: String = RoomEnum.TIMING.flag
    override suspend fun validNull() {
        people = null
        currentCount = null
    }
}
