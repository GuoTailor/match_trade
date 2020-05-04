package com.mt.mtcommon

import java.math.BigDecimal
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
        var price: BigDecimal? = null,  // 报价
        var roomId: String? = null, // 房间号
        var isBuy: Boolean? = null, // 是否买家
        var number: Int = 100,      // 交易数量，默认100股
        var flag: String? = null,
        var time: Date = Date()     // 报价时间，默认当前时间
) {

    fun verify(): Boolean {
        return (price?.let { it.toDouble() > 0 } ?: false) && number > 0
    }

    override fun toString(): String {
        return "OrderParam(userId=$userId, price=$price, roomId=$roomId, isBuy=$isBuy, number=$number, time=$time)"
    }

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
}