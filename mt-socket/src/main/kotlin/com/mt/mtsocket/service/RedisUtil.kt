package com.mt.mtsocket.service

import com.mt.mtcommon.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.*
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalTime
import java.util.*

/**
 * Created by gyh on 2020/3/24.
 */
@Component
class RedisUtil {
    @Autowired
    lateinit var redisTemplate: ReactiveRedisTemplate<String, Any>

    private val roomKey = Consts.roomKey
    private val roomInfo = Consts.roomInfo
    private val userOrderKey = Consts.userOrder
    private val rivalInfoKey = Consts.rivalKey
    private val topThreeKey = Consts.topThree

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

    // -----------------------=====>>用户订单<<=====----------------------------

    /**
     * 添加元素到队列尾部
     */
    fun putUserOrder(order: OrderParam, endTime: Date): Mono<Boolean> {
        return redisTemplate.opsForList().rightPush("$userOrderKey${order.roomId}:${order.userId}", order)
                .then(redisTemplate.expire("$userOrderKey${order.roomId}:${order.userId}",  // 房间结束时自动过期
                        Duration.ofSeconds((endTime.time / 1000) - LocalTime.now().toSecondOfDay() + 1L)))
    }

    /**
     * 更新用户的订单状态
     */
    fun updateUserOrder(order: OrderParam): Mono<Boolean> {
        val timeout = redisTemplate.getExpire("$userOrderKey${order.roomId}:${order.userId}")
        return getUserOrder(order)
                .filter { it.strictEquals(order) }
                .take(1)
                .zipWith(timeout)
                .flatMap { tuple ->
                    redisTemplate.opsForList().remove("$userOrderKey${order.roomId}:${order.userId}", 0, tuple.t1)
                            .flatMap { redisTemplate.opsForList().rightPush("$userOrderKey${order.roomId}:${order.userId}", order) }
                            .flatMap { redisTemplate.expire("$userOrderKey${order.roomId}:${order.userId}", tuple.t2) }
                }.next()
    }

    /**
     * 获取全部元素
     */
    fun getUserOrder(order: OrderParam): Flux<OrderParam> {
        return getUserOrder(order.userId!!, order.roomId!!)
    }

    /**
     * 获取全部元素
     */
    fun getUserOrder(userId: Int, roomId: String): Flux<OrderParam> {
        return redisTemplate.opsForList().range("$userOrderKey${roomId}:${userId}", 0, -1).cast(OrderParam::class.java)
    }

    /**
     * 获取队列的大小
     */
    fun getUserOrderSize(roomId: String, userId: Int): Mono<Long> {
        return redisTemplate.opsForList().size("$userOrderKey${roomId}:${userId}")
    }

    /**
     * 删除指定房间号下的全部订单，一般用于房间结束后的善后操作 create
     */
    fun deleteAllUserOrder(roomId: String): Mono<Long> {
        return redisTemplate.delete("$userOrderKey${roomId}:*")
    }

    // ------------------------=======>>>对手<<<=====----------------------

    fun putUserRival(rival: RivalInfo, endTime: Date): Mono<Boolean> {
        return redisTemplate.opsForList().rightPush("$rivalInfoKey${rival.roomId}:${rival.userId}", rival.rivals ?: arrayOf<Int>())
                .then(redisTemplate.expire("$rivalInfoKey${rival.roomId}:${rival.userId}",  // 房间结束时自动过期
                        Duration.ofSeconds(((endTime.time - System.currentTimeMillis()) / 1000) + 1L)))
    }

    fun getUserRival(userId: Int, roomId: String): Flux<Int> {
        return redisTemplate.opsForList().range("$rivalInfoKey${roomId}:${userId}", 0, -1).cast(Int::class.java)
    }

    // ------------------------=======>>>前三档<<<=====----------------------

    fun setRoomTopThree(topThree: TopThree): Mono<Boolean> {
        return redisTemplate.opsForHash<String, TopThree>().put(roomKey + topThree.roomId, topThreeKey, topThree)
    }

    fun getRoomTopThree(roomId: String): Mono<TopThree> {
        return redisTemplate.opsForHash<String, TopThree>().get(roomKey + roomId, topThreeKey)
    }
}
