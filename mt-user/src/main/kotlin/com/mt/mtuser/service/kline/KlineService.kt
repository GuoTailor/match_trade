package com.mt.mtuser.service.kline

import com.mt.mtcommon.toEpochMilli
import com.mt.mtuser.common.Util
import com.mt.mtuser.entity.Kline
import com.mt.mtuser.entity.logger
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.page.getPage
import com.mt.mtuser.entity.room.BaseRoom
import com.mt.mtuser.service.StockService
import com.mt.mtuser.service.room.RoomService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitOne
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.r2dbc.core.awaitRowsUpdated
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.locks.ReentrantLock

/**
 * Created by gyh on 2020/6/6
 */
@Service
class KlineService {
    @Autowired
    private lateinit var connect: DatabaseClient

    @Autowired
    private lateinit var stockService: StockService

    @Autowired
    private lateinit var roomService: RoomService

    val klineServiceList = TreeSet<ComputeKline>()
    val lock = ReentrantLock()

    suspend fun start(stockId: Int, companyId: Int) {
        klineServiceList.forEach {
            // 获取计算k线的最后一次计算时间，用于在服务重启后，从上一次k线计算时间开始计算。
            // 如果计算服务（ComputeKline）记录的最后一次计算时间为null，就说明服务重启过，
            // 就只能从该k线的数据库表里面获取最近一次k线的计算时间（也就是表里面的最大时间），
            // 如果该表里面没有记录任何数据（也就是k线一次都还没计算过）就从计算用的数据表（这个表对于一分钟的k线来说就是订单表，
            // 对于十五分钟的k线来说就是一分钟的k线表）里获取最小的时间用于从头开始计算k线
            var lastTime = it.getLastTime(stockId)
            val now = System.currentTimeMillis()
            if (lastTime == null) { // 如果是刚启动就查表获取该k线图的最后一次计算时间
                // 如果该k线没有参与计算过就从头开始计算
                lastTime = getLastTimeByKline(it.tableName)?.toEpochMilli() ?: it.getMinComputeTime()?.toEpochMilli() ?: return@forEach
                // 加一个步长，用于计算当前时间的k线，比如计算5-18 00:00的日k就应该计算5-18 00:00 到 5-18 23:59,而不是5-17 00:00 到 5-17 23:59
                lastTime = it.formatDate(lastTime) + it.step()
                for (time in lastTime..now step it.step()) {
                    logger.info("计算k线 {} {} {}", stockId, companyId, Util.createDate(time))
                    val kline = it.handler(stockId, companyId, time)
                    if (!kline.isEmpty()) {
                        saveKline(kline, it.tableName)
                    }
                }
            } else if (it.handlerRequest(now)) {
                // 加一个步长,不然上一次计算的时间点会被重新计算一次
                lastTime = it.formatDate(lastTime) + it.step()
                for (time in lastTime..now step it.step()) {
                    logger.info("计算k线 {} {} {} {}", it.tableName, stockId, companyId, Util.createDate(time))
                    val kline = it.handler(stockId, companyId, time)
                    if (!kline.isEmpty()) {
                        saveKline(kline, it.tableName)
                    }
                }
            }
        }
    }

    fun handler() = runBlocking {
        lock.lock()
        try {
            val list = stockService.findAll().toList()
            val countLatch = CountDownLatch(list.size)
            list.forEach { stock ->
                launch(Dispatchers.Default) {
                    start(stock.id!!, stock.companyId!!)
                    countLatch.countDown()
                }
            }
            countLatch.await()
            logger.info("kline计算完成 {}", Util.createDate())
        } finally {
            lock.unlock()
        }
    }

    fun register(computeKline: ComputeKline) {
        klineServiceList.add(computeKline)
    }

    suspend fun getLastTimeByKline(tableName: String): LocalDateTime? {
        return connect.execute("select max(time) as time from $tableName")
                .map { t, _ -> Optional.ofNullable(t.get("time", LocalDateTime::class.java)) }
                .awaitOne().orElse(null)
    }

    suspend fun saveKline(kline: Kline, tableName: String): Int {
        return connect.insert().into(tableName)
                .value("stock_id", kline.stockId!!)
                .value("company_id", kline.companyId!!)
                .value("time", kline.time!!)
                .value("trades_capacity", kline.tradesCapacity!!)
                .value("trades_volume", kline.tradesVolume!!)
                .value("trades_number", kline.tradesNumber!!)
                .value("avg_price", kline.avgPrice!!)
                .value("max_price", kline.maxPrice!!)
                .value("min_price", kline.minPrice!!)
                .value("open_price", kline.openPrice!!)
                .value("close_price", kline.closePrice!!)
                .fetch().awaitRowsUpdated()
    }

    suspend fun findKline(roomId: String, mode: String, timeline: String, page: PageQuery): PageView<Kline> {
        val baseDao = roomService.getBaseRoomDao<BaseRoom>(mode)
        val baseRoom = baseDao.findByRoomId(roomId) ?: error("没有该房间号：$roomId-$mode")
        val table = when(timeline) {
            "1m" -> "mt_1m_kline"
            "15m" -> "mt_15m_kline"
            "1h" -> "mt_1h_kline"
            "4h" -> "mt_4h_kline"
            "1d" -> "mt_1d_kline"
            else -> error("不支持的timeline: $timeline")
        }
        val where = page.where().and("stock_id").`is`(baseRoom.stockId!!)
        return getPage(connect.select()
                .from(table)
                .matching(where)
                .page(page.page())
                .`as`(Kline::class.java)
                .fetch()
                .all()
                , connect, page, table, where)
    }

    /**
     * 获取收盘价，这次的收盘价也可以用于下一次开盘的开盘价
     */
    suspend fun getClosePriceByTableName(endTime: LocalDateTime, stockId: Int, tableName: String): BigDecimal? {
        return connect.execute("select open_price from $tableName where time < :endTime and stock_id = :stockId order by time desc limit 1")
                .bind("endTime", endTime)
                .bind("stockId", stockId)
                .map { r, _ -> r.get("open_price", BigDecimal::class.java) }
                .awaitOneOrNull()
    }

}