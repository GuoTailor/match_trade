package com.mt.mtcommon

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
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
        var buyerPrice: BigDecimal? = null,    // 买方价格
        var sellerId: Int? = null,             // 卖方id
        var sellerPrice: BigDecimal? = null,   // 卖方价格
        var tradePrice: BigDecimal? = null,    // 成交价格
        val tradeAmount: Int? = null,           // 成交数量
        var tradeMoney: BigDecimal? = null,     // 成交金额
        var tradeTime: Date? = null,           // 交易时间
        var tradeState: String? = null,        // 交易状态
        var stateDetails: String? = null       // 状态原因
) {
    constructor(buy: OrderParam?, sell: OrderParam?, companyId: Int, stockId: Int, flag: String) : this(
            companyId = companyId,
            stockId = stockId,
            roomId = buy?.roomId,
            model = flag,
            tradeAmount = buy?.number,
            buyerId = buy?.userId,
            buyerPrice = buy?.price,
            sellerId = sell?.userId,
            sellerPrice = sell?.price,
            tradeTime = Date()
    )
}