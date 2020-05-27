package com.mt.mtuser.service

import com.mt.mtcommon.RedisConsts
import com.mt.mtcommon.RoomEvent
import com.mt.mtcommon.RoomRecord
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.*
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Created by gyh on 2020/3/24.
 */
@Component
class RedisUtil {
    @Autowired
    lateinit var redisTemplate: ReactiveRedisOperations<String, Any>
    private val closeTopic = ChannelTopic(RedisConsts.roomEvent)
    private val roomKey = RedisConsts.roomKey
    private val roomInfo = RedisConsts.roomInfo
    private val codeKey = RedisConsts.codeKey
    private val codeTime = Duration.ofMinutes(5)
    private val verifyTime = Duration.ofMinutes(60)
    private val verifyKey = "VERIFY:"

    // -------------------------=======>>>房间<<<=======-------------------------

    /**
     * 保存一个房间记录,并通过房间记录的时长设置过期时间
     */
    suspend fun saveRoomRecord(roomRecord: RoomRecord) {
        redisTemplate.opsForHash<String, RoomRecord>().putAndAwait(roomKey + roomRecord.roomId, roomInfo, roomRecord)
        redisTemplate.expireAndAwait(roomKey + roomRecord.roomId, roomRecord.expire!!)
    }

    /**
     * 更新房间记录
     */
    suspend fun updateRoomRecord(roomRecord: RoomRecord) {
        redisTemplate.opsForHash<String, RoomRecord>().putAndAwait(roomKey + roomRecord.roomId, roomInfo, roomRecord)
    }

    /**
     * 获取一个房间记录
     */
    suspend fun getRoomRecord(roomId: String): RoomRecord? {
        return redisTemplate
                .opsForHash<String, RoomRecord>()
                .getAndAwait(roomKey + roomId, roomInfo)
    }

    /**
     * 删除一个房间记录
     */
    suspend fun deleteRoomRecord(roomId: String) {
        redisTemplate
                .opsForHash<String, RoomRecord>()
                .deleteAndAwait(roomKey + roomId)
    }

    /**
     * 获取并删除一个记录<br>
     * 注意该方法不安全
     */
    suspend fun deleteAndGetRoomRecord(roomId: String): RoomRecord? {
        // 不安全 也许有更好的办法
        val roomRecord = redisTemplate.opsForHash<String, RoomRecord>().getAndAwait(roomKey + roomId, roomInfo)
        redisTemplate.opsForHash<String, RoomRecord>().deleteAndAwait(roomKey + roomId)
        return roomRecord
    }

    /**
     * 更新房间的过期时间
     */
    suspend fun updateRoomExpire(roomId: String, duration: Duration) {
        redisTemplate.expireAndAwait(roomKey + roomId, duration)
    }


    // -------------------------=======>>>验证码<<<=======-------------------------

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
        return redisTemplate.opsForValue().getAndAwait(codeKey + phone) as? String?
    }

    suspend fun deleteCode(phone: String) {
        redisTemplate.opsForValue().deleteAndAwait(codeKey + phone)
    }

    suspend fun saveVerifyResult(phone: String) {
        redisTemplate.opsForValue().setAndAwait(verifyKey + phone, phone, verifyTime)
    }

    suspend fun getVerify(phone: String): Any? {
        return redisTemplate.opsForValue().getAndAwait(verifyKey + phone)
    }

    suspend fun deleteVerify(phone: String): Boolean {
        return redisTemplate.opsForValue().deleteAndAwait(verifyKey + phone)
    }

    // ------------------------=======>>>推送<<<=====----------------------

    /**
     * 推送房间开启/关闭事件
     * 注意房间开启通知需要在房间记录写入到redis之后再发送
     */
    // todo 改为MQ
    suspend fun publishRoomEvent(event: RoomEvent): Long {
        return redisTemplate.convertAndSend(closeTopic.topic, event).awaitSingle()
    }
}
