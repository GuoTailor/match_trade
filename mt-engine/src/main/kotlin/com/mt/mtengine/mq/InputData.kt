package com.mt.mtengine.mq

import com.mt.mtcommon.CancelOrder
import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.RivalInfo
import com.mt.mtengine.match.MatchManager
import org.slf4j.LoggerFactory
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

/**
 * Created by gyh on 2020/4/30.
 */
@Component
class InputData {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @StreamListener(MatchSink.IN_ORDER)
    fun inputOrder(@Payload orderParam: OrderParam) {
        logger.info("添加订单 {}", orderParam)
        MatchManager.add(orderParam)
    }

    @StreamListener(MatchSink.IN_RIVAL)
    fun inputRival(@Payload rivalInfo: RivalInfo) {
        MatchManager.add(rivalInfo)
    }

    @StreamListener(MatchSink.IN_CANCEL)
    fun inputCancel(@Payload cancelOrder: CancelOrder) {
        MatchManager.cancel(cancelOrder)
    }
}