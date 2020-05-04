package com.mt.mtengine.entity

import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.TradeInfo
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.util.*

/**
 * Created by gyh on 2020/4/20.
 * 交易信息
 */
@Table("mt_trade_info")
class MtTradeInfo(
        @Id
        override var id: Int? = null,
        companyId: Int? = null,         // 公司id
        stockId: Int? = null,           // 股票id
        roomId: String? = null,         // 房间id(在那个房间进行的交)
        model: String? = null,          // 模式对应撮合模式
        buyerId: Int? = null,           // 买方id
        buyerPrice: BigDecimal? = null,     // 买方价格
        sellerId: Int? = null,          // 卖方id
        sellerPrice: BigDecimal? = null,    // 卖方价格
        tradePrice: BigDecimal? = null,     // 成交价格
        tradeTime: Date? = null,        // 交易时间
        tradeState: String? = null,     // 交易状态
        stateDetails: String? = null    // 状态原因
) : TradeInfo(id, companyId, stockId, roomId, model, buyerId, buyerPrice, sellerId, sellerPrice, tradePrice, tradeTime, tradeState, stateDetails)
{
    constructor(tradeInfo: TradeInfo): this() {
        id = tradeInfo.id
        companyId = tradeInfo.companyId
        stockId = tradeInfo.stockId
        roomId = tradeInfo.roomId
        model = tradeInfo.model
        buyerId = tradeInfo.buyerId
        buyerPrice = tradeInfo.buyerPrice
        sellerId = tradeInfo.sellerId
        sellerPrice = tradeInfo.sellerPrice
        tradePrice = tradeInfo.tradePrice
        tradeTime = tradeInfo.tradeTime
        tradeState = tradeInfo.tradeState
        stateDetails = tradeInfo.stateDetails
    }

    constructor(buy: OrderParam?, sell: OrderParam?, companyId: Int, stockId: Int): this() {
        this.companyId = companyId
        this.stockId = stockId
        this.roomId = buy?.roomId
        this.model = buy?.flag
        this.buyerId = buy?.userId
        this.buyerPrice = buy?.price
        this.sellerId = sell?.userId
        this.sellerPrice = sell?.price
        this.tradeTime = Date()
    }

}