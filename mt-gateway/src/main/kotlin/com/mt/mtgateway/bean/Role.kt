package com.mt.mtgateway.bean

import org.springframework.data.relational.core.mapping.Table

/**
 * Created by gyh on 2020/3/21.
 */
@Table("mt_stockholder")
open class Role(
        var name: String,
        var companyId: Int? = null,
        var realName: String? = null
)