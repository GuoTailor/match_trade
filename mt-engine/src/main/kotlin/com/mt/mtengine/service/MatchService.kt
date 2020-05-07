package com.mt.mtengine.service

import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.TradeInfo
import com.mt.mtcommon.TradeState
import com.mt.mtengine.mq.MatchSink
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Created by gyh on 2020/5/6.
 */
@Service
class MatchService {
    @Autowired
    private lateinit var sink: MatchSink

    @Autowired
    private lateinit var roomService: RoomService

    @Autowired
    private lateinit var positionsService: PositionsService

    @Autowired
    private lateinit var tradeInfoService: TradeInfoService

    @Autowired
    private lateinit var r2dbc: R2dbcService

    @Autowired
    private lateinit var redisUtil: RedisUtil

    fun onMatchSuccess(roomId: String, buy: OrderParam, sell: OrderParam) = r2dbc.withTransaction {
        roomService.findCompanyIdByRoomId(roomId)
                .flatMap { info ->
                    val threadInfo = TradeInfo(buy, sell, info.companyId, info.stockId)
                    threadInfo.tradePrice = buy.price?.add(sell.price)?.divide(BigDecimal(2))
                    threadInfo.tradeMoney = threadInfo.tradePrice?.multiply(BigDecimal(threadInfo.tradeAmount ?: 0))
                    threadInfo.tradeState = TradeState.SUCCESS
                    buy.onTrade(threadInfo)
                    sell.onTrade(threadInfo)
                    positionsService.addAmount(info.companyId, info.stockId, buy.userId!!, buy.number!!)
                            .flatMap { positionsService.minusAmount(info.companyId, info.stockId, sell.userId!!, sell.number!!) }
                            .flatMap { tradeInfoService.save(threadInfo) }
                }.flatMap { redisUtil.updateUserOrder(buy) }
                .flatMap { redisUtil.updateUserOrder(sell) }
                //.doOnSuccess { threadInfo -> sink.outTrade().send(MessageBuilder.withPayload(threadInfo).build()) }
                .doOnError { onMatchFailed(roomId, buy, sell, it.message ?: "失败") }
    }

    fun onMatchFailed(roomId: String, buy: OrderParam?, sell: OrderParam?, fileInfo: String) = r2dbc.withTransaction {
        roomService.findCompanyIdByRoomId(roomId)
                .flatMap {
                    val threadInfo = TradeInfo(buy, sell, it.companyId, it.stockId)
                    if (buy?.price != null && sell?.price != null)
                        threadInfo.tradePrice = buy.price?.add(sell.price)?.divide(BigDecimal(2))
                    threadInfo.tradeState = TradeState.FAILED
                    threadInfo.stateDetails = fileInfo
                    buy?.onTrade(threadInfo)
                    sell?.onTrade(threadInfo)
                    tradeInfoService.save(threadInfo)
                }.flatMap { buy?.let { redisUtil.updateUserOrder(it) } }
                .flatMap { sell?.let { redisUtil.updateUserOrder(it) } }
                //.doOnSuccess { threadInfo -> sink.outTrade().send(MessageBuilder.withPayload(threadInfo).build()) }
                .doOnError { onMatchError(buy, sell, it.message ?: "失败") }

    }

    fun onMatchError(buy: OrderParam?, sell: OrderParam?, fileInfo: String): Mono<Void> {
        val buyResult = buy?.let {
            it.tradeState = TradeState.FAILED
            it.stateDetails = fileInfo
            redisUtil.updateUserOrder(it)
        } ?: Mono.empty<Boolean>()
        val sellResult = sell?.let {
            it.tradeState = TradeState.FAILED
            it.stateDetails = fileInfo
            redisUtil.updateUserOrder(it)
        } ?: Mono.empty()
        return buyResult.zipWith(sellResult).then()
    }

}