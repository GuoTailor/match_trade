package com.mt.mtuser.entity.room

import com.fasterxml.jackson.annotation.JsonFormat
import com.mt.mtuser.service.room.RoomEnum
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.format.annotation.DateTimeFormat
import java.sql.Time
import java.time.Duration
import java.time.LocalTime
import java.util.*

/**
 * Created by gyh on 2020/3/23.
 *  定时撮合
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
        var currentCount: Int? = null,       // 当前撮合次数
        override val flag: String = RoomEnum.TIMING.flag
) : BaseRoom {
    override suspend fun validNull() {
        people = null
        currentCount = null
    }
}
