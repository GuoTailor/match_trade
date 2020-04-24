package com.mt.mtuser.entity

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
        var amount: Int? = null

)