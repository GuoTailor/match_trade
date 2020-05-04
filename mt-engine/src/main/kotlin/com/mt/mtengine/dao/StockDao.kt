package com.mt.mtengine.dao

import com.mt.mtengine.entity.Stock
import kotlinx.coroutines.flow.Flow
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/22.
 */
interface StockDao: ReactiveCrudRepository<Stock, Int> {

    @Query("select * from mt_stock where company_id = :companyId")
    fun findByCompanyId(companyId: Int) : Flux<Stock>

    @Query("select * from mt_stock where name = :stockName")
    fun findByName(stockName: String): Mono<Stock>
}