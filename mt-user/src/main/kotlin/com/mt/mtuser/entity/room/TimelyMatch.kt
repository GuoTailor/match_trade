package com.mt.mtuser.entity.room

import com.fasterxml.jackson.annotation.JsonFormat
import com.mt.mtuser.service.room.RoomEnum
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.format.annotation.DateTimeFormat
import java.time.Duration
import java.util.*

/**
 * Created by gyh on 2020/3/23.
 * 及时撮合
 */
@Table("mt_room_timely")
class TimelyMatch(
        @Id
        override var id: Int? = null,
        override var roomNumber: String? = null,     // 房间号
        override var companyId: Int? = null,         // 公司id
        override var stockId: Int? = null,           // 股票id
        override var name: String? = null,           // 房间名字
        override var people: Int? = null,            // 人数
        @DateTimeFormat(pattern = "HH:mm:SS")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "GMT+8")
        override var time: Duration? = null,         // 时长
        var matchTime: Date? = null,        // 撮合时间
        override var numberTrades: Int? = null,      // 单笔交易数量
        var count: Int? = null,             // 撮合次数
        override var highScope: Double? = null,      // 报价最高值
        override var lowScope: Double? = null,       // 报价最低值
        override var enable: String? = null,         // 是否开启（0：关闭，1：开启）
        override var createTime: Date? = null,       // 创建时间
        var quoteTime: Date? = null,        // 报价时间
        var currentCount: Int? = null,       // 创建时间
        override var flag: String = RoomEnum.TIMELY.flag
) : BaseRoom