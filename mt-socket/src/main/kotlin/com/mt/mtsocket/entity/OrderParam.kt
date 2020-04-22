package com.mt.mtsocket.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

/**
 * Created by gyh on 2020/4/15.
 * @apiDefine OrderParam
 * @apiParam {Double} price 报价
 * @apiParam {Boolean} [isBuy] 是否买家 true:是买家，false:不是买家
 * @apiParam {Int} [number] 交易数量，默认100股
 */
class OrderParam(
        var id: Int? = null,        // 用户id
        var price: Double? = null,  // 报价
        var isBuy: Boolean? = null, // 是否买家
        var number: Int = 100,      // 交易数量，默认100股
        @JsonIgnore
        var time: Date = Date()     // 报价时间，默认当前时间
) {

    fun verify() : Boolean{
        return (price?.let { it > 0 } ?: false) && number > 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OrderParam

        if (id != other.id) return false
        if (price != other.price) return false
        if (isBuy != other.isBuy) return false
        if (number != other.number) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + (price?.hashCode() ?: 0)
        result = 31 * result + (isBuy?.hashCode() ?: 0)
        result = 31 * result + number
        return result
    }

    override fun toString(): String {
        return "OrderParam(id=$id, price=$price, isBuy=$isBuy, number=$number, time=$time)"
    }
}