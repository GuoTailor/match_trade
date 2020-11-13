package com.mt.mtengine.service

import com.mt.mtcommon.toLocalDateTime
import com.mt.mtengine.dao.PositionsDao
import com.mt.mtengine.dao.TradeInfoDao
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalTime

/**
 * Created by gyh on 2020/5/2.
 */
@Service
class PositionsService {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    private lateinit var positionsDao: PositionsDao
    @Autowired
    private lateinit var tradeInfoDao: TradeInfoDao

    /**
     * 注意使用了排他锁，需要在事务上执行才会生效
     */
    fun getUserPositions(companyId: Int, stockId: Int, userId: Int) = positionsDao.findBy(companyId, stockId, userId)

    fun addAmount(companyId: Int, stockId: Int, userId: Int, amount: Int): Mono<Int> {
        val startTime = LocalTime.MIN.toLocalDateTime()
        val endTIme = LocalTime.MAX.toLocalDateTime()
        return getUserPositions(companyId, stockId, userId)
                .zipWith(tradeInfoDao.countAmountByTradeTimeAndCompanyIdAndUserId(startTime, endTIme, companyId, userId)) { o1, o2 ->
                    if (o1.limit!! <= o2.toInt())
                        error("{$o1}达到交易上限")
                    o1
                }.switchIfEmpty(Mono.error(RuntimeException("没有这个 companyId: $companyId stockId: $stockId userId: $userId")))
                .flatMap { positionsDao.updateAmount(it.id!!, it.amount!! + amount) }
    }

    fun minusAmount(companyId: Int, stockId: Int, userId: Int, amount: Int): Mono<Int> {
        val startTime = LocalTime.MIN.toLocalDateTime()
        val endTIme = LocalTime.MAX.toLocalDateTime()
        return getUserPositions(companyId, stockId, userId)
                .zipWith(tradeInfoDao.countAmountByTradeTimeAndCompanyIdAndUserId(startTime, endTIme, companyId, userId)) { o1, o2 ->
                    if (o1.limit!! <= o2.toInt()) error("{$o1}达到交易上限")
                    o1
                }.switchIfEmpty(Mono.error(RuntimeException("没有这个 companyId: $companyId stockId: $stockId userId: $userId\"")))
                .flatMap { positionsDao.updateAmount(it.id!!, it.amount!! - amount) }   // 股票可以为负
    }
}
