package com.mt.mtuser.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("mt_stockholder_info")
class StockholderInfo(
        @Id
        var id: Int? = null,

        /**
         * 股票所属公司id
         */
        var companyId: Int? = null,

        /**
         * 股票id
         */
        var stockId: Int? = null,

        /**
         * 用户id
         */
        var userId: Int? = null,

        /**
         * 数量
         */
        var amount: Int? = null,

        /**
         * 真实姓名
         */
        var realName: String? = null,

        /**
         * 所在部门
         */
        var department: String? = null,

        /**
         * 职位
         */
        var position: String? = null

)