package com.mt.mtuser.dao

import com.mt.mtuser.entity.Positions
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * Created by gyh on 2020/4/23.
 */
interface PositionsDao : CoroutineCrudRepository<Positions, Int> {

    @Query("select $sql from mt_positions where user_id = :userId and company_id = :companyId and stock_id = :stockId")
    suspend fun findStockByCompanyIdAndUserIdAndStockId(userId: Int, companyId: Int, stockId: Int): Positions?

    @Query("select COALESCE(sum(amount), 0) from mt_positions where user_id = :userId and company_id = :companyId")
    suspend fun countStockByCompanyIdAndUserId(userId: Int, companyId: Int): Long

    companion object {
        const val sql = "id,company_id,stock_id,user_id,amount"
    }
}