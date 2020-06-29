package com.mt.mtengine.service

import com.mt.mtcommon.*
import com.mt.mtengine.match.strategy.MatchStrategy
import com.mt.mtengine.mq.MatchSink
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.util.*

/**
 * Created by gyh on 2020/5/6.
 */
@Service
class MatchService {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private lateinit var sink: MatchSink

    @Autowired
    private lateinit var roomService: RoomService

    @Autowired
    private lateinit var positionsService: PositionsService

    @Autowired
    private lateinit var tradeInfoService: TradeInfoService

    @Autowired
    private lateinit var stockholderService: StockholderService

    @Autowired
    private lateinit var r2dbc: R2dbcService

    @Autowired
    private lateinit var redisUtil: RedisUtil

    fun <T: MatchStrategy.RoomInfo> onMatchSuccess(roomInfo: T, buy: OrderParam, sell: OrderParam, isTopThree: Boolean = false) = r2dbc.withTransaction {
        roomService.findCompanyIdByRoomId(roomInfo.roomId, roomInfo.mode).flatMap { info ->
            val threadInfo = TradeInfo(buy, sell, roomInfo.roomId, info.companyId, info.stockId, roomInfo.mode)
            threadInfo.tradePrice = buy.price?.add(sell.price)?.divide(BigDecimal(2))
            threadInfo.tradeMoney = threadInfo.tradePrice?.multiply(BigDecimal(threadInfo.tradeAmount ?: 0))
            threadInfo.tradeState = TradeState.SUCCESS
            buy.onTrade(threadInfo)
            sell.onTrade(threadInfo)
            positionsService.addAmount(info.companyId, info.stockId, buy.userId!!, threadInfo.tradeAmount!!)        // 添加买家的持股数
                    .flatMap { positionsService.minusAmount(info.companyId, info.stockId, sell.userId!!, threadInfo.tradeAmount!!) }// 减少卖价的持股数
                    .flatMap { stockholderService.addMoney(sell.userId!!, info.companyId, threadInfo.tradeMoney!!) }    // 添加卖家的钱
                    .flatMap { stockholderService.minusMoney(buy.userId!!, info.companyId, threadInfo.tradeMoney!!) }   // 减少买家的钱
                    .flatMap { tradeInfoService.save(threadInfo) }
                    .flatMap { redisUtil.updateUserOrder(buy) }
                    .flatMap { redisUtil.updateUserOrder(sell) }
                    .flatMap { redisUtil.setTradeInfo(threadInfo, roomInfo.endTime) }
                    .filter { isTopThree }
                    .flatMap { redisUtil.setRoomLastOrder(threadInfo.toOrderInfo()) }
                    .map { sink.outResult().send(MessageBuilder.withPayload(threadInfo.toOrderInfo().toFirstOrder(roomInfo.roomId, roomInfo.mode)).build()) }
                    .thenReturn(threadInfo)
        }.doOnSuccess { threadInfo ->
            sink.outTrade().send(MessageBuilder.withPayload(threadInfo).build())
        }.doOnError { onMatchFailed(roomInfo, buy, sell, it.message ?: "失败", isTopThree) }
    }

    fun <T: MatchStrategy.RoomInfo> onMatchFailed(roomInfo: T, buy: OrderParam?, sell: OrderParam?, failedInfo: String, isTopThree: Boolean = false) = r2dbc.withTransaction {
        roomService.findCompanyIdByRoomId(roomInfo.roomId, roomInfo.mode).flatMap { info ->
            val threadInfo = TradeInfo(buy, sell, roomInfo.roomId, info.companyId, info.stockId, roomInfo.mode)
            if (buy?.price != null && sell?.price != null)
                threadInfo.tradePrice = buy.price?.add(sell.price)?.divide(BigDecimal(2))
            threadInfo.tradeState = TradeState.FAILED
            threadInfo.stateDetails = failedInfo
            buy?.onTrade(threadInfo)
            sell?.onTrade(threadInfo)
            tradeInfoService.save(threadInfo)
                    .flatMap { buy?.let { b -> redisUtil.updateUserOrder(b) } }
                    .flatMap { sell?.let { s -> redisUtil.updateUserOrder(s) } }
                    .flatMap { redisUtil.setTradeInfo(threadInfo, roomInfo.endTime) }
                    .filter { isTopThree }
                    .flatMap { redisUtil.setRoomLastOrder(threadInfo.toOrderInfo()) }
                    .map { sink.outResult().send(MessageBuilder.withPayload(threadInfo.toOrderInfo().toFirstOrder(roomInfo.roomId, roomInfo.mode)).build()) }
                    .thenReturn(threadInfo)
        }.doOnSuccess { threadInfo -> sink.outTrade().send(MessageBuilder.withPayload(threadInfo).build()) }
                .doOnError { onMatchError(roomInfo, buy, sell, it.message ?: "失败", isTopThree) }
    }

    fun <T: MatchStrategy.RoomInfo> onMatchError(roomInfo: T, buy: OrderParam?, sell: OrderParam?, failedInfo: String, isTopThree: Boolean = false): Mono<TradeInfo> {
        return roomService.findCompanyIdByRoomId(roomInfo.roomId, roomInfo.mode).flatMap { info ->
            val threadInfo = TradeInfo(buy, sell, roomInfo.roomId, info.companyId, info.stockId, roomInfo.mode)
            val buyResult = buy?.let {
                it.tradeState = TradeState.FAILED
                it.stateDetails = failedInfo
                redisUtil.updateUserOrder(it)
            } ?: Mono.just(false)
            val sellResult = sell?.let {
                it.tradeState = TradeState.FAILED
                it.stateDetails = failedInfo
                redisUtil.updateUserOrder(it)
            } ?: Mono.just(false)
            threadInfo.tradeState = TradeState.FAILED
            threadInfo.stateDetails = failedInfo
            logger.error("onMatchError $failedInfo")
            buyResult.flatMap { sellResult }
                    .flatMap { redisUtil.setTradeInfo(threadInfo, roomInfo.endTime) }
                    .map { sink.outTrade().send(MessageBuilder.withPayload(threadInfo).build()) }
                    .filter { isTopThree }
                    .flatMap { redisUtil.setRoomLastOrder(threadInfo.toOrderInfo()) }
                    .map { sink.outResult().send(MessageBuilder.withPayload(threadInfo.toOrderInfo().toFirstOrder(roomInfo.roomId, roomInfo.mode)).build()) }
                    .thenReturn(threadInfo)
        }
    }

}