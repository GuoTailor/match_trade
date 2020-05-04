package com.mt.mtengine.dao

import com.mt.mtengine.entity.User
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/17.
 */
interface UserDao : ReactiveCrudRepository<User, Int> {

    @Query("select count(0) from mt_user where phone = $1 limit 1")
    fun existsUserByPhone(phone: String): Mono<Int>

    @Query("SELECT * from mt_user where id in (:ids) ")
    fun findByIdIn(ids: List<Int>): Flux<User>

    @Query("select * from mt_user where phone = :phone")
    fun findByPhone(phone: String): Mono<User>
}