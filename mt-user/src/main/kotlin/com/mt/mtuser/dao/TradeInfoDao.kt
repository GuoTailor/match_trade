package com.mt.mtuser.dao

import com.mt.mtcommon.TradeInfo
import com.mt.mtuser.entity.Overview
import com.mt.mtuser.entity.TradeDetails
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import java.math.BigDecimal
import java.util.*

/**
 * Created by gyh on 2020/5/2.
 */
interface TradeInfoDao : CoroutineCrudRepository<TradeInfo, Int> {

    @Query("select COALESCE(sum(trade_amount), 0) from $table where trade_time > :time")
    suspend fun countStockByTradeTime(time: Date): Long

    @Query("select COALESCE(sum(trade_amount), 0) from $table where trade_time > :time and company_id = :companyId")
    suspend fun countStockByTradeTimeAndCompanyId(time: Date, companyId: Int): Long

    @Query("select COALESCE(sum(trade_amount), 0) from $table where trade_time > :time and stock_id = :stockId")
    suspend fun countStockByTradeTimeAndStockId(time: Date, stockId: Int): Long

    @Query("select COALESCE(sum(trade_money), 0) from $table where trade_time > :time")
    suspend fun countMoneyTradeTime(time: Date): Long

    @Query("select COALESCE(sum(trade_money), 0) from $table where trade_time > :time and company_id = :companyId")
    suspend fun countMoneyByTradeTimeAndCompanyId(time: Date, companyId: Int): Long

    @Query("select COALESCE(sum(trade_money), 0) from $table where trade_time > :time and stock_id = :stockId")
    suspend fun countMoneyByTradeTimeAndStockId(time: Date, stockId: Int): Long

    @Query("select trade_price from $table " +
            " where trade_time >= :startTime " +
            " and trade_time <= :endTime " +
            " and company_id = :companyId " +
            " order by trade_time desc limit 1")
    suspend fun findLastPriceByTradeTimeAndCompanyId(startTime: Date, endTime: Date, companyId: Int): BigDecimal?

    @Query("select trade_price from $table " +
            " where trade_time >= :startTime " +
            " and trade_time <= :endTime " +
            " and stock_id = :stockId " +
            " order by trade_time desc limit 1")
    suspend fun findLastPriceByTradeTimeAndStockId(startTime: Date, endTime: Date, stockId: Int): BigDecimal?

    @Query("select COALESCE(max(trade_price), 0) from $table " +
            " where trade_time >= :startTime " +
            " and trade_time <= :endTime " +
            " and room_id = :roomId ")
    suspend fun findMaxPriceByTradeTimeAndRoomId(roomId: String, startTime: Date, endTime: Date): BigDecimal

    @Query("select COALESCE(min(trade_price), 0) from $table " +
            " where trade_time >= :startTime " +
            " and trade_time <= :endTime " +
            " and room_id = :roomId ")
    suspend fun findMinPriceByTradeTimeAndRoomId(roomId: String, startTime: Date, endTime: Date): BigDecimal

    @Query("select COALESCE(avg(trade_price), 0) from $table " +
            " where trade_time >= :startTime " +
            " and trade_time <= :endTime " +
            " and company_id = :companyId ")
    suspend fun avgPriceByTradeTimeAndCompanyId(startTime: Date, endTime: Date, companyId: Int): BigDecimal

    @Query("select COALESCE(avg(trade_price), 0) from $table " +
            " where trade_time >= :startTime " +
            " and trade_time <= :endTime " +
            " and stock_id = :stockId ")
    suspend fun avgPriceByTradeTimeAndStockId(startTime: Date, endTime: Date, stockId: Int): BigDecimal

    @Query("select t.*, s.real_name as sellerName from $table t " +
            " left join mt_stockholder s on s.user_id = t.seller_id and s.company_id = t.company_id " +
            " where t.id = :id")
    suspend fun findDetailsById(id: Int): TradeDetails?

    companion object {
        const val table = "mt_trade_info"
    }
}