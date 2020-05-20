package com.mt.mtsocket.mq

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mt.mtcommon.*
import com.mt.mtsocket.common.NotifyReq
import com.mt.mtsocket.distribute.ServiceResponseInfo
import com.mt.mtsocket.entity.ResponseInfo
import com.mt.mtsocket.service.PeekPushService
import com.mt.mtsocket.service.PeekSocketService
import com.mt.mtsocket.service.RedisUtil
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
    private val json = jacksonObjectMapper()
    @Autowired
    private lateinit var peekPushService: PeekPushService

    @StreamListener(MatchSink.IN_TRADE)
    fun inTrade(@Payload tradeInfo: TradeInfo) {
        val data = ServiceResponseInfo.DataResponse(ResponseInfo(0, "交易通知", tradeInfo), NotifyReq.notifyTrade)
        val msg = json.writeValueAsString(data)
        logger.info(msg)
        tradeInfo.buyerId?.let { SocketSessionStore.userInfoMap[it]?.session?.send(msg)?.subscribe() }
        tradeInfo.sellerId?.let { SocketSessionStore.userInfoMap[it]?.session?.send(msg)?.subscribe() }
        peekPushService.addTradeInfoEvent(tradeInfo.roomId!!, tradeInfo.model!!)
    }

    @StreamListener(MatchSink.IN_RESULT)
    fun inResult(@Payload notifyResult: NotifyResult) {
        if (notifyResult.obj == updateTopThree) {
            SocketSessionStore.userInfoMap.forEach { _, info ->
                if (info.roomId == notifyResult.roomId) {
                    info.session.send(ResponseInfo.ok("前三档报价更新通知", notifyResult.data), NotifyReq.notifyTopThree)
                            .doOnNext { logger.info(it) }.subscribe()
                }
            }
        } else {
            notifyResult.userId?.let {
                SocketSessionStore.userInfoMap[it]?.session?.send(ResponseInfo.ok("操作结果通知", notifyResult), NotifyReq.notifyResult)
                        ?.doOnNext { msg -> logger.info(msg) }?.subscribe()
            }
            if (notifyResult.obj == addOrderNotify || notifyResult.obj == cancelOrderNotify) {
                peekPushService.addOrderEvent(notifyResult.roomId!!, notifyResult.model!!)
            }
        }
    }
}
