package com.mt.mtsocket.mq

import com.mt.mtcommon.*
import com.mt.mtsocket.common.NotifyOrder
import com.mt.mtsocket.entity.ResponseInfo
import com.mt.mtsocket.service.PeekPushService
import com.mt.mtsocket.socket.SocketSessionStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

/**
 * Created by gyh on 2020/4/20.
 */
@Component
class MatchOutProcessor {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private lateinit var peekPushService: PeekPushService

    @StreamListener(MatchSink.IN_TRADE)
    fun inTrade(@Payload tradeInfo: TradeInfo) {
        val data = ResponseInfo.ok("交易通知", tradeInfo)
        tradeInfo.buyerId?.let { SocketSessionStore.userInfoMap[it]?.session
                ?.send(data, NotifyOrder.notifyTrade, true)?.doOnNext { logger.info(tradeInfo.toString()) }?.subscribe() }
        tradeInfo.sellerId?.let { SocketSessionStore.userInfoMap[it]?.session
                ?.send(data, NotifyOrder.notifyTrade, true)?.doOnNext { logger.info(tradeInfo.toString()) }?.subscribe() }
        peekPushService.addTradeInfoEvent(tradeInfo.roomId!!, tradeInfo.model!!)
        peekPushService.addOrderEvent(tradeInfo.roomId!!, tradeInfo.model!!)    // 同时通知报价发生变化
    }

    @StreamListener(MatchSink.IN_RESULT)
    fun inResult(@Payload notifyResult: NotifyResult) {
        if (notifyResult.obj == updateTopThree || notifyResult.obj == updateFirstOrder) {
            SocketSessionStore.userInfoMap.forEach { _, info ->
                if (info.roomId == notifyResult.roomId) {
                    if (notifyResult.obj == updateTopThree) {
                        info.session.send(ResponseInfo.ok("前三档报价更新通知", notifyResult.data), NotifyOrder.notifyTopThree, true)
                                .doOnNext { logger.info("前三档报价更新通知{}", notifyResult.data) }.subscribe()
                    } else {
                        info.session.send(ResponseInfo.ok("上一笔成交价通知", notifyResult.data), NotifyOrder.notifyFirstOrder, true)
                                .doOnNext { logger.info("上一笔成交价通知{}", notifyResult.data) }.subscribe()
                    }
                }
            }
        } else {
            notifyResult.userId?.let {
                val order = when (notifyResult.obj) {
                    addOrderNotify -> NotifyOrder.offerResult
                    cancelOrderNotify -> NotifyOrder.cancelResult
                    addRivalNotify -> NotifyOrder.rivalResult
                    else -> error("不支持的通知的对象 ${notifyResult.obj}")
                }
                SocketSessionStore.userInfoMap[it]?.session?.send(ResponseInfo.ok("操作结果通知", notifyResult), order, true)
                        ?.doOnNext { msg -> logger.info("操作结果通知 {}", notifyResult) }?.subscribe()
            }
            if (notifyResult.obj == addOrderNotify || notifyResult.obj == cancelOrderNotify) {
                peekPushService.addOrderEvent(notifyResult.roomId!!, notifyResult.model!!)
            }
        }
    }
}
