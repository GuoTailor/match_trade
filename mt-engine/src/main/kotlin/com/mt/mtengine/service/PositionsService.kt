package com.mt.mtengine.service

import com.mt.mtengine.dao.PositionsDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/5/2.
 */
@Service
class PositionsService {
    @Autowired
    private lateinit var positionsDao: PositionsDao

    /**
     * 注意使用了排他锁，需要在事务上执行才会生效
     */
    fun getUserPositions(companyId: Int, stockId: Int, userId: Int) = positionsDao.findBy(companyId, stockId, userId)


    fun addAmount(id: Int, amount: Int): Mono<Int> {
        return positionsDao.findById(id)
                .switchIfEmpty(Mono.error(IllegalStateException("没有这个id：$id")))
                .flatMap { positionsDao.updateAmount(it.id!!, it.amount!! + amount) }
    }

    fun addAmount(companyId: Int, stockId: Int, userId: Int, amount: Int): Mono<Int> {
        return getUserPositions(companyId, stockId, userId)
                .switchIfEmpty(Mono.error(IllegalStateException("没有这个 companyId: $companyId stockId: $stockId userId: $userId")))
                .flatMap { positionsDao.updateAmount(it.id!!, it.amount!! + amount) }
    }

    fun minusAmount(id: Int, amount: Int): Mono<Int> {
        return positionsDao.findById(id)
                .switchIfEmpty(Mono.error(IllegalStateException("没有这个id：$id")))
                .flatMap { positionsDao.updateAmount(it.id!!, it.amount!! - amount) }   // 股票可以为负
    }

    fun minusAmount(companyId: Int, stockId: Int, userId: Int, amount: Int): Mono<Int> {
        return getUserPositions(companyId, stockId, userId)
                .switchIfEmpty(Mono.error(IllegalStateException("没有这个 companyId: $companyId stockId: $stockId userId: $userId\"")))
                .flatMap { positionsDao.updateAmount(it.id!!, it.amount!! - amount) }   // 股票可以为负
    }
}