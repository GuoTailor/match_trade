package com.mt.mtuser.common

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.ReactiveRedisOperations
import org.springframework.stereotype.Component

/**
 * Created by gyh on 2020/3/24.
 */
@Component
class RedisUtil {
    @Autowired
    lateinit var redisTemplate: ReactiveRedisOperations<String, Any>


}
