package com.mt.mtengine.dao

import com.mt.mtengine.entity.Company
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

/**
 * Created by gyh on 2020/3/18.
 */
interface CompanyDao : ReactiveCrudRepository<Company, Int> {

    @Query("select id, name, room_count, mode, create_time from mt_company $1::string")
    fun findAllByQuery(search: String): Flux<Company>

}