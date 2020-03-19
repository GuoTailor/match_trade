package com.mt.mtuser.dao

import com.mt.mtuser.entity.Company
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

/**
 * Created by gyh on 2020/3/18.
 */
interface CompanyDao : ReactiveCrudRepository<Company, Int> {
    @Query("select id, `name`, room_count, `mode`, create_time " +
            "from company" +
            "where &1")
    fun findAllByQuery(search: String): Flux<Company>
}