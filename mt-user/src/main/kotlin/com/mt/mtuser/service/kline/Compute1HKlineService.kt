package com.mt.mtuser.service.kline

import com.mt.mtuser.entity.Kline
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitOne
import org.springframework.r2dbc.core.awaitOne
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Created by gyh on 2020/6/8
 */
@Service
class Compute1HKlineService : ComputeKline() {
    override val tableName: String = "mt_1h_kline"

    override fun handlerRequest(time: LocalDateTime): Boolean {
        return time.minute == 0 && time.plusSeconds(stepOfSeconds).isBefore(LocalDateTime.now())
    }

    val stepOfSeconds = 60 * 60L

    @Autowired
    private lateinit var template: R2dbcEntityTemplate

    override fun formatDate(time: LocalDateTime): LocalDateTime {
        return time.withMinute(0)
                .withSecond(0)
                .withNano(0)
    }

    override suspend fun getMinComputeTime(): LocalDateTime? {
        return template.databaseClient.sql("select min(time) as time from mt_15m_kline")
                .map { t, _ -> Optional.ofNullable(t.get("time", LocalDateTime::class.java)) }
                .awaitOne().orElse(null)
    }

    override fun step(): Long = stepOfSeconds
    override suspend fun isExist(time: LocalDateTime, stockId: Int, companyId: Int): Boolean = false

    override suspend fun compute(stockId: Int, companyId: Int, time: LocalDateTime, offset: Long): Kline {
        val startTime = time.plusSeconds(offset)
        val endTime = time.plusSeconds(stepOfSeconds)
        val kline = Kline()
        kline.stockId = stockId
        kline.companyId = companyId
        kline.time = endTime
        kline.openPrice = klineService.getOpenPriceByTableName(startTime, endTime, stockId, "mt_15m_kline")
        kline.closePrice = klineService.getClosePriceByTableName(endTime, stockId, "mt_15m_kline")
        return template.databaseClient.sql("select" +
                " COALESCE(sum(trades_capacity), 0) as tradesCapacity," +
                " COALESCE(sum(trades_volume), 0) as tradesVolume," +
                " COALESCE(sum(trades_number), 0) as tradesNumber," +
                " COALESCE(avg(avg_price), 0) as avgPrice," +
                " COALESCE(max(max_price), 0) as maxPrice," +
                " COALESCE(min(min_price), 0) as minPrice" +
                " from mt_15m_kline " +
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
}