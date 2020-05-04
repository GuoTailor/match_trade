package com.mt.mtengine.service

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

    // -----------------------=====>>用户订单<<=====----------------------------

    /**
     * 添加元素到队列尾部，如果元素已存在就删除后再添加
     */
    fun putUserOrder(orderParam: OrderParam): Mono<Boolean> {
        return getUserOrder(orderParam.roomId!!)
                .filter { it.userId == orderParam.userId }
                .flatMap { redisTemplate.opsForList().remove(userOrder + orderParam.roomId, 0, it) }
                .then(redisTemplate.opsForList().rightPush(userOrder + orderParam.roomId, orderParam))
                .then(redisTemplate.expire(userOrder + orderParam.roomId,   // 今天23:59:59自动过期
                        Duration.ofSeconds(LocalTime.MAX.toSecondOfDay() - LocalTime.now().toSecondOfDay() + 1L)))
    }

    /**
     * 获取全部元素
     */
    fun getUserOrder(roomId: String): Flux<OrderParam> {
        return redisTemplate.opsForList().range(userOrder + roomId, 0, -1).cast(OrderParam::class.java)
    }

    /**
     * 获取队列的大小
     */
    fun getUserOrderSize(roomId: String): Mono<Long> {
        return redisTemplate.opsForList().size(userOrder + roomId)
    }

    /**
     * 从队头移出并获取列表的第一个元素
     */
    fun popUserOrder(roomId: String): Mono<OrderParam> {
        return redisTemplate.opsForList().leftPop(userOrder + roomId).cast(OrderParam::class.java)
    }

    fun popUserOrder(roomId: String, timeout: Duration): Mono<OrderParam> {
        return redisTemplate.opsForList().leftPop(userOrder + roomId, timeout).cast(OrderParam::class.java)
    }

    /**
     * 删除指定房间号下的全部订单，一般用于房间结束后的善后操作 create
     */
    fun deleteAllUserOrder(roomId: String): Mono<Long> {
        return redisTemplate.delete(userOrder + roomId)
    }
}
