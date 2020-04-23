package com.mt.mtsocket.entity

import org.springframework.security.core.GrantedAuthority

/**
 * Created by gyh on 2020/3/7.
 */
class Role() : GrantedAuthority {
    var id: Int? = null

    /**
     * 用户id
     */
    var userId: Int? = null

    /**
     * 该用户在公司的角色id
     */
    var roleId: Int? = null

    /**
     * 公司id
     */
    var companyId: Int? = null

    /**
     * 角色名
     */
    var name: String? = null

    /**
     * 角色中文名
     */
    var nameZh: String? = null


    constructor(userId: Int?, roleId: Int?, companyId: Int?) : this() {
        this.userId = userId
        this.roleId = roleId
        this.companyId = companyId
    }

    override fun getAuthority(): String {
        return name!!
    }

    override fun toString(): String {
        return "Role(id=$id, userId=$userId, roleId=$roleId, companyId=$companyId, name=$name, nameZh=$nameZh)"
    }

    companion object {
        const val SUPER_ADMIN = "ROLE_SUPER_ADMIN"
        const val ADMIN = "ROLE_ADMIN"
        const val USER = "ROLE_USER"
    }

}