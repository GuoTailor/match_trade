package com.mt.mtuser.entity.room

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

/**
 * Created by gyh on 2020/3/23.
 *  定时撮合
 */
@Table("mt_room_timing")
class TimingMatch(
        @Id
        var id: Int? = null,
        var roomNumber: String? = null, // 房间号
        var companyId: Int? = null,     // 公司id
        var stockId: Int? = null,       // 股票id
        var name: String? = null,       // 房间名字
        var people: Int? = null,        // 人数
        var endTime: Date? = null,      // 结束时间
        var startTime: Date? = null,    // 开始时间
        var matchTime: Date? = null,    // 撮合时间
        var numberTrades: Int? = null,  // 单笔交易数量
        var count: Int? = null,         // 撮合次数
        var lowScope: Double? = null,   // 最低报价
        var highScope: Double? = null,  // 最高报价
        var enable: String? = null,     // 是否启用（0：关闭，1：开启）
        var createTime: Date? = null,   // 创建时间
        var quoteTime: Date? = null,    // 报价时间
        var currentCount: Int? = null   // 当前撮合次数
)
