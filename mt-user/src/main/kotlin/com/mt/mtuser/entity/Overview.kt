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
        buyStock?.let { buyStock = other.buyStock }
        sellStock?.let { sellStock = other.sellStock }
        buyMoney?.let { buyMoney = other.buyMoney }
        sellMoney?.let { sellMoney = other.sellMoney }
        avgBuyMoney?.let { avgBuyMoney = other.avgBuyMoney }
        avgSellMoney?.let { avgSellMoney = other.avgSellMoney }
        netBuyStock?.let { netBuyStock = other.netBuyStock }
        netBuyMoney?.let { netBuyMoney = other.netBuyMoney }
        return this
    }

    fun computeNetBuy() {
        netBuyStock = (buyStock ?: 0) - (sellStock ?: 0)
        netBuyMoney = (buyMoney ?: BigDecimal(0)).subtract(sellMoney ?: BigDecimal(0))
    }
}