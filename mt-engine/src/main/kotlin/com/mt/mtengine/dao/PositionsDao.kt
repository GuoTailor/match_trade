package com.mt.mtengine.dao

import com.mt.mtengine.entity.Positions
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/4/23.
 */
interface PositionsDao : ReactiveCrudRepository<Positions, Int> {

    @Query("select $sql from mt_positions where company_id = :companyId and stock_id = :stockId and user_id = :userId")
    fun findBy(companyId: Int, stockId: Int, userId: Int): Mono<Positions>

    @Modifying
    @Query("update mt_positions set amount = :amount where id = :id")
    fun updateAmount(id: Int, amount: Int): Mono<Int>

    companion object {
        const val sql = "id,company_id,stock_id,user_id,amount"
    }
}