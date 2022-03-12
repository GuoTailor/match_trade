package com.mt.mtuser.service.kline

import com.mt.mtuser.common.Util
import com.mt.mtuser.entity.Kline
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.page.getPage
import com.mt.mtuser.entity.room.BaseRoom
import com.mt.mtuser.service.R2dbcService
import com.mt.mtuser.service.StockService
import com.mt.mtuser.service.room.RoomService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitOne
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.r2dbc.core.awaitRowsUpdated
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Created by gyh on 2020/6/6
 */
@Service
class KlineService : ApplicationRunner {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private lateinit var connect: DatabaseClient

    @Autowired
    private lateinit var stockService: StockService

    @Autowired
    private lateinit var roomService: RoomService

    @Autowired
    private lateinit var r2dbcService: R2dbcService

    @Autowired
    lateinit var compute1MKlineService: Compute1MKlineService

    @Autowired
    lateinit var compute15MKlineService: Compute15MKlineService

    @Autowired
    lateinit var compute1HKlineService: Compute1HKlineService

    @Autowired
    lateinit var compute4HKlineService: Compute4HKlineService

    @Autowired
    lateinit var compute1DKlineService: Compute1DKlineService

    lateinit var kLineChain: ComputeKline
    val lock = ReentrantLock()

    override fun run(args: ApplicationArguments?) {
        compute1MKlineService
                .setNext(compute15MKlineService)
                .setNext(compute1HKlineService)
                .setNext(compute4HKlineService)
                .setNext(compute1DKlineService)
        kLineChain = compute1MKlineService
        logger.info(kLineChain.tableName)
    }

    fun handler() = runBlocking {
        lock.lock()
        try {
            val list = stockService.findAll().toList()
            val countLatch = CountDownLatch(list.size)
            list.forEach { stock ->
                launch(Dispatchers.Default) {
                    try {
                        kLineChain.handler(stock.id!!, stock.companyId!!)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    } finally {
                        countLatch.countDown()
                    }
                }
            }
            countLatch.await()
            logger.info("kline计算完成 {}", Util.createDate())
        } finally {
            lock.unlock()
        }
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
                .fetch()
                .awaitRowsUpdated()
    }

    suspend fun updateKline(kline: Kline, tableName: String): Int {
        return connect.update()
                .table(tableName)
                .using(r2dbcService.getUpdate(kline))
                .matching(where("time").`is`(kline.time!!)
                        .and("stock_id").`is`(kline.stockId!!)
                        .and("company_id").`is`(kline.companyId!!))
                .fetch().awaitRowsUpdated()
    }

    suspend fun findKline(roomId: String, mode: String, timeline: String, page: PageQuery): PageView<Kline> {
        val baseDao = roomService.getBaseRoomDao<BaseRoom>(mode)
        val baseRoom = baseDao.findByRoomId(roomId) ?: error("没有该房间号：$roomId-$mode")
        return findKlineByStockId(baseRoom.stockId!!, timeline, page)
    }

    suspend fun findKlineByCompanyId(companyId: Int, timeline: String, page: PageQuery): PageView<Kline> {
        val stockId = stockService.findByCompanyId(companyId).toList()[0]
        return findKlineByStockId(stockId.id!!, timeline, page)
    }

    suspend fun findKlineByStockId(stockId: Int, timeline: String, page: PageQuery): PageView<Kline> {
        val table = when (timeline) {
            "1m" -> "mt_1m_kline"
            "15m" -> "mt_15m_kline"
            "1h" -> "mt_1h_kline"
            "4h" -> "mt_4h_kline"
            "1d" -> "mt_1d_kline"
            else -> error("不支持的timeline: $timeline")
        }
        val where = page.where().and("stock_id").`is`(stockId)
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
     * 获取开盘价
     */
    suspend fun getOpenPriceByTableName(startTime: LocalDateTime, endTime: LocalDateTime, stockId: Int, tableName: String): BigDecimal {
        return connect.execute("select open_price from $tableName where time between :startTime and :endTime and stock_id = :stockId order by time asc limit 1")
                .bind("startTime", startTime)
                .bind("endTime", endTime)
                .bind("stockId", stockId)
                .map { r, _ -> r.get("open_price", BigDecimal::class.java) }
                .awaitOneOrNull() ?: BigDecimal.ZERO
    }

    /**
     * 获取收盘价
     */
    suspend fun getClosePriceByTableName(endTime: LocalDateTime, stockId: Int, tableName: String): BigDecimal {
        return connect.execute("select close_price from $tableName where time < :endTime and stock_id = :stockId order by time desc limit 1")
                .bind("endTime", endTime)
                .bind("stockId", stockId)
                .map { r, _ -> r.get("close_price", BigDecimal::class.java) }
                .awaitOneOrNull() ?: BigDecimal.ZERO
    }

    /**
     * 获取开盘价
     */
    fun getWeekTraderInfo(): Mono<List<Map<String, Any?>>> {
        return connect.execute("select trades_capacity, trades_volume, time from mt_1d_kline order by time DESC limit 7")
                .map { r, _ ->
                    mapOf<String, Any?>("capacity" to r.get("trades_capacity", java.lang.Long::class.java),
                            "volume" to r.get("trades_volume", BigDecimal::class.java),
                            "time" to r.get("time", LocalDateTime::class.java))
                }.all().collectList()
    }

}