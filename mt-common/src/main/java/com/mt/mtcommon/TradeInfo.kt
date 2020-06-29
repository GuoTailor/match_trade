package com.mt.mtcommon

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Created by gyh on 2020/4/20.
 * 交易信息
 */
@Table("mt_trade_info")
open class TradeInfo(
        @Id
        open var id: Int? = null,
        var companyId: Int? = null,            // 公司id
        var stockId: Int? = null,              // 股票id
        var roomId: String? = null,            // 房间id(在那个房间进行的交)
        var model: String? = null,             // 模式对应撮合模式
        var buyerId: Int? = null,              // 买方id
        var buyerName: String? = null,          // 买方姓名
        var buyerPrice: BigDecimal? = null,    // 买方价格
        var sellerId: Int? = null,             // 卖方id
        var sellerName: String? = null,         // 卖方姓名
        var sellerPrice: BigDecimal? = null,   // 卖方价格
        var tradePrice: BigDecimal? = null,    // 成交价格
        var tradeAmount: Int? = null,           // 成交数量
        var tradeMoney: BigDecimal? = null,     // 成交金额
        var tradeTime: LocalDateTime? = null,   // 交易时间
        var tradeState: String? = null,        // 交易状态
        var stateDetails: String? = null       // 状态原因
) {
    constructor(buy: OrderParam?, sell: OrderParam?, roomId: String, companyId: Int, stockId: Int, flag: String) : this(
            companyId = companyId,
            stockId = stockId,
            roomId = roomId,
            model = flag,
            tradeAmount = buy?.number,
            buyerId = buy?.userId,
            buyerName = buy?.userName,
            buyerPrice = buy?.price,
            sellerId = sell?.userId,
            sellerName = sell?.userName,
            sellerPrice = sell?.price,
            tradeTime = LocalDateTime.now()
    )

    constructor(buy: OrderParam?, sell: OrderParam?) : this(
            roomId = buy?.roomId ?: sell?.roomId,
            model = buy?.mode ?: sell?.mode,
            tradeAmount = buy?.number ?: sell?.number,
            buyerId = buy?.userId,
            buyerName = buy?.userName,
            buyerPrice = buy?.price,
            sellerId = sell?.userId,
            sellerName = sell?.userName,
            sellerPrice = sell?.price,
            tradeTime = LocalDateTime.now()
    )

    override fun toString(): String {
        return "TradeInfo(id=$id, companyId=$companyId, stockId=$stockId, roomId=$roomId, model=$model, buyerId=$buyerId, buyerName=$buyerName, buyerPrice=$buyerPrice, sellerId=$sellerId, sellerName=$sellerName, sellerPrice=$sellerPrice, tradePrice=$tradePrice, tradeAmount=$tradeAmount, tradeMoney=$tradeMoney, tradeTime=$tradeTime, tradeState=$tradeState, stateDetails=$stateDetails)"
    }


}