package com.mt.mtgateway

/**
 * Created by gyh on 2020/3/15.
 */
data class User (
    var id: Int? = null,
    var username: String? = null,
    var password: String? = null,
    var roles: Collection<String> = mutableListOf()
)