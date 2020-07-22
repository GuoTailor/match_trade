package com.mt.mtcommon

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Created by gyh on 2020/3/26.
 * 房间启用记录
 * @apiDefine RoomRecord
 * @apiParam {Integer} id
 * @apiParam {Integer} roomId 房间id
 * @apiParam {String} model 模式对应撮合模式
 * @apiParam {Integer} stockId 股票id
 * @apiParam {Integer} companyId 公司id
 * @apiParam {Date} startTime 启用时间
 * @apiParam {Date} endTime 结束时间
 * @apiParam {Decimal} maxPrice 最高价
 * @apiParam {Decimal} minPrice 最低价
 * @apiParam {Decimal} openPrice 开盘价
 * @apiParam {Decimal} closePrice 收盘价
 */
@Table("mt_room_record")
// 必须open，用于redis的json反序列化
open class RoomRecord(
        @Id var id: Int? = null,
        open var roomId: String? = null,        // 房间id，四张房间表唯一
        open var mode: String? = null,          // 模式对应撮合模式
        open var stockId: Int? = null,          // 股票id
        open var companyId: Int? = null,        // 公司id
        @JsonDeserialize(using = LocalDateTimeDeserializer::class)
        @JsonSerialize(using = LocalDateTimeSerializer::class)
        open var startTime: LocalDateTime? = null,       // 启用时间
        @JsonDeserialize(using = LocalDateTimeDeserializer::class)
        @JsonSerialize(using = LocalDateTimeSerializer::class)
        open var endTime: LocalDateTime? = null,         // 结束时间,房间结束时会重新计算
        open var maxPrice: BigDecimal? = null,  // 最高价
        open var minPrice: BigDecimal? = null,  // 最低价
        open var openPrice: BigDecimal? = null, // 开盘价
        open var closePrice: BigDecimal? = null // 收盘价
) {
    @JsonDeserialize(using = LocalTimeDeserializer::class)
    @JsonSerialize(using = LocalTimeSerializer::class)
    @Transient
    open var quoteTime: LocalTime? = LocalTime.MIN  // 报价和选择身份时间

    @JsonDeserialize(using = LocalTimeDeserializer::class)
    @JsonSerialize(using = LocalTimeSerializer::class)
    @Transient
    open var secondStage: LocalTime? = null // 第二阶段时间

    @Transient
    open var rival: Int? = null            // 选择对手个数

    @Transient
    open var tradeAmount: Int? = null      // 交易数量

    @JsonDeserialize(using = LocalTimeDeserializer::class)
    @JsonSerialize(using = LocalTimeSerializer::class)
    @Transient
    open var cycle: LocalTime? = null      // 周期

    @JsonDeserialize(using = LocalTimeDeserializer::class)
    @JsonSerialize(using = LocalTimeSerializer::class)
    @Transient
    open var duration: LocalTime? = null   // 时长,房间结束时会重新计算
    @JsonIgnore
    @Transient
    /**
     * 用于redis的过期时间，一般会比房间结束时间晚，主要防止房间结束后撮合模块获取不到房间记录的问题
     * 注意和[duration]字段的区别
     */
    open var expire: Duration? = null

    open fun computingTime(): RoomRecord {
        duration = startTime?.let { start ->
            endTime?.let { end ->
                LocalTime.ofSecondOfDay((end.toEpochMilli() - start.toEpochMilli()) / 1000 - (quoteTime ?: LocalTime.MIN).toSecondOfDay())
            }
        }
        return this
    }


}

