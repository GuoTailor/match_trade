package com.mt.mtuser.entity.room

import com.fasterxml.jackson.annotation.JsonFormat
import com.mt.mtuser.common.Util
import org.springframework.format.annotation.DateTimeFormat
import java.time.Duration
import java.util.*

/**
 * Created by gyh on 2020/3/24.
 */
interface BaseRoom {
    var id: Int?
    var roomNumber: String?      // 房间号
    var companyId: Int?          // 公司id
    var stockId: Int?            // 股票id
    var name: String?            // 房间名字
    var people: Int?             // 人数

    @set:DateTimeFormat(pattern = "HH:mm:SS")
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss", timezone = "GMT+8")
    var time: Duration?          // 时长
    var numberTrades: Int?       // 单笔交易数量
    var highScope: Double?       // 报价最高值
    var lowScope: Double?        // 报价最低值
    var enable: String?          // 是否开启（0：关闭，1：开启）
    var createTime: Date?        // 创建时间
    @get:org.springframework.data.annotation.Transient
    @set:org.springframework.data.annotation.Transient
    var flag: String            // 标识符

    @Suppress("UNCHECKED_CAST")
    suspend fun <T : BaseRoom> createRoomNumber(): T {
        this.roomNumber = flag + Util.getRandomInt(6)
        return this as T
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun <T : BaseRoom> isEnable(value: Boolean): T {
        this.enable = if (value) "1" else "0"
        return this as T
    }
}