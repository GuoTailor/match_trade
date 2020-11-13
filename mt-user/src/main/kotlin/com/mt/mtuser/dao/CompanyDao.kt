package com.mt.mtuser.dao

import com.mt.mtuser.entity.Company
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

/**
 * Created by gyh on 2020/3/18.
 */
interface CompanyDao : CoroutineCrudRepository<Company, Int> {

    @Query("select id, name, room_count, mode, create_time from mt_company $1::string")
    suspend fun findAllByQuery(search: String): Flow<Company>

    @Modifying
    @Query("update mt_company set enable = :enable where id = :id")
    suspend fun enable(id: Int, enable: String): Int

    @Query("select enable from mt_company where id = :id")
    suspend fun findEnableById(id: Int): String
}