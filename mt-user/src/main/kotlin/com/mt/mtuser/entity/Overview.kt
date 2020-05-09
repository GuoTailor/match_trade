package com.mt.mtuser.entity

import java.math.BigDecimal

/**
 * Created by gyh on 2020/5/9.
 */
class Overview(
        var buyStock: Long? = null,         // 买入股数
        var sellStock: Long? = null,        // 卖出股数
        var buyMoney: BigDecimal? = null,   // 买入金额
        var sellMoney: BigDecimal? = null,  // 卖出金额
        var avgBuyMoney: BigDecimal? = null,// 平均买价
        var avgSellMoney: BigDecimal? = null,   // 平均卖价
        var netBuyStock: Long? = null,      // 净买入股数
        var netBuyMoney: BigDecimal? = null // 净买入金额
) {
    fun copyNotNullField(other: Overview): Overview {
        if (buyStock == null) { buyStock = other.buyStock }
        if (sellStock == null) { sellStock = other.sellStock }
        if (buyMoney == null) { buyMoney = other.buyMoney }
        if (sellMoney == null) { sellMoney = other.sellMoney }
        if (avgBuyMoney == null) { avgBuyMoney = other.avgBuyMoney }
        if (avgSellMoney == null) { avgSellMoney = other.avgSellMoney }
        if (netBuyStock == null) { netBuyStock = other.netBuyStock }
        if (netBuyMoney == null) { netBuyMoney = other.netBuyMoney }
        return this
    }

    fun computeNetBuy() {
        netBuyStock = (buyStock ?: 0) - (sellStock ?: 0)
        netBuyMoney = (buyMoney ?: BigDecimal(0)).subtract(sellMoney ?: BigDecimal(0))
    }
}