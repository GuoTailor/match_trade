package com.mt.mtengine.dao

import com.mt.mtengine.entity.Stockholder
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Created by gyh on 2020/3/17.
 */
interface StockholderDao: ReactiveCrudRepository<Stockholder, Int> {

    @Query("select * from mt_stockholder where user_id = :userId and company_id = :companyId for update")
    fun findByUserIdAndCompanyId(userId: Int,  companyId : Int): Mono<Stockholder>

    @Modifying
    @Query("update mt_stockholder set money = :money where id = :id")
    fun updateMoney(id: Int, money: BigDecimal): Mono<Int>
}