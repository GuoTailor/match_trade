package com.mt.mtuser.dao

import com.mt.mtcommon.TradeInfo
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.math.BigDecimal
import java.util.*

/**
 * Created by gyh on 2020/5/2.
 */
interface TradeInfoDao : CoroutineCrudRepository<TradeInfo, Int> {

    @Query("select COALESCE(sum(trade_amount), 0) from $table where trade_time between :startTime and :endTime")
    suspend fun countStockByTradeTime(startTime: Date, endTime: Date): Long

    @Query("select COALESCE(sum(trade_amount), 0) from $table where trade_time between :startTime and :endTime and company_id = :companyId")
    suspend fun countStockByTradeTimeAndCompanyId(startTime: Date, endTime: Date, companyId: Int): Long

    @Query("select COALESCE(sum(trade_amount), 0) from $table where trade_time between :startTime and :endTime and stock_id = :stockId")
    suspend fun countStockByTradeTimeAndStockId(startTime: Date, endTime: Date, stockId: Int): Long

    @Query("select COALESCE(sum(trade_money), 0) from $table where trade_time between :startTime and :endTime")
    suspend fun countMoneyTradeTime(startTime: Date, endTime: Date): Long

    @Query("select COALESCE(sum(trade_money), 0) from $table where trade_time between :startTime and :endTime and company_id = :companyId")
    suspend fun countMoneyByTradeTimeAndCompanyId(startTime: Date, endTime: Date, companyId: Int): Long

    @Query("select COALESCE(sum(trade_money), 0) from $table where trade_time between :startTime and :endTime and stock_id = :stockId")
    suspend fun countMoneyByTradeTimeAndStockId(startTime: Date, endTime: Date, stockId: Int): Long

    @Query("select trade_price from $table " +
            " where trade_time between :startTime and :endTime " +
            " and company_id = :companyId " +
            " order by trade_time desc limit 1")
    suspend fun findLastPriceByTradeTimeAndCompanyId(startTime: Date, endTime: Date, companyId: Int): BigDecimal?

    @Query("select trade_price from $table " +
            " where trade_time between :startTime and :endTime " +
            " and stock_id = :stockId " +
            " order by trade_time desc limit 1")
    suspend fun findLastPriceByTradeTimeAndStockId(startTime: Date, endTime: Date, stockId: Int): BigDecimal?

    @Query("select trade_price from $table " +
            " where trade_time between :startTime and :endTime " +
            " and room_id = :roomId " +
            " order by trade_time desc limit 1")
    suspend fun findLastPriceByTradeTimeAndRoomId(startTime: Date, endTime: Date, roomId: String): BigDecimal?

    @Query("select COALESCE(max(trade_price), 0) from $table " +
            " where trade_time between :startTime and :endTime " +
            " and room_id = :roomId ")
    suspend fun findMaxPriceByTradeTimeAndRoomId(startTime: Date, endTime: Date, roomId: String): BigDecimal

    @Query("select COALESCE(min(trade_price), 0) from $table " +
            " where trade_time between :startTime and :endTime " +
            " and room_id = :roomId ")
    suspend fun findMinPriceByTradeTimeAndRoomId(roomId: String, startTime: Date, endTime: Date): BigDecimal

    @Query("select COALESCE(avg(trade_price), 0) from $table " +
            " where trade_time between :startTime and :endTime " +
            " and company_id = :companyId ")
    suspend fun avgPriceByTradeTimeAndCompanyId(startTime: Date, endTime: Date, companyId: Int): BigDecimal

    @Query("select COALESCE(avg(trade_price), 0) from $table " +
            " where trade_time between :startTime and :endTime " +
            " and stock_id = :stockId ")
    suspend fun avgPriceByTradeTimeAndStockId(startTime: Date, endTime: Date, stockId: Int): BigDecimal

    @Query("select t.* from $table t " +
            " where t.id = :id")
    suspend fun findDetailsById(id: Int): TradeInfo?

    companion object {
        const val table = "mt_trade_info"
        const val sql = "id,company_id,stock_id,room_id,model,buyer_id,buyer_price,seller_id,seller_price,trade_price,trade_time,trade_state," +
                "state_details,trade_amount,trade_money,buyer_name,seller_name"
    }
}