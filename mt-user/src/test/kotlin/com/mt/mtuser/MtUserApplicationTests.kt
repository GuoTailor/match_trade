package com.mt.mtuser

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.*
import java.time.Duration


@SpringBootTest
class MtUserApplicationTests {
    @Autowired
    private lateinit var redisTemplate: ReactiveRedisOperations<String, Any>

    @Test
    fun contextLoads() {
        runBlocking {
            redisTemplate.opsForHash<String, Any>().putAndAwait("nmka:2", "info", "asfdsdfsdf")
            redisTemplate.expireAndAwait("nmka:2", Duration.ofMinutes(5))
            delay(10_000)
            println("end")
            redisTemplate.opsForHash<String, Any>().putAndAwait("nmka:2", "info", "123456")
            redisTemplate
        }
    }

}
