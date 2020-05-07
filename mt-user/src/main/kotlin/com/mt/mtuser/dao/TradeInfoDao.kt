package com.mt.mtuser.dao

import com.mt.mtcommon.TradeInfo
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import java.util.*

/**
 * Created by gyh on 2020/5/2.
 */
interface TradeInfoDao : CoroutineCrudRepository<TradeInfo, Int> {

    @Query("select sum(trade_amount) from mt_trade_info where trade_time > :time")
    suspend fun countStockByTradeTime(time: Date): Long

    @Query("select sum(trade_amount) from mt_trade_info where trade_time > :time and company_id = :companyId")
    suspend fun countStockByTradeTimeAndCompanyId(time: Date, companyId: Int): Long
}