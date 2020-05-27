package com.mt.mtuser.dao

import com.mt.mtuser.entity.User
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

/**
 * Created by gyh on 2020/3/17.
 */
interface UserDao : CoroutineCrudRepository<User, Int> {

    @Query("select count(0) from mt_user where phone = $1 limit 1")
    suspend fun existsUserByPhone(phone: String): Int

    @Query("SELECT * from mt_user where id in (:ids) ")
    fun findByIdIn(ids: List<Int>): Flow<User>

    @Query("select * from mt_user where phone = :phone")
    suspend fun findByPhone(phone: String): User?

    @Query("select read_time from mt_user where id = :userId")
    suspend fun findReadTimeByUserId(userId: Int): Date

    @Modifying
    @Query("UPDATE mt_user set read_time = :readTime where id = :userId")
    suspend fun setReadTimeByUserId(userId: Int, readTime: Date): Int
}