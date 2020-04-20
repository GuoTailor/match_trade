package com.mt.mtsocket.entity

import java.util.*

/**
 * Created by gyh on 2020/4/20.
 * 交易信息
 */
class TradeInfo(
        val id: Int,
        val companyId: Int,         // 公司id
        val stockId: Int,           // 股票id
        val roomId: Int,            // 房间id(在那个房间进行的交)
        val model: Int,             // 模式对应撮合模式
        val buyerId: Int,           // 买方id
        val buyerPrice: Double,     // 买方价格
        val sellerId: Int,          // 卖方id
        val sellerPrice: Double,    // 卖方价格
        val tradePrice: Double,     // 成交价格
        val tradeTime: Date,        // 交易时间
        val tradeState: String,     // 交易状态
        val stateDetails: String    // 状态原因
)