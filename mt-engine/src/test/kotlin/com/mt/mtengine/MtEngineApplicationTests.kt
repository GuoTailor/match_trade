package com.mt.mtengine

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mt.mtcommon.RoomRecord
import com.mt.mtengine.entity.User
import com.mt.mtengine.mq.MatchSink
import com.mt.mtengine.service.MatchService
import com.mt.mtengine.service.RedisUtil
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.ReactiveRedisTemplate
import reactor.core.scheduler.Schedulers
import java.time.LocalDateTime

@SpringBootTest
class MtEngineApplicationTests {
    @Autowired
    lateinit var redisTemplate: ReactiveRedisTemplate<String, Any>

    @Autowired
    lateinit var matchSink: MatchSink

    @Autowired
    lateinit var redisUtil: RedisUtil

    @Autowired
    lateinit var matchService: MatchService

    @Test
    fun contextLoads() {
        val user = User()
        user.phone = "nmka"
        user.password = "nmka"
        user.createTime = LocalDateTime.now()
        var subscribeOn = matchService.saveUser(user).subscribeOn(Schedulers.boundedElastic()).subscribe()
        Thread.sleep(10_000)
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
