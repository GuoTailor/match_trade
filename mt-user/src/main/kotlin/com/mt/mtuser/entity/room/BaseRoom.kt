package com.mt.mtuser.entity.room

import com.fasterxml.jackson.annotation.JsonFormat
import com.mt.mtcommon.RoomEnum
import com.mt.mtcommon.RoomRecord
import com.mt.mtcommon.plus
import com.mt.mtcommon.toLocalDateTime
import org.springframework.data.domain.Persistable
import org.springframework.format.annotation.DateTimeFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime

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

    @set:DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:SS", iso = DateTimeFormat.ISO.DATE_TIME)
    @set:JsonFormat(pattern = "YYYY-MM-dd HH:mm:SS")
    var createTime: LocalDateTime?        // 创建时间

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
    fun <T : BaseRoom> setEnable(value: Boolean): T {
        this.enable = if (value) "1" else "0"
        return this as T
    }

    fun isEnable(): Boolean {
        return enable == "1"
    }

    fun getDelayEndTIme(): LocalTime {
        // 延迟一分钟，防止那种只撮合一次的房间在撮合时由于房间关闭，在更新用户报价信息时获取不到用户的历史报价导致撮合失败的问题
        return startTime!!.plusNanos(time!!.toNanoOfDay() + 59_000_000_000)
    }

    fun toRoomRecord(): RoomRecord {
        val record = RoomRecord(
                roomId = roomId,
                mode = flag,
                companyId = companyId,
                stockId = stockId
        )
        record.duration = time
        record.tradeAmount = numberTrades
        record.startTime = startTime!!.toLocalDateTime()
        record.endTime = (time!! + startTime!!).toLocalDateTime()
        val endTIme = getDelayEndTIme()
        record.expire = Duration.ofNanos(endTIme.minusNanos(LocalTime.now().toNanoOfDay()).toNanoOfDay())

        if (this is ClickMatch) {
            record.quoteTime = quoteTime ?: LocalTime.MIN
            record.secondStage = secondStage
            record.rival = rival
        }
        record.cycle = when (flag) {
            RoomEnum.CLICK.mode -> LocalTime.MIN
            RoomEnum.BICKER.mode -> LocalTime.MIN
            RoomEnum.DOUBLE.mode -> LocalTime.ofSecondOfDay(1)
            RoomEnum.CONTINUE.mode -> LocalTime.ofSecondOfDay(1)
            RoomEnum.TIMING.mode -> LocalTime.MIN
            else -> throw IllegalStateException("不支持的房间号模式${roomId}")
        }
        return record
    }
}