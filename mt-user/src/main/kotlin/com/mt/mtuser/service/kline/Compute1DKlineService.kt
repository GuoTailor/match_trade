package com.mt.mtuser.service.kline

import com.mt.mtuser.dao.TradeInfoDao
import com.mt.mtuser.entity.Kline
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.convert.EntityRowMapper
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.r2dbc.core.awaitOne
import org.springframework.r2dbc.core.awaitOneOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Created by gyh on 2020/6/8
 */
@Service
class Compute1DKlineService : ComputeKline() {
    override val tableName: String = "mt_1d_kline"
    val stepOfSeconds = 24 * 60 * 60L

    @Autowired
    private lateinit var template: R2dbcEntityTemplate

    @Autowired
    private lateinit var tradeInfoDao: TradeInfoDao

    override fun handlerRequest(time: LocalDateTime): Boolean {
        return time.minute == 0 && time.hour == 0 && time.plusSeconds(stepOfSeconds).isBefore(LocalDateTime.now())
    }

    override fun formatDate(time: LocalDateTime): LocalDateTime {
        return time.withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
    }

    override suspend fun getMinComputeTime(): LocalDateTime? {
        return template.databaseClient.sql("select min(time) as time from mt_4h_kline")
                .map { t, _ -> Optional.ofNullable(t.get("time", LocalDateTime::class.java)) }
                .awaitOne().orElse(null)
    }

    override fun step() = stepOfSeconds

    override suspend fun compute(stockId: Int, companyId: Int, time: LocalDateTime, offset: Long): Kline {
        val startTime = time.plusSeconds(offset)
        val endTime = time.plusSeconds(stepOfSeconds)
        val kline = Kline()
        kline.stockId = stockId
        kline.companyId = companyId
        kline.time = time
        kline.openPrice = klineService.getOpenPriceByTableName(startTime, endTime, stockId, "mt_4h_kline")
        kline.closePrice = klineService.getClosePriceByTableName(endTime, stockId, "mt_4h_kline")
        return template.databaseClient.sql("select" +
                " COALESCE(sum(trades_capacity), 0) as tradesCapacity," +
                " COALESCE(sum(trades_volume), 0) as tradesVolume," +
                " COALESCE(sum(trades_number), 0) as tradesNumber," +
                " COALESCE(avg(avg_price), 0) as avgPrice," +
                " COALESCE(max(max_price), 0) as maxPrice," +
                " COALESCE(min(min_price), 0) as minPrice" +
                " from mt_4h_kline " +
                " where time between :startTime and :endTime " +
                " and stock_id = :stockId ")
                .bind("startTime", startTime)
                .bind("endTime", endTime)
                .bind("stockId", stockId)
                .map { r, _ ->
                    kline.tradesCapacity = r.get("tradesCapacity", java.lang.Long::class.java)?.toLong()
                    kline.tradesVolume = r.get("tradesVolume", BigDecimal::class.java)
                    kline.tradesNumber = r.get("tradesNumber", java.lang.Long::class.java)?.toLong()
                    kline.avgPrice = r.get("avgPrice", BigDecimal::class.java)
                    kline.minPrice = r.get("minPrice", BigDecimal::class.java)
                    kline.maxPrice = r.get("maxPrice", BigDecimal::class.java)
                    kline
                }.awaitOne()
    }

    override suspend fun isExist(time: LocalDateTime, stockId: Int, companyId: Int): Boolean {
        return template.databaseClient.sql("select * from $tableName where time = :time and stock_id = :stockId and company_id = :companyId limit 1")
                .bind("time", time)
                .bind("stockId", stockId)
                .bind("companyId", companyId)
            .map(EntityRowMapper(Kline::class.java, template.converter))
            .awaitOneOrNull() != null
    }

    suspend fun computeCurrent(stockId: Int, companyId: Int) {
        val startTime = LocalDate.now().atStartOfDay()
        val endTime = LocalDateTime.now()
        val exist = isExist(startTime, stockId, companyId)
        val newKline = template.databaseClient.sql("select count(1) as tradesNumber," +
                " COALESCE(sum(trade_amount), 0) as tradesCapacity, " +
                " COALESCE(sum(trade_money), 0) as tradesVolume," +
                " COALESCE(avg(trade_price), 0) as avgPrice," +
                " COALESCE(min(trade_price), 0) as minPrice," +
                " COALESCE(max(trade_price), 0) as maxPrice" +
                " from ${TradeInfoDao.table} " +
                " where trade_time >= :startTime " +
                " and stock_id = :stockId ")
                .bind("startTime", startTime)
                .bind("stockId", stockId)
                .map { r, _ ->
                    val kline = Kline()
                    kline.tradesNumber = r.get("tradesNumber", java.lang.Long::class.java)?.toLong()
                    kline.tradesCapacity = r.get("tradesCapacity", java.lang.Long::class.java)?.toLong()
                    kline.tradesVolume = r.get("tradesVolume", BigDecimal::class.java)
                    kline.avgPrice = r.get("avgPrice", BigDecimal::class.java)
                    kline.minPrice = r.get("minPrice", BigDecimal::class.java)
                    kline.maxPrice = r.get("maxPrice", BigDecimal::class.java)
                    kline
                }.awaitOne()
        if (!newKline.isEmpty()) {
            newKline.stockId = stockId
            newKline.companyId = companyId
            newKline.time = startTime
            newKline.openPrice = tradeInfoDao.findFirstPriceByTradeTimeAndStockId(startTime, endTime, stockId)
            newKline.closePrice = tradeInfoDao.findLastPriceByTradeTimeAndStockId(endTime, stockId)
            if (exist) {
                klineService.updateKline(newKline, tableName)
            } else {
                klineService.saveKline(newKline, tableName)
            }
        }
    }
}