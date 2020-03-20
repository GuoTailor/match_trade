package com.mt.mtgateway

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

/**
 * Created by gyh on 2020/3/21.
 */
interface RoleRepository : ReactiveCrudRepository<Role, Int> {
    @Query("select name from mt_user_role ur, mt_role r where ur.userid = :userId and ur.roleid = r.id")
    fun findRoleByUserId(userId: Int): Flux<Role>
}