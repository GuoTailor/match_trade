package com.mt.mtuser.dao

import com.mt.mtuser.entity.Positions
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/4/23.
 */
interface PositionsDao : CoroutineCrudRepository<Positions, Int> {

    @Query("select $sql from mt_positions where user_id = :userId and company_id = :companyId and stock_id = :stockId")
    suspend fun findStockByCompanyIdAndUserIdAndStockId(userId: Int, companyId: Int, stockId: Int): Positions?

    @Query("select COALESCE(sum(amount), 0) from mt_positions where user_id = :userId and company_id = :companyId")
    suspend fun countStockByCompanyIdAndUserId(userId: Int, companyId: Int): Long

    @Query("select $sql from mt_positions where company_id = :companyId and stock_id = :stockId and user_id = :userId for update")
    suspend fun findLimit(companyId: Int, stockId: Int, userId: Int): Positions

    @Modifying
    @Query("update mt_positions set \"limit\" = :limit where user_id in (:userId) and company_id = :companyId")
    suspend fun updateLimit(userId: List<Int>, companyId: Int, limit: Int): Int

    @Modifying
    @Query("update mt_positions set \"limit\" = :limit where user_id = :userId and company_id = :companyId")
    suspend fun updateLimit(userId: Int, companyId: Int, limit: Int): Int

    @Modifying
    @Query("UPDATE mt_positions SET company_id = :#{[0].companyId}, stock_id = :#{[0].stockId}, user_id = :#{[0].userId}, amount = :#{[0].amount}, \"limit\" = :#{[0].limit} WHERE mt_positions.id = :#{[0].id}")
    suspend fun update(entity: Positions): Positions

    companion object {
        const val sql = "id,company_id,stock_id,user_id,amount,\"limit\""
    }
}