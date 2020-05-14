package com.mt.mtengine

import com.mt.mtcommon.OrderParam
import com.mt.mtengine.common.Util
import com.mt.mtengine.mq.MatchSink
import com.mt.mtengine.service.RedisUtil
import org.apache.rocketmq.common.message.MessageConst
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.support.MessageBuilder
import java.math.BigDecimal
import java.util.*

@SpringBootTest
class MtEngineApplicationTests {
    @Autowired
    lateinit var matchSink: MatchSink

    @Autowired
    lateinit var redisUtil: RedisUtil

    @Test
    fun contextLoads() {
        val msg = mapOf("12" to 12, "nmka" to "abc")
        val message = MessageBuilder.withPayload(msg)
                .setHeader(MessageConst.PROPERTY_TAGS, "testTag")
                .build()
        val rest = matchSink.outTrade().send(message)
        println(rest)
    }

    @Test
    fun testOrder() {
        val date = Date()
        redisUtil.putUserOrder(OrderParam(userId = 1, roomId = "nm", price = BigDecimal(0), time = date), Util.encoderDate("2020-5-15 00:00:01"))
                .flatMap { redisUtil.updateUserOrder(OrderParam(userId = 1, roomId = "nm", price = BigDecimal(0), time = date, tradePrice = BigDecimal(223))) }
                .log().block()
    }

}
