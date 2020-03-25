package com.mt.mtuser.entity.room

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

/**
 * Created by gyh on 2020/3/23.
 * 两两撮合
 */
@Table("mt_room_double")
class DoubleMatch(
        @Id
        var id: Int? = null,
        var roomNumber: String? = null,     // 房间号
        var companyId: Int? = null,         // 公司id
        var stockId: Int? = null,           // 股票id
        var name: String? = null,           // 房间名字
        var people: Int? = null,            // 人数
        var startTime: Date? = null,        // 开始时间
        var endTime: Date? = null,          // 结束时间
        var numberTrades: Int? = null,      // 单笔交易数量
        var highScope: Double? = null,      // 报价最高值
        var lowScope: Double? = null,       // 报价最低值
        var enable: String? = null,         // 是否开启（0：关闭，1：开启）
        var createTime: Date? = null        // 创建时间
)