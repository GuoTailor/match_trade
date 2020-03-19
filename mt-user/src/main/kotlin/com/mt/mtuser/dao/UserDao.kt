package com.mt.mtuser.dao

import com.mt.mtuser.entity.User
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/17.
 */
interface UserDao : ReactiveCrudRepository<User, Int> {
    @Query("select count(0) from mt_user where phone = $1 limit 1")
    fun existsUserByPhone(phone: String): Mono<Int>
}