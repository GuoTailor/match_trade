package com.mt.mtengine.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("mt_positions")
class Positions(
        @Id
        var id: Int? = null,
        /*** 股票所属公司id*/
        var companyId: Int? = null,
        /*** 股票id*/
        var stockId: Int? = null,
        /*** 用户id*/
        var userId: Int? = null,
        /*** 数量*/
        var amount: Int? = null,
        /** 卖买限制 */
        var limit: Int? = null
) {
        override fun toString(): String {
                return "Positions(id=$id, companyId=$companyId, stockId=$stockId, userId=$userId, amount=$amount, limit=$limit)"
        }
}