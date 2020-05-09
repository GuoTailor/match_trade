package com.mt.mtengine.service

import com.mt.mtengine.dao.StockholderDao
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Created by gyh on 2020/5/9.
 */
@Service
class StockholderService {
    @Autowired
    private lateinit var stockholderDao: StockholderDao

    /**
     * 注意该查询使用了排他锁，由于reactive编程不能使用synchronized加锁，于是只能在数据库的行上加锁
     */
    fun findByUserIdAndCompanyId(userId: Int, companyId: Int) = stockholderDao.findByUserIdAndCompanyId(userId, companyId)

    fun addMoney(userId: Int, companyId: Int, money: BigDecimal): Mono<Int> {
        return findByUserIdAndCompanyId(userId, companyId)
                .switchIfEmpty(Mono.error(IllegalStateException("没有这个 companyId: $companyId userId: $userId")))
                .flatMap { stockholderDao.updateMoney(it.id!!, it.money!!.add(money)) }
    }

    fun minusMoney(userId: Int, companyId: Int, money: BigDecimal): Mono<Int> {
        return findByUserIdAndCompanyId(userId, companyId)
                .switchIfEmpty(Mono.error(IllegalStateException("没有这个 companyId: $companyId userId: $userId")))
                .flatMap { stockholderDao.updateMoney(it.id!!, it.money!!.subtract(money)) }
    }
}