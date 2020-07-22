package com.mt.mtgateway.server

import com.mt.mtgateway.bean.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration

/**
 * Created by gyh on 2020/7/8
 */
@Component
class RedisService {
    private val tokenKey = "TOKEN:"

    @Autowired
    lateinit var redisTemplate: ReactiveRedisOperations<String, Any>

    /**
     * 设置用户的token
     */
    fun setUserToken(user: User, time: Long): Mono<Boolean> {
        return redisTemplate.opsForValue().set("$tokenKey${user.id}", user)
                .then(redisTemplate.expire("$tokenKey${user.id}", Duration.ofMillis(time)))
    }

    /**
     * 获取用户的token
     */
    fun getUserByToken(id: Int): Mono<User> {
        return redisTemplate.opsForValue().get("$tokenKey$id").cast(User::class.java)
    }

    /**
     * 删除用户的token
     */
    fun delUserToken(token: String): Mono<Boolean> {
        return redisTemplate.opsForValue().delete("$tokenKey$token")
    }
}