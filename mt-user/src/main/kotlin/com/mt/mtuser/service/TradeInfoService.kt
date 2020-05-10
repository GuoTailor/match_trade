package com.mt.mtuser.service

import com.mt.mtcommon.*
import com.mt.mtuser.dao.TradeInfoDao
import com.mt.mtuser.entity.Company
import com.mt.mtuser.entity.Overview
import com.mt.mtuser.entity.Stockholder
import com.mt.mtuser.entity.TradeDetails
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.page.getPage
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.from
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

    suspend fun countStockByTradeTime(time: Date = LocalTime.MIN.toDate()) = tradeInfoDao.countStockByTradeTime(time)

    suspend fun countStockByTradeTimeAndCompanyId(time: Date = LocalTime.MIN.toDate()): Long {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        //val stockId = stockService.findByCompanyId(companyId).first()// TODO 替换为股票id
        return tradeInfoDao.countStockByTradeTimeAndCompanyId(time, companyId)
    }

    suspend fun countMoneyByTradeTime(time: Date = LocalTime.MIN.toDate()) = tradeInfoDao.countMoneyTradeTime(time)

    suspend fun countMoneyByTradeTimeAndCompanyId(time: Date = LocalTime.MIN.toDate()): Long {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        return tradeInfoDao.countMoneyByTradeTimeAndCompanyId(time, companyId)
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
                " COALESCE(sum(trade_price), 0) as buyMoney," +
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
                }
                .one()
                .awaitSingle()
    }

    suspend fun sellOverview(startTime: Date, endTime: Date, companyId: Int, sellId: Int): Overview {
        return connect.execute("select COALESCE(sum(trade_amount), 0) as sellStock," +
                " COALESCE(sum(trade_price), 0) as sellMoney," +
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
        val max = tradeInfoDao.findMaxPriceByTradeTimeAndRoomId(roomId, startTime, endTime)
        val min = tradeInfoDao.findMinPriceByTradeTimeAndRoomId(roomId, startTime, endTime)
        return mapOf("maxPrice" to max, "minPrice" to min)
    }

    /**
     * 获取订单详情
     */
    suspend fun findDetailsById(id: Int): TradeDetails? {
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

    suspend fun findOrder(roomId: String, query: PageQuery): PageView<TradeInfo> {
        val where = query.where()
                .and("room_id").`is`(roomId)
        return getPage(connect.select()
                .from<TradeInfo>()
                .matching(where)
                .page(query.page())
                .fetch()
                .all()
                , connect, query, where)
    }

}