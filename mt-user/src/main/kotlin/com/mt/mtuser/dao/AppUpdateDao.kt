package com.mt.mtuser.dao

import com.mt.mtuser.entity.AppUpdate
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/7/11
 */
interface AppUpdateDao: ReactiveCrudRepository<AppUpdate, Int> {

    @Query("select * from mt_app_update where version_code > :version order by id desc limit 1")
    fun getVersionByVersionCode(version: String): Mono<AppUpdate>
}