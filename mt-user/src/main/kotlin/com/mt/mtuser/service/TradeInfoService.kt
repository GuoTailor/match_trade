package com.mt.mtuser.service

import com.mt.mtcommon.*
import com.mt.mtuser.common.Util
import com.mt.mtuser.dao.TradeInfoDao
import com.mt.mtuser.entity.Kline
import com.mt.mtuser.entity.Overview
import com.mt.mtuser.entity.Stockholder
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.page.getPage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitOne
import org.springframework.data.r2dbc.core.from
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalTime
import java.util.*

/**
 * Created by gyh on 2020/5/7.
 */
@Service
class TradeInfoService {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private lateinit var tradeInfoDao: TradeInfoDao

    @Autowired
    private lateinit var roleService: RoleService

    @Autowired
    private lateinit var stockService: StockService

    @Autowired
    private lateinit var connect: DatabaseClient

    @Autowired
    private lateinit var roomRecordService: RoomRecordService

    /**
     * 获取今天的交易量
     */
    suspend fun countStockByTradeTime(startTime: Date = LocalTime.MIN.toDate()) = tradeInfoDao.countStockByTradeTime(startTime, Date())

    /**
     * 获取公司今天的交易量
     */
    suspend fun countStockByTradeTimeAndCompanyId(startTime: Date = LocalTime.MIN.toDate()): Long {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        //val stockId = stockService.findByCompanyId(companyId).first()// TODO 替换为股票id
        return tradeInfoDao.countStockByTradeTimeAndCompanyId(startTime, Date(), companyId)
    }

    /**
     * 获取今天交易额
     */
    suspend fun countMoneyByTradeTime(startTime: Date = LocalTime.MIN.toDate()) = tradeInfoDao.countMoneyTradeTime(startTime, Date())

    /**
     * 获取公司今天的交易额
     */
    suspend fun countMoneyByTradeTimeAndCompanyId(startTime: Date = LocalTime.MIN.toDate()): BigDecimal {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        return tradeInfoDao.countMoneyByTradeTimeAndCompanyId(startTime, Date(), companyId)
    }

    /**
     * 获取昨天的收盘价，也就是今天的开盘价
     */
    suspend fun getYesterdayClosingPriceByCompanyId(): BigDecimal {
        // TODO 交易失败的不计入计算
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        // 今天凌晨
        val endTime = System.currentTimeMillis() - LocalTime.now().toMillisOfDay()
        return tradeInfoDao.findLastPriceByTradeTimeAndCompanyId(endTime.toDate(), companyId) ?: BigDecimal(0)
    }

    suspend fun getYesterdayClosingPriceByRoomId(roomId: String): BigDecimal {
        val startTime = System.currentTimeMillis() - LocalTime.now().toMillisOfDay() - LocalTime.MAX.toMillisOfDay()
        val endTime = System.currentTimeMillis() - LocalTime.now().toMillisOfDay()
        return tradeInfoDao.findLastPriceByTradeTimeAndRoomId(startTime.toDate(), endTime.toDate(), roomId)
                ?: BigDecimal(0)
    }

    suspend fun getClosingPriceByRoomId(roomId: String, startTime: Date, endTime: Date): BigDecimal? {
        return tradeInfoDao.findLastPriceByTradeTimeAndRoomId(startTime, endTime, roomId)
    }

    suspend fun getOpenPriceByRoomId(startTime: Date, endTime: Date, roomId: String) =
            tradeInfoDao.findFirstPriceByTradeTimeAndRoomId(startTime, endTime, roomId) ?: BigDecimal(0)

    /**
     * 获取今天的最新一次报价
     */
    suspend fun getTodayOpeningPriceByCompanyId(): BigDecimal {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        return tradeInfoDao.findLastPriceByTradeTimeAndCompanyId(Date(), companyId) ?: BigDecimal(0)
    }

    /**
     * 获取今天的平均报价
     */
    suspend fun getAvgPriceByCompanyId(): BigDecimal {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        val startTime = System.currentTimeMillis() - LocalTime.now().toMillisOfDay()
        val endTime = System.currentTimeMillis()
        return tradeInfoDao.avgPriceByTradeTimeAndCompanyId(startTime.toDate(), endTime.toDate(), companyId)
    }

    suspend fun buyOverview(startTime: Date, endTime: Date, companyId: Int, buyerId: Int): Overview {
        return connect.execute("select COALESCE(sum(trade_amount), 0) as buyStock," +
                " COALESCE(sum(trade_money), 0) as buyMoney," +
                " COALESCE(avg(trade_price), 0) as avgBuyMoney " +
                " from ${TradeInfoDao.table} " +
                " where trade_time >= :startTime " +
                " and trade_time <= :endTime " +
                " and company_id = :companyId " +
                " and buyer_id = :buyerId")
                .bind("startTime", startTime)
                .bind("endTime", endTime)
                .bind("companyId", companyId)
                .bind("buyerId", buyerId)
                .map { r, _ ->  // 我不知道为什么不能用 as方法进行转换
                    val buyStock = r.get("buyStock", java.lang.Long::class.java)
                    val buyMoney = r.get("buyMoney", BigDecimal::class.java)
                    val avgBuyMoney = r.get("avgBuyMoney", BigDecimal::class.java)
                    Overview(buyStock = buyStock!!.toLong(), buyMoney = buyMoney, avgBuyMoney = avgBuyMoney)
                }.one()
                .awaitSingle()
    }

    suspend fun sellOverview(startTime: Date, endTime: Date, companyId: Int, sellId: Int): Overview {
        return connect.execute("select COALESCE(sum(trade_amount), 0) as sellStock," +
                " COALESCE(sum(trade_money), 0) as sellMoney," +
                " COALESCE(avg(trade_price), 0) as avgSellMoney " +
                " from ${TradeInfoDao.table} " +
                " where trade_time >= :startTime " +
                " and trade_time <= :endTime " +
                " and company_id = :companyId " +
                " and seller_id = :sellId")
                .bind("startTime", startTime)
                .bind("endTime", endTime)
                .bind("companyId", companyId)
                .bind("sellId", sellId)
                .map { r, _ ->
                    val sellStock = r.get("sellStock", java.lang.Long::class.java)
                    val sellMoney = r.get("sellMoney", BigDecimal::class.java)
                    val avgSellMoney = r.get("avgSellMoney", BigDecimal::class.java)
                    Overview(sellStock = sellStock!!.toLong(), sellMoney = sellMoney, avgSellMoney = avgSellMoney)
                }.one()
                .awaitSingle()
    }

    /**
     * 获取今日成交概述
     */
    suspend fun dayOverview(userId: Int, companyId: Int): Overview {
        val startTime = System.currentTimeMillis() - LocalTime.now().toMillisOfDay()
        val endTime = System.currentTimeMillis()
        val buyOverview = buyOverview(startTime.toDate(), endTime.toDate(), companyId, userId)
        val sellOverview = sellOverview(startTime.toDate(), endTime.toDate(), companyId, userId)
        buyOverview.copyNotNullField(sellOverview)
        buyOverview.computeNetBuy()
        return buyOverview
    }

    /**
     * 获取本月成交概述
     */
    suspend fun monthOverview(userId: Int, companyId: Int): Overview {
        val startTime = minDay()
        val endTime = maxDay()
        val buyOverview = buyOverview(startTime, endTime, companyId, userId)
        val sellOverview = sellOverview(startTime, endTime, companyId, userId)
        buyOverview.copyNotNullField(sellOverview)
        buyOverview.computeNetBuy()
        return buyOverview
    }

    /**
     * 获取指定时间范围的最大和最小报价
     */
    suspend fun getMaxMinPrice(roomId: String, startTime: Date, endTime: Date): Map<String, BigDecimal> {
        return findMaxMinPriceByTradeTimeAndRoomId(roomId, startTime, endTime)
    }

    /**
     * 获取订单详情
     */
    suspend fun findDetailsById(id: Int): TradeInfo? {
        return tradeInfoDao.findDetailsById(id)
    }

    /**
     * 查询指定时间内的历史订单
     */
    suspend fun findOrder(roomId: String, query: PageQuery, endTime: Date): PageView<TradeInfo> {
        val where = query.where()
                .and("room_id").`is`(roomId)
                .and("trade_time").lessThan(endTime)
        return getPage(connect.select()
                .from<TradeInfo>()
                .matching(where)
                .page(query.page())
                .fetch()
                .all()
                , connect, query, where)
    }

    /**
     * 查找指定房间的历史订单
     */
    suspend fun findOrder(roomId: String, query: PageQuery): PageView<TradeInfo> {
        val where = query.where().and("room_id").`is`(roomId)
        return getPage(connect.select()
                .from<TradeInfo>()
                .matching(where)
                .page(query.page())
                .fetch()
                .all()
                , connect, query, where)
    }

    /**
     * 查询指定用户的交易记录
     */
    suspend fun findOrderByUserId(userId: Int, query: PageQuery, isBuy: Boolean?, date: Date): PageView<TradeInfo> {
        val endTime = Date(date.time + LocalTime.MAX.toMillisOfDay())
        val where = when {
            isBuy == null -> query.where().and(where("buyer_id").`is`(userId).or("seller_id").`is`(userId))
            isBuy -> query.where().and(where("buyer_id").`is`(userId))
            else -> query.where().and(where("seller_id").`is`(userId))
        }.and(where("trade_time").between(date, endTime))
        // 无赖之举，使用connect.execute无法使用matching，只能手动拼接字符串，就必须格式化时间，
        // 而使用connect.select格式化时间后会抱怨：操作符不存在: timestamp without time zone >= character varying
        val countWhere = when {
            isBuy == null -> query.where().and(where("buyer_id").`is`(userId).or("seller_id").`is`(userId))
            isBuy -> query.where().and(where("buyer_id").`is`(userId))
            else -> query.where().and(where("seller_id").`is`(userId))
        }.and(where("trade_time").between("'${Util.createDate(date)}'", "'${Util.createDate(endTime)}'"))

        return getPage(connect.select()
                .from<TradeInfo>()
                .matching(where)
                .page(query.page())
                .fetch()
                .all()
                , connect, query, countWhere)
    }

    /**
     * 按天统计交易详情
     */
    suspend fun statisticsOrderByDay(page: PageQuery): PageView<Map<String, Any?>> {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        val stockId = stockService.findByCompanyId(companyId).first().id!!
        val where = page.where("k").and("k.stock_id").`is`(stockId)
        val pageSql = page.toPageSql()
        val sql = "select k.*, count(rr.id) as openNumber from mt_1d_kline k " +
                " left join mt_room_record rr " +
                " on rr.stock_id = k.stock_id " +
                " and rr.start_time between k.time - INTERVAL'1 day' and k.time - INTERVAL'1 sec' " +
                " where $where group by k.id $pageSql"
        return getPage(connect.execute(sql)
                .map { r, _ ->
                    mapOf("id" to r.get("id", java.lang.Long::class.java),
                            "stockId" to r.get("stock_id", java.lang.Integer::class.java),
                            "time" to Util.createDate(r.get("time", Date::class.java)),
                            "tradesCapacity" to r.get("trades_capacity", java.lang.Long::class.java),
                            "tradesVolume" to r.get("trades_volume", BigDecimal::class.java),
                            "tradesNumber" to r.get("trades_number", java.lang.Integer::class.java),
                            "avgPrice" to r.get("avg_price", BigDecimal::class.java),
                            "maxPrice" to r.get("max_price", BigDecimal::class.java),
                            "minPrice" to r.get("min_price", BigDecimal::class.java),
                            "openPrice" to r.get("open_price", BigDecimal::class.java),
                            "closePrice" to r.get("close_price", BigDecimal::class.java),
                            "companyId" to r.get("company_id", java.lang.Integer::class.java),
                            "openNumber" to r.get("openNumber", java.lang.Integer::class.java))
                }.all(), connect, page, "mt_1d_kline k", where)
    }

    /**
     * 获取指定时间范围的最大和最小报价
     */
    suspend fun findMaxMinPriceByTradeTimeAndRoomId(roomId: String, startTime: Date, endTime: Date): Map<String, BigDecimal> {
        return connect.execute("select COALESCE(min(trade_price), 0) as minPrice," +
                " COALESCE(max(trade_price), 0) as maxPrice from ${TradeInfoDao.table} " +
                " where trade_time between :startTime and :endTime " +
                " and room_id = :roomId ")
                .bind("startTime", startTime)
                .bind("endTime", endTime)
                .bind("roomId", roomId)
                .map { r, _ ->
                    mapOf("minPrice" to r.get("minPrice", BigDecimal::class.java)!!,
                            "maxPrice" to r.get("maxPrice", BigDecimal::class.java)!!)
                }.awaitOne()
    }

    suspend fun findMaxMinPriceByTradeTimeAndStockId(startTime: Date, endTime: Date, stockId: Int): Map<String, BigDecimal> {
        return connect.execute("select COALESCE(min(trade_price), 0) as minPrice," +
                " COALESCE(max(trade_price), 0) as maxPrice from ${TradeInfoDao.table} " +
                " where trade_time between :startTime and :endTime " +
                " and stock_id = :stockId ")
                .bind("startTime", startTime)
                .bind("endTime", endTime)
                .bind("stockId", stockId)
                .map { r, _ ->
                    mapOf("minPrice" to r.get("minPrice", BigDecimal::class.java)!!,
                            "maxPrice" to r.get("maxPrice", BigDecimal::class.java)!!)
                }.awaitOne()
    }


}