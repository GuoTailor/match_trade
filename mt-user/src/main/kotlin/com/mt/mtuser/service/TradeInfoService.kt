package com.mt.mtuser.service

import com.mt.mtcommon.toDate
import com.mt.mtuser.dao.TradeInfoDao
import com.mt.mtuser.entity.Role
import kotlinx.coroutines.flow.first
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalTime
import java.util.*

/**
 * Created by gyh on 2020/5/7.
 */
@Service
class TradeInfoService  {
    @Autowired
    private lateinit var tradeInfoDao: TradeInfoDao

    @Autowired
    private lateinit var roleService: RoleService
    @Autowired
    private lateinit var stockService: StockService

    suspend fun countStockByTradeTime(time: Date = LocalTime.MIN.toDate()) = tradeInfoDao.countStockByTradeTime(time)

    suspend fun countStockByTradeTimeAndCompanyId(time: Date = LocalTime.MIN.toDate()): Long {
        val companyId = roleService.getCompanyList(Role.ADMIN)[0]
        //val stockId = stockService.findByCompanyId(companyId).first()// TODO 替换为股票id
        return tradeInfoDao.countStockByTradeTimeAndCompanyId(time, companyId)
    }

    suspend fun countMoneyByTradeTime(time: Date = LocalTime.MIN.toDate()) = tradeInfoDao.countMoneyTradeTime(time)

    suspend fun countMoneyByTradeTimeAndCompanyId(time: Date = LocalTime.MIN.toDate()): Long {
        val companyId = roleService.getCompanyList(Role.ADMIN)[0]
        return tradeInfoDao.countMoneyByTradeTimeAndCompanyId(time, companyId)
    }

    suspend fun getYesterdayClosingPrice() {}
}