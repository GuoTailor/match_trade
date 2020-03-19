package com.mt.mtgateway

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/15.
 */
interface UserRepository : ReactiveCrudRepository<User, Int> {
    @Query("select id, phone as username, password from mt_user where phone = $1")
    fun findByUsername(username: String): Mono<User>
}