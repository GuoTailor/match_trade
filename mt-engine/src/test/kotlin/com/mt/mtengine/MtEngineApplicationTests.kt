package com.mt.mtengine

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.RoomRecord
import com.mt.mtcommon.TradeInfo
import com.mt.mtengine.common.Util
import com.mt.mtengine.mq.MatchSink
import com.mt.mtengine.service.RedisUtil
import org.apache.rocketmq.common.message.MessageConst
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.messaging.support.MessageBuilder
import java.math.BigDecimal
import java.util.*

//@SpringBootTest
class MtEngineApplicationTests {
    @Autowired
    lateinit var redisTemplate: ReactiveRedisTemplate<String, Any>

    @Autowired
    lateinit var matchSink: MatchSink

    @Autowired
    lateinit var redisUtil: RedisUtil

    @Test
    fun contextLoads() {
    }

    @Test
    fun testOrder() {
    }

    @Test
    fun testTack() {
        val s = redisTemplate.opsForList().range("nmka_12:1", -1, -1)
                .cast(Integer::class.java)
                .next().block()
        println(s)
    }

    @Test
    fun testJson() {
        val om = jacksonObjectMapper()
        println(om.writeValueAsString(RoomRecord()))
    }

}
