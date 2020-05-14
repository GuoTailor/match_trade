package com.mt.mtengine.mq

import com.mt.mtcommon.*
import com.mt.mtengine.match.MatchManager
import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Component

/**
 * Created by gyh on 2020/4/30.
 */
@Component
class InputData {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @SendTo(MatchSink.OUT_RESULT)
    @StreamListener(MatchSink.IN_ORDER)
    fun inputOrder(@Payload orderParam: OrderParam): NotifyResult {
        logger.info("添加订单 {}", orderParam)
        val result = MatchManager.add(orderParam)
        return orderParam.toNotifyResult(result)
    }

    @SendTo(MatchSink.OUT_RESULT)
    @StreamListener(MatchSink.IN_RIVAL)
    fun inputRival(@Payload rivalInfo: RivalInfo): NotifyResult {
        logger.info("添加对手 {}", rivalInfo)
        val result = MatchManager.add(rivalInfo)
        return rivalInfo.toNotifyResult(result)
    }

    @SendTo(MatchSink.OUT_RESULT)
    @StreamListener(MatchSink.IN_CANCEL)
    fun inputCancel(@Payload cancelOrder: CancelOrder): NotifyResult {
        logger.info("撤销订单 {}", cancelOrder)
        val result = MatchManager.cancel(cancelOrder)
        return cancelOrder.toNotifyResult(result)
    }
}