package com.mt.mtuser.service

import com.mt.mtcommon.toDate
import com.mt.mtcommon.toMillisOfDay
import com.mt.mtuser.dao.TradeInfoDao
import com.mt.mtuser.entity.Stockholder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalTime
import java.util.*

/**
 * Created by gyh on 2020/5/7.
 */
@Service
class TradeInfoService  {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    private lateinit var tradeInfoDao: TradeInfoDao

    @Autowired
    private lateinit var roleService: RoleService
    @Autowired
    private lateinit var stockService: StockService

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
    suspend fun getYesterdayClosingPriceByCompanyId() {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        val startTime = System.currentTimeMillis() - LocalTime.now().toMillisOfDay() - LocalTime.MAX.toMillisOfDay()
        val endTime = System.currentTimeMillis() - LocalTime.now().toMillisOfDay()
        tradeInfoDao.findLastPriceByTradeTimeAndCompanyId(startTime.toDate(), endTime.toDate(), companyId)
    }

    /**
     * 获取今天的最新一次报价
     */
    suspend fun getTodayOpeningPriceByCompanyId(): BigDecimal {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        val startTime = System.currentTimeMillis() - LocalTime.now().toMillisOfDay()
        val endTime = System.currentTimeMillis()
        return tradeInfoDao.findLastPriceByTradeTimeAndCompanyId(startTime.toDate(), endTime.toDate(), companyId)
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
}