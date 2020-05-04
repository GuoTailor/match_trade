package com.mt.mtengine.dao

import com.mt.mtengine.entity.Role
import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/17.
 */
interface UserRoleDao: ReactiveCrudRepository<Role, Int> {
    @Query("select" +
            " ur.id, user_id, role_id, company_id, name, name_zh" +
            " from mt_user_role ur" +
            " left join mt_role r on ur.role_id = r.id" +
            " where user_id = $1")
    fun selectRolesByUserId(userId: Int): Flux<Role>

    @Query("insert into mt_user_role(user_id, role_id, company_id) values(:userId, :roleId, :companyId)")
    fun save(userId: Int, roleId: Int, companyId: Int): Mono<Int>

    @Query("select count(*) from mt_user_role where user_id = :userId and role_id = :roleId and company_id = :companyId limit 1")
    fun exists(userId: Int, roleId: Int, companyId : Int): Mono<Int>

    @Query("select * from mt_user_role where user_id = :userId and role_id = :roleId and company_id = :companyId")
    fun find(userId: Int, roleId: Int, companyId : Int): Mono<Role>
}