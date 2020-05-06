package com.mt.mtsocket.mq

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mt.mtcommon.TradeInfo
import com.mt.mtsocket.distribute.ServiceResponseInfo
import com.mt.mtsocket.entity.ResponseInfo
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
    private lateinit var redisUtil: RedisUtil

    @StreamListener(MatchSink.IN_TRADE)
    fun inTrade(@Payload tradeInfo: TradeInfo) {
        val data = ServiceResponseInfo.DataResponse(ResponseInfo(0, "交易通知", tradeInfo), -1)
        val msg = json.writeValueAsString(data)
        logger.info(msg)
        tradeInfo.buyerId?.let { SocketSessionStore.userSession[it]?.send(msg)?.subscribe() }
        tradeInfo.sellerId?.let { SocketSessionStore.userSession[it]?.send(msg)?.subscribe() }
    }
}
