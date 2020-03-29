package com.mt.mtuser.service

import com.mt.mtuser.entity.RoomRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.*
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Created by gyh on 2020/3/24.
 */
@Component
class RedisUtil {
    @Autowired
    lateinit var redisTemplate: ReactiveRedisOperations<String, Any>
    private val roomKey = "ROOM:"

    suspend fun saveRoomRecord(roomRecord: RoomRecord) {
        redisTemplate
                .opsForValue()
                .setAndAwait(roomKey + roomRecord.roomNumber, roomRecord, roomRecord.duration!!)
    }

    suspend fun getRoomRecord(roomNumber: String) {
        redisTemplate.opsForValue().getAndAwait(roomKey + roomNumber)
    }

    suspend fun deleteRoomRecord(roomNumber: String) {
        redisTemplate.deleteAndAwait(roomKey + roomNumber)
    }

    /**
     * 获取并删除一个记录<br>
     * 注意该方法不安全
     */
    suspend fun deleteAndGetRoomRecord(roomNumber: String): RoomRecord {
        // 不安全 也许有更好的办法
        val roomRecord = redisTemplate.opsForValue().getAndAwait(roomKey + roomNumber)
        redisTemplate.deleteAndAwait(roomKey + roomNumber)
        return roomRecord as RoomRecord
    }

    /**
     * 更新房间的过期时间
     */
    suspend fun updateRoomExpire(roomNumber: String, duration: Duration) {
        redisTemplate.expireAndAwait(roomKey + roomNumber, duration)
    }
}
