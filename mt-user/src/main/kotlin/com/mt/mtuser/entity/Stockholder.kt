package com.mt.mtuser.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import org.springframework.security.core.GrantedAuthority
import java.math.BigDecimal
import org.springframework.data.annotation.Transient

/**
 * Created by gyh on 2020/3/7.
 */
@Table("mt_stockholder")
class Stockholder(
        @Id
        var id: Int? = null,
        /*** 用户id */
        var userId: Int? = null,
        /*** 该用户在公司的角色id */
        var roleId: Int? = null,
        /*** 公司id */
        var companyId: Int? = null,
        /*** 真实姓名 */
        var realName: String? = null,
        var dpId: Int? = null,
        /** 资金 */
        var money: BigDecimal? = null

) : GrantedAuthority {

    /*** 角色名 */
    @Transient
    var name: String? = null

    /*** 角色中文名 */
    @Transient
    var nameZh: String? = null

    override fun getAuthority(): String? {
        return name
    }

    fun setName(name: String): Stockholder {
        this.name = name
        return this
    }

    override fun toString(): String {
        return "Role(id=$id, userId=$userId, roleId=$roleId, companyId=$companyId, name=$name, nameZh=$nameZh)"
    }

    fun cleanCompany() {
        companyId = null
        realName = null
        dpId = null
        money = null
    }

    companion object {
        const val SUPER_ADMIN = "ROLE_SUPER_ADMIN"
        const val ANALYST = "ROLE_ANALYST"
        const val ADMIN = "ROLE_ADMIN"
        const val USER = "ROLE_USER"
    }

}