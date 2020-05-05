package com.mt.mtsocket.entity

import org.springframework.security.core.GrantedAuthority

/**
 * Created by gyh on 2020/3/7.
 */
class Role(
        var id: Int? = null,
        /*** 用户id */
        var userId: Int? = null,
        /*** 该用户在公司的角色id */
        var roleId: Int? = null,
        /*** 公司id */
        var companyId: Int? = null,
        /*** 真实姓名 */
        var realName: String? = null,
        /*** 所在部门 */
        var department: String? = null,
        /*** 职位 */
        var position: String? = null,
        /*** 角色名 */
        var name: String? = null,
        /*** 角色中文名 */
        var nameZh: String? = null
) : GrantedAuthority {

    override fun getAuthority(): String {
        return name!!
    }

    override fun toString(): String {
        return "Role(id=$id, userId=$userId, roleId=$roleId, companyId=$companyId, name=$name, nameZh=$nameZh)"
    }

    companion object {
        const val SUPER_ADMIN = "ROLE_SUPER_ADMIN"
        const val ANALYST = "ROLE_ANALYST"
        const val ADMIN = "ROLE_ADMIN"
        const val USER = "ROLE_USER"
    }

}