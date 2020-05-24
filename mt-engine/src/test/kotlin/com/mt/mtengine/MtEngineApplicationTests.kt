package com.mt.mtengine

import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.TradeInfo
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
        val order = OrderParam(roomId = "27", userName = "nmkla",userId = 12, price = BigDecimal(12), number = 120)
        val tradeInfo = TradeInfo(order, order, "", 12, 12, "C")
        redisUtil.setTradeInfo(tradeInfo, Date(1589999000000)).block()
        redisUtil.setTradeInfo(tradeInfo, Date(1589999000000)).block()
        redisUtil.getTradeInfo("27").doOnNext { println(it) }.blockLast()
    }

    @Test
    fun testOrder() {
        val date = Date()
        redisUtil.putUserOrder(OrderParam(userId = 1, roomId = "nm", price = BigDecimal(0), time = date), Util.encoderDate("2020-5-15 00:00:01"))
                .flatMap { redisUtil.updateUserOrder(OrderParam(userId = 1, roomId = "nm", price = BigDecimal(0), time = date, tradePrice = BigDecimal(223))) }
                .log().block()
    }

}
