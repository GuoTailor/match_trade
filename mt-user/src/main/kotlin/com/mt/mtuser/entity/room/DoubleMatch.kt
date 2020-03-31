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
 * 两两撮合
 */
@Table("mt_room_double")
class DoubleMatch(
        @Id
        override var roomId: String? = null,     // 房间id，四张房间表唯一
        override var companyId: Int? = null,         // 公司id
        override var stockId: Int? = null,           // 股票id
        override var name: String? = null,           // 房间名字
        override var people: Int? = null,            // 人数
        override var time: LocalTime? = null,         // 时长
        override var numberTrades: Int? = null,      // 单笔交易数量
        override var highScope: Double? = null,      // 报价最高值
        override var lowScope: Double? = null,       // 报价最低值
        override var enable: String? = null,         // 是否开启（0：关闭，1：开启）
        override var createTime: Date? = null,       // 创建时间
        override val flag: String = RoomEnum.DOUBLE.flag
) : BaseRoom {

        override suspend fun validNull() {
                people = null
        }
}