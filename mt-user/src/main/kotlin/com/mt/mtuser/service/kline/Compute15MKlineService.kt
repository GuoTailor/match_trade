package com.mt.mtuser.service.kline

import com.mt.mtuser.entity.Kline
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitOne
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

/**
 * Created by gyh on 2020/6/8
 */
@Service
class Compute15MKlineService : ComputeKline() {
    override val tableName: String = "mt_15m_kline"
    val step = 15 * 60 * 1000L

    @Autowired
    private lateinit var connect: DatabaseClient

    override fun handlerRequest(time: Long): Boolean {
        val c = Calendar.getInstance()
        c.timeInMillis = time
        return c.get(Calendar.MINUTE) % 15 == 0
    }

    override fun formatDate(time: Long): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = time
        c.set(Calendar.MINUTE, c.get(Calendar.MINUTE) / 15 * 15)    // 15的倍数
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    override suspend fun getMinComputeTime(): Date? {
        return connect.execute("select min(time) as time from mt_1m_kline")
                .map { t, _ -> Optional.ofNullable(t.get("time", Date::class.java)) }
                .awaitOne().orElse(null)
    }

    override fun step(): Long = step

    override suspend fun compute(stockId: Int, companyId: Int, time: Long): Kline {
        val startTime = Date(time - step)
        val endTime = Date(time)
        val kline = Kline()
        kline.stockId = stockId
        kline.companyId = companyId
        kline.time = Date(time)
        // TODO 也许获取开盘价和收盘价有更好的方法
        kline.openPrice = klineService.getClosePriceByTableName(startTime, stockId, "mt_1m_kline")
        kline.closePrice = klineService.getClosePriceByTableName(endTime, stockId, "mt_1m_kline")
        return connect.execute("select" +
                " COALESCE(sum(trades_capacity), 0) as tradesCapacity," +
                " COALESCE(sum(trades_volume), 0) as tradesVolume," +
                " COALESCE(sum(trades_number), 0) as tradesNumber," +
                " COALESCE(avg(avg_price), 0) as avgPrice," +
                " COALESCE(max(max_price), 0) as maxPrice," +
                " COALESCE(min(min_price), 0) as minPrice" +
                " from mt_1m_kline " +
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