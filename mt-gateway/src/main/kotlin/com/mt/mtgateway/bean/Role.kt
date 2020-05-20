package com.mt.mtgateway.bean

/**
 * Created by gyh on 2020/3/21.
 */
data class Role(
        var name: String,
        var companyId: Int? = null,
        var realName: String? = null
)