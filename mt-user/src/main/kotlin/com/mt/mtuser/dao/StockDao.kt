package com.mt.mtuser.dao

import com.mt.mtuser.entity.Stock
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * Created by gyh on 2020/3/22.
 */
interface StockDao: CoroutineCrudRepository<Stock, Int> {

    @Query("select * from mt_stock where company_id = :companyId")
    fun findByCompanyId(companyId: Int) : Flow<Stock>

    @Query("select * from mt_stock where name = :stockName")
    suspend fun findByName(stockName: String): Stock?
}