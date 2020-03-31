package com.mt.mtuser.service

import com.mt.mtuser.common.toDuration
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
    private val roomKey = "ROOM_RECORD:"
    private val codeKey = "CODE:"
    private val codeTime = Duration.ofMinutes(5)

    /**
     * 保存一个房间记录
     */
    suspend fun saveRoomRecord(roomRecord: RoomRecord) {
        redisTemplate.opsForValue().setAndAwait(roomKey + roomRecord.roomId, roomRecord, roomRecord.duration!!.toDuration())
    }

    /**
     * 获取一个房间记录
     */
    suspend fun getRoomRecord(roomId: String): RoomRecord? {
        return redisTemplate.opsForValue().getAndAwait(roomKey + roomId) as RoomRecord?
    }

    /**
     * 删除一个房间记录
     */
    suspend fun deleteRoomRecord(roomId: String) {
        redisTemplate.opsForValue().deleteAndAwait(roomKey + roomId)
    }

    /**
     * 获取并删除一个记录<br>
     * 注意该方法不安全
     */
    suspend fun deleteAndGetRoomRecord(roomId: String): RoomRecord? {
        // 不安全 也许有更好的办法
        val roomRecord = redisTemplate.opsForValue().getAndAwait(roomKey + roomId)
        redisTemplate.deleteAndAwait(roomKey + roomId)
        return roomRecord as RoomRecord?
    }

    /**
     * 更新房间的过期时间
     */
    suspend fun updateRoomExpire(roomId: String, duration: Duration) {
        redisTemplate.expireAndAwait(roomKey + roomId, duration)
    }

    /**
     * 保存一个验证码
     */
    suspend fun saveCode(phone: String, code: String) {
        redisTemplate.opsForValue().setAndAwait(codeKey + phone, code, codeTime)
    }

    /**
     * 获取一个验证码
     */
    suspend fun getCode(phone: String): String? {
        return redisTemplate.opsForValue().getAndAwait(codeKey + phone) as String?
    }

    suspend fun deleteCode(phone: String) {
        redisTemplate.opsForValue().deleteAndAwait(codeKey + phone)
    }
}
