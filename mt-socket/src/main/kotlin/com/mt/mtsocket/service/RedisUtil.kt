package com.mt.mtsocket.service

import com.mt.mtsocket.entity.OrderParam
import com.mt.mtsocket.entity.RoomRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.*
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration

/**
 * Created by gyh on 2020/3/24.
 */
@Component
class RedisUtil {
    @Autowired
    lateinit var redisTemplate: ReactiveRedisOperations<String, Any>
    private val roomKey = "ROOM_RECORD:"
    private val peopleList = "peopleList"

    // -------------------------=======>>>房间<<<=======-------------------------

    /**
     * 更新房间人员列表
     */
    fun updateRoomPeople(roomId: String, list: Set<OrderParam>): Mono<Boolean> {
        return redisTemplate.opsForHash<String, Set<OrderParam>>().put(roomKey + roomId, peopleList, list)
    }

    /**
     * 获取房间的人员列表
     */
    fun getRoomPeople(roomId: String): Mono<MutableSet<OrderParam>> {
        return redisTemplate.opsForHash<String, MutableSet<OrderParam>>()
                .get(roomKey + roomId, peopleList)
                .defaultIfEmpty(mutableSetOf())
    }
}
