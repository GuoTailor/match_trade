package com.mt.mtgateway.repository

import com.mt.mtgateway.bean.Role
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

/**
 * Created by gyh on 2020/3/21.
 */
interface RoleRepository : ReactiveCrudRepository<Role, Int> {
    @Query("select name, company_id, real_name from mt_stockholder ur, mt_role r where ur.user_id = :userId and ur.role_id = r.id")
    fun findRoleByUserId(userId: Int): Flux<Role>
}