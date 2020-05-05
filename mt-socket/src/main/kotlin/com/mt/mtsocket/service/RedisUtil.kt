package com.mt.mtsocket.service

import com.mt.mtcommon.Consts
import com.mt.mtcommon.RoomRecord
import com.mt.mtcommon.OrderParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.*
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalTime

/**
 * Created by gyh on 2020/3/24.
 */
@Component
class RedisUtil {
    @Autowired
    lateinit var redisTemplate: ReactiveRedisTemplate<String, Any>

    private val roomKey = Consts.roomKey
    private val roomInfo = Consts.roomInfo
    private val userOrder = Consts.userOrder

    // -------------------------=======>>>房间<<<=======-------------------------

    /**
     * 获取一个房间记录
     */
    fun getRoomRecord(roomId: String): Mono<RoomRecord> {
        return redisTemplate.opsForHash<String, RoomRecord>().get(roomKey + roomId, roomInfo)
    }

    fun getAllRoom() {
        redisTemplate.keys("$roomKey*")
    }

}
