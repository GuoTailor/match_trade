package com.mt.mtengine.dao

import com.mt.mtcommon.TradeInfo
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Created by gyh on 2020/5/2.
 */
interface TradeInfoDao : ReactiveCrudRepository<TradeInfo, Int> {

    @Query("select COALESCE(sum(trade_amount), 0) from mt_trade_info where trade_time between :startTime and :endTime and company_id = :companyId and (buyer_id = :userId or seller_id = :userId)")
    fun countAmountByTradeTimeAndCompanyIdAndUserId(startTime: LocalDateTime, endTime: LocalDateTime, companyId: Int, userId: Int): Mono<BigDecimal>

}