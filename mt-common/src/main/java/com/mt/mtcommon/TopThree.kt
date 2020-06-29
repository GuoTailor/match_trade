package com.mt.mtcommon

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Created by gyh on 2020/5/15.
 */
open class TopThree(
        var roomId: String,
        var mode: String,
        var buyTopThree: ArrayList<OrderInfo> = ArrayList(),
        var sellTopThree: ArrayList<OrderInfo> = ArrayList()
)

/**
 * @apiDefine OrderInfo
 * @apiSuccess (返回) {Long} userId 用户id
 * @apiSuccess (返回) {String} roomId 房间id
 * @apiSuccess (返回) {Decimal} price 价格
 * @apiSuccess (返回) {Integer} number 数量
 * @apiSuccess (返回) {Date} date 时间
 */
open class OrderInfo(val userId: Int,
                     val roomId: String,
                     val price: BigDecimal,
                     val number: Int,
                     val date: LocalDateTime
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


fun OrderParam.toOrderInfo() = OrderInfo(this.userId!!, this.roomId!!, this.price!!, this.number!!, this.time)

fun TradeInfo.toOrderInfo() = OrderInfo(-1, this.roomId!! , this.tradePrice!!, this.tradeAmount!!, this.tradeTime!!)
