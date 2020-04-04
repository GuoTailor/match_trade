package com.mt.mtuser.entity

import org.springframework.data.annotation.Transient
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import reactor.core.publisher.Mono
import java.util.logging.Logger
import java.util.stream.Collectors

/**
 * Created by gyh on 2019/8/22.
 */
abstract class BaseUser : UserDetails {
    @Transient
    open var roles: Collection<GrantedAuthority> = emptyList()
    abstract var id: Int?

    /**
     * 用户[BCryptPasswordEncoder]编码密码
     * @param password 未编码的密码
     * @return 编码后的密码
     */
    private fun passwordEncoder(password: String?): String? {
        return if (password == null || password.isEmpty()) null else passwordEncoder.encode(password)
    }

    /**
     * 用户[BCryptPasswordEncoder]编码密码
     */
    fun passwordEncoder(): BaseUser {
        password = passwordEncoder(password)
        return this
    }

    @JsonIgnore
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return roles
    }

    @JsonIgnore
    abstract override fun getPassword(): String?
    abstract fun setPassword(password: String?)

    @JsonIgnore
    override fun isAccountNonExpired(): Boolean {
        return true
    }

    @JsonIgnore
    override fun isAccountNonLocked(): Boolean {
        return true
    }

    @JsonIgnore
    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    @JsonIgnore
    override fun isEnabled(): Boolean {
        return true
    }

    /**
     * 获取该用户的公司列表
     */
    @JsonIgnore
    fun getCompanyList(): MutableList<Int> {
        return roles.stream()
                .filter { it is Role && it.companyid != null }
                .map { (it as Role).companyid }
                .collect(Collectors.toList())
    }

    /**
     * 获取该用户的指定橘角色得公司列表
     * 比如获取自己能管理得房间，那么角色就是 ROLE_ADMIN
     * 获取所有自己能加入的房间就请调用[getCompanyList]的无参方法
     */
    @JsonIgnore
    fun getCompanyList(roleName: String): MutableList<Int> {
        return roles.stream()
                .filter { it is Role && it.authority == roleName && it.companyid != null }
                .map { (it as Role).companyid }
                .collect(Collectors.toList())
    }

    /**
     * 获取指定角色的集合
     */
    @JsonIgnore
    fun getRoleByName(roleName: String): MutableList<Role> {
        return roles.stream()
                .filter { it is Role && it.authority == roleName }
                .map { it as Role }
                .collect(Collectors.toList())
    }

    companion object {
        private val logger = Logger.getLogger(BaseUser::class.java.simpleName)
        var passwordEncoder = BCryptPasswordEncoder()

        /**
         * 获取当前登陆的用户<P>
         * 注意：该方法只返回用户的id,角色
         *
         * @return 用户
         */
        fun getcurrentUser(): Mono<BaseUser> {
            return ReactiveSecurityContextHolder
                    .getContext()
                    .map { context ->
                        val id = context.authentication.principal?.toString()
                        val roles = context.authentication.authorities
                        object : BaseUser() {
                            override var roles = roles
                            override var id: Int? = id?.toInt()
                            override fun getPassword(): String? = null
                            override fun setPassword(password: String?) {}
                            override fun getUsername() = null
                        }
                    }
        }

        /**
         * 通过用户的关键信息创建一个[BaseUser]
         * @param id    用户的id
         * @param username  用户名
         * @param role  用的角色
         * @return [BaseUser]
         */
        fun createUser(id: Int, username: String, role: Collection<String>?): BaseUser {
            return object : BaseUser() {
                override var id: Int? = id
                override fun getUsername() = username
                override fun getAuthorities(): Collection<GrantedAuthority> {
                    return role?.stream()?.map { GrantedAuthority { it } }?.collect(Collectors.toList())
                            ?: mutableListOf()
                }

                override fun getPassword(): String? = null
                override fun setPassword(password: String?) {}
            }
        }
    }
}