package com.mt.mtcommon

import java.math.BigDecimal
import java.util.*

/**
 * Created by gyh on 2020/4/20.
 * 交易信息
 */
open class TradeInfo(
        open var id: Int? = null,
        open var companyId: Int? = null,         // 公司id
        open var stockId: Int? = null,           // 股票id
        open var roomId: String? = null,         // 房间id(在那个房间进行的交)
        open var model: String? = null,          // 模式对应撮合模式
        open var buyerId: Int? = null,           // 买方id
        open var buyerPrice: BigDecimal? = null,     // 买方价格
        open var sellerId: Int? = null,          // 卖方id
        open var sellerPrice: BigDecimal? = null,    // 卖方价格
        open var tradePrice: BigDecimal? = null,     // 成交价格
        open var tradeTime: Date? = null,        // 交易时间
        open var tradeState: String? = null,     // 交易状态
        open var stateDetails: String? = null    // 状态原因
)