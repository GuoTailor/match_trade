package com.mt.mtuser.service.kline

import com.mt.mtcommon.toLocalDateTime
import com.mt.mtuser.entity.Kline
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitOne
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Created by gyh on 2020/6/8
 */
@Service
class Compute1DKlineService : ComputeKline() {
    override val tableName: String = "mt_1d_kline"
    val step = 24 * 60 * 60 * 1000L

    @Autowired
    private lateinit var connect: DatabaseClient

    override fun handlerRequest(time: Long): Boolean {
        val c = Calendar.getInstance()
        c.timeInMillis = time
        return c.get(Calendar.MINUTE) == 0 && c.get(Calendar.HOUR_OF_DAY) == 0
    }

    override fun formatDate(time: Long): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = time
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    override suspend fun getMinComputeTime(): LocalDateTime? {
        return connect.execute("select min(time) as time from mt_4h_kline")
                .map { t, _ -> Optional.ofNullable(t.get("time", LocalDateTime::class.java)) }
                .awaitOne().orElse(null)
    }

    override fun step(): Long = step

    override suspend fun compute(stockId: Int, companyId: Int, time: Long): Kline {
        val startTime = (time - step).toLocalDateTime()
        val endTime = time.toLocalDateTime()
        val kline = Kline()
        kline.stockId = stockId
        kline.companyId = companyId
        kline.time = time.toLocalDateTime()
        kline.openPrice = klineService.getClosePriceByTableName(startTime, stockId, "mt_4h_kline")
        kline.closePrice = klineService.getClosePriceByTableName(endTime, stockId, "mt_4h_kline")
        return connect.execute("select" +
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
}