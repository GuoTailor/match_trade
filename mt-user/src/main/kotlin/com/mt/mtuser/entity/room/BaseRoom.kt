package com.mt.mtuser.entity.room

import com.fasterxml.jackson.annotation.JsonFormat
import com.mt.mtcommon.*
import org.springframework.data.domain.Persistable
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalTime
import java.util.*

/**
 * Created by gyh on 2020/3/24.
 */
interface BaseRoom : Persistable<String> {
    var roomId: String?         // 房间id，四张房间表唯一
    var companyId: Int?          // 公司id
    var stockId: Int?            // 股票id
    var name: String?            // 房间名字
    var people: Int?             // 人数
    var startTime: LocalTime?   // 房间开启时间
    var time: LocalTime?          // 时长
    var numberTrades: Int?       // 单笔交易数量
    var highScope: Double?       // 报价最高值
    var lowScope: Double?        // 报价最低值
    var enable: String?          // 是否开启（0：关闭，1：开启）

    @set:DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:SS")
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    var createTime: Date?        // 创建时间

    @get:org.springframework.data.annotation.Transient
    val flag: String            // 标识符
    override fun getId() = roomId
    override fun isNew() = true

    suspend fun validNull()

    companion object {
        const val ENABLE = "1"
        const val DISABLED = "0"
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseRoom> isEnable(value: Boolean): T {
        this.enable = if (value) "1" else "0"
        return this as T
    }

    fun toRoomRecord(): RoomRecord {
        val record = RoomRecord(
                roomId = roomId,
                model = flag,
                companyId = companyId
        )
        record.duration = time
        record.tradeAmount = numberTrades
        startTime?.let { sTime ->
            time?.let {
                record.endTime = (sTime + it).toDate()
            }
        }
        if (this is ClickMatch) {
            record.quoteTime = quoteTime ?: LocalTime.MIN
            record.secondStage = secondStage
            record.rival = rival
        }
        record.cycle = when (flag) {
            RoomEnum.CLICK.flag -> LocalTime.MIN
            RoomEnum.BICKER.flag -> LocalTime.MIN
            RoomEnum.DOUBLE.flag -> LocalTime.ofSecondOfDay(1)
            RoomEnum.CONTINUE.flag -> LocalTime.ofSecondOfDay(1)
            RoomEnum.TIMING.flag -> LocalTime.MIN
            else -> throw IllegalStateException("不支持的房间号模式${roomId}")
        }
        return record
    }
}