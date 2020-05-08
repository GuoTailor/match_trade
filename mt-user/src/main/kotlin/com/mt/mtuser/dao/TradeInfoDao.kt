package com.mt.mtuser.dao

import com.mt.mtcommon.TradeInfo
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import java.math.BigDecimal
import java.util.*

/**
 * Created by gyh on 2020/5/2.
 */
interface TradeInfoDao : CoroutineCrudRepository<TradeInfo, Int> {

    @Query("select sum(trade_amount) from mt_trade_info where trade_time > :time")
    suspend fun countStockByTradeTime(time: Date): Long

    @Query("select sum(trade_amount) from mt_trade_info where trade_time > :time and company_id = :companyId")
    suspend fun countStockByTradeTimeAndCompanyId(time: Date, companyId: Int): Long

    @Query("select sum(trade_amount) from mt_trade_info where trade_time > :time and stock_id = :stockId")
    suspend fun countStockByTradeTimeAndStockId(time: Date, stockId: Int): Long

    @Query("select sum(trade_money) from mt_trade_info where trade_time > :time")
    suspend fun countMoneyTradeTime(time: Date): Long

    @Query("select sum(trade_money) from mt_trade_info where trade_time > :time and company_id = :companyId")
    suspend fun countMoneyByTradeTimeAndCompanyId(time: Date, companyId: Int): Long

    @Query("select sum(trade_money) from mt_trade_info where trade_time > :time and stock_id = :stockId")
    suspend fun countMoneyByTradeTimeAndStockId(time: Date, stockId: Int): Long

    @Query("select trade_price from mt_trade_info " +
            " where trade_time >= :startTime " +
            " and trade_time <= :endTime " +
            " and company_id = :companyId " +
            " order by trade_time desc limit 1")
    suspend fun findLastPriceByTradeTimeAndCompanyId(startTime: Date, entTime: Date, companyId: Int): BigDecimal

    @Query("select trade_price from mt_trade_info " +
            " where trade_time >= :startTime " +
            " and trade_time <= :endTime " +
            " and stock_id = :stockId " +
            " order by trade_time desc limit 1")
    suspend fun findLastPriceByTradeTimeAndStockId(startTime: Date, entTime: Date, stockId: Int): BigDecimal

}