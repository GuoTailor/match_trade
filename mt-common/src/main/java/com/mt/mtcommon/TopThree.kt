package com.mt.mtcommon

import java.math.BigDecimal
import java.util.*

/**
 * Created by gyh on 2020/5/15.
 */
open class TopThree(
        var roomId: String,
        var mode: String,
        var buyTopThree: ArrayList<OrderInfo> = ArrayList(),
        var sellTopThree: ArrayList<OrderInfo> = ArrayList(),
        var lastOrder: OrderInfo? = null
)

open class OrderInfo(val userId: Int,
                     val price: BigDecimal,
                     val number: Int,
                     val date: Date
) : Comparable<OrderInfo> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OrderInfo

        if (price != other.price) return false
        if (number != other.number) return false

        return true
    }

    override fun hashCode(): Int {
        var result = price.hashCode()
        result = 31 * result + number
        return result
    }

    override fun compareTo(other: OrderInfo): Int {
        val result = other.price.compareTo(this.price)
        return if (result == 0) {
            other.number.compareTo(this.number)
        } else result
    }

    override fun toString(): String {
        return "OrderInfo(price=$price, number=$number)"
    }

}


fun OrderParam.toOrderInfo() = OrderInfo(this.userId!!, this.price!!, this.number!!, this.time)

fun TradeInfo.toOrderInfo() = OrderInfo(-1, this.tradePrice!!, this.tradeAmount!!, this.tradeTime!!)
