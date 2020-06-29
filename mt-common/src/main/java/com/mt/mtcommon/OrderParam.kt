package com.mt.mtcommon

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Created by gyh on 2020/4/15.
 * @apiDefine OrderParam
 * @apiParam {Double} price 报价
 * @apiParam {Boolean} [isBuy] 是否买家 true:是买家，false:不是买家
 * @apiParam {Int} [number] 交易数量，默认100股
 */
open class OrderParam(
        var userId: Int? = null,        // 用户id
        var userName: String? = null,   // 用户名
        var price: BigDecimal? = null,  // 报价
        var roomId: String? = null,     // 房间号
        @set:JsonProperty("isBuy")
        var isBuy: Boolean? = null,     // 是否买家
        var number: Int? = null,        // 交易数量
        var mode: String? = null,       // 房间模式
        var time: LocalDateTime = LocalDateTime.now(),        // 报价时间，默认当前时间
        var tradeTime: LocalDateTime? = null,    // 成交时间
        var tradeState: String? = TradeState.STAY,  // 交易状态
        var stateDetails: String? = null,   // 状态原因
        var tradePrice: BigDecimal? = null  // 成交价格
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    fun verify(): Boolean {
        return price?.let { it.toDouble() > 0 } ?: false
    }

    fun onTrade(tradeInfo: TradeInfo) {
        tradeTime = tradeInfo.tradeTime
        tradeState = tradeInfo.tradeState
        tradePrice = tradeInfo.tradePrice
        stateDetails = tradeInfo.stateDetails
    }

    /**
     * 更严格的匹配，主要用于redis匹配历史报价
     */
    fun strictEquals(other: OrderParam): Boolean {
        if (userId != other.userId) return false
        if (roomId != other.roomId) return false
        if (price != other.price) return false
        if (number != other.number) return false
        if (mode != other.mode) return false
        if (time != other.time) return false

        return true
    }

    /**
     * 只需要用户id和房间号，就可以匹配是否为同已用户报价，主要用于用户的待撮合列表比较
     */
    override fun equals(other: Any?): Boolean {     // 只需要用户id和房间号就可以唯一标识一个报价
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OrderParam

        if (userId != other.userId) return false
        if (roomId != other.roomId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId ?: 0
        result = 31 * result + (roomId?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "OrderParam(userId=$userId, price=$price, roomId=$roomId, isBuy=$isBuy, number=$number, flag=$mode, time=$time, tradeTime=$tradeTime, tradeState=$tradeState, stateDetails=$stateDetails, tradePrice=$tradePrice, logger=$logger)"
    }
}