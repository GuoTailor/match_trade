package com.mt.mtuser.dao

import com.mt.mtuser.entity.User
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * Created by gyh on 2020/3/17.
 */
interface UserDao : CoroutineCrudRepository<User, Int> {

    @Query("select count(0) from mt_user where phone = $1 limit 1")
    suspend fun existsUserByPhone(phone: String): Int

    @Query("SELECT * from mt_user where id in (:ids) ")
    fun findByIdIn(ids: List<Int>): Flow<User>
}