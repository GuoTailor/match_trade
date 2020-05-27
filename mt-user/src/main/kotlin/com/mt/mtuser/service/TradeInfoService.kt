package com.mt.mtuser.service

import com.mt.mtcommon.*
import com.mt.mtuser.common.Util
import com.mt.mtuser.dao.TradeInfoDao
import com.mt.mtuser.entity.Overview
import com.mt.mtuser.entity.Stockholder
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.page.getPage
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitOne
import org.springframework.data.r2dbc.core.from
import org.springframework.data.relational.core.query.Criteria
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
    suspend fun countMoneyByTradeTimeAndCompanyId(startTime: Date = LocalTime.MIN.toDate()): Long {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        return tradeInfoDao.countMoneyByTradeTimeAndCompanyId(startTime, Date(), companyId)
    }

    /**
     * 获取昨天的收盘价，也就是今天的开盘价
     */
    suspend fun getYesterdayClosingPriceByCompanyId(): BigDecimal {
        // TODO 交易失败的不计入计算
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        val startTime = System.currentTimeMillis() - LocalTime.now().toMillisOfDay() - LocalTime.MAX.toMillisOfDay()
        val endTime = System.currentTimeMillis() - LocalTime.now().toMillisOfDay()
        return tradeInfoDao.findLastPriceByTradeTimeAndCompanyId(startTime.toDate(), endTime.toDate(), companyId)
                ?: BigDecimal(0)
    }

    suspend fun getYesterdayClosingPriceByRoomId(roomId: String): BigDecimal {
        val startTime = System.currentTimeMillis() - LocalTime.now().toMillisOfDay() - LocalTime.MAX.toMillisOfDay()
        val endTime = System.currentTimeMillis() - LocalTime.now().toMillisOfDay()
        return tradeInfoDao.findLastPriceByTradeTimeAndRoomId(startTime.toDate(), endTime.toDate(), roomId)
                ?: BigDecimal(0)
    }

    suspend fun getClosingPriceByRoomId(roomId: String, startTime: Date, endTime: Date): BigDecimal {
        return tradeInfoDao.findLastPriceByTradeTimeAndRoomId(startTime, endTime, roomId) ?: BigDecimal(0)
    }

    /**
     * 获取今天的最新一次报价
     */
    suspend fun getTodayOpeningPriceByCompanyId(): BigDecimal {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        val startTime = System.currentTimeMillis() - LocalTime.now().toMillisOfDay()
        val endTime = System.currentTimeMillis()
        return tradeInfoDao.findLastPriceByTradeTimeAndCompanyId(startTime.toDate(), endTime.toDate(), companyId)
                ?: BigDecimal(0)
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
        return finMaxMinPriceByTradeTimeAndRoomId(roomId, startTime, endTime)
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
    suspend fun findOrder(roomId: String, query: PageQuery, startTime: Date, endTime: Date): PageView<TradeInfo> {
        val where = query.where()
                .and("room_id").`is`(roomId)
                .and("trade_time").greaterThan(startTime)
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
    suspend fun statisticsOrderByDay() {

    }

    suspend fun finMaxMinPriceByTradeTimeAndRoomId(roomId: String, startTime: Date, endTime: Date): Map<String, BigDecimal> {
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

    suspend fun statistics(startTime: Date, endTime: Date, companyId: Int) {
        connect.execute("select DATE(trade_time) as date" +
                " COALESCE(max(trade_price), 0) as tradesCapacity," +
                " COALESCE(min(trade_price), 0) as tradesVolume," +
                " COALESCE(avg(trade_price), 0) as avgPrice " +
                " from ${TradeInfoDao.table} " +
                " where trade_time between :startTime and :endTime " +
                " and company_id = :companyId " +
                " group by date")
                .bind("startTime", startTime)
                .bind("endTime", endTime)
                .bind("companyId", companyId)
                .map { r, _ ->
                    mapOf("tradesCapacity" to r.get("tradesCapacity", java.lang.Long::class.java),
                            "tradesVolume" to r.get("tradesVolume", BigDecimal::class.java),
                            "avgPrice" to r.get("avgPrice", BigDecimal::class.java),
                            "date" to r.get("avgPrice", Date::class.java))
                }.awaitOne()
    }

    suspend fun statisticsOverview(startTime: Date, endTime: Date, companyId: Int): Map<String, *>? {
        return connect.execute("select DATE(trade_time) as date" +
                " COALESCE(sum(trade_amount), 0) as tradesCapacity," +
                " COALESCE(sum(trade_price), 0) as tradesVolume," +
                " COALESCE(avg(trade_price), 0) as avgPrice " +
                " from ${TradeInfoDao.table} " +
                " where trade_time between :startTime and :endTime " +
                " and company_id = :companyId " +
                " group by date")
                .bind("startTime", startTime)
                .bind("endTime", endTime)
                .bind("companyId", companyId)
                .map { r, _ ->
                    mapOf("tradesCapacity" to r.get("tradesCapacity", java.lang.Long::class.java),
                            "tradesVolume" to r.get("tradesVolume", BigDecimal::class.java),
                            "avgPrice" to r.get("avgPrice", BigDecimal::class.java),
                            "date" to r.get("avgPrice", Date::class.java))
                }.awaitOne()
    }

}