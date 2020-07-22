package com.mt.mtuser.dao.department

import com.mt.mtuser.entity.department.Post
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * Created by gyh on 2020/7/20
 */
interface PostDao : CoroutineCrudRepository<Post, Int> {

    @Query("select count(*) from mt_post where name = :name limit 1")
    suspend fun exitByName(name: String): Int

    @Query("select * from mt_post where name = :name limit 1")
    suspend fun findByName(name: String): Post?
}