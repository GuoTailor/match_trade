package com.mt.mtengine.service

import com.mt.mtcommon.*
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.*
import org.springframework.data.redis.listener.ChannelTopic
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
    private val closeTopic = ChannelTopic(Consts.roomEvent)
    private val roomKey = Consts.roomKey
    private val roomInfo = Consts.roomInfo
    private val userOrderKey = Consts.userOrder

    // -------------------------=======>>>房间<<<=======-------------------------

    /**
     * 保存一个房间记录,并通过房间记录的时长设置过期时间
     */
    fun saveRoomRecord(roomRecord: RoomRecord): Mono<Boolean> {
        return redisTemplate.opsForHash<String, RoomRecord>().put(roomKey + roomRecord.roomId, roomInfo, roomRecord)
                .then(redisTemplate.expire(roomKey + roomRecord.roomId, roomRecord.duration!!.toDuration()))

    }

    /**
     * 获取一个房间记录
     */
    fun getRoomRecord(roomId: String): Mono<RoomRecord> {
        return redisTemplate.opsForHash<String, RoomRecord>().get(roomKey + roomId, roomInfo)
    }

    /**
     * 获取并删除一个记录<br>
     * 注意该方法不安全
     */
    fun deleteAndGetRoomRecord(roomId: String): Mono<RoomRecord> {
        // 不安全 也许有更好的办法
        return redisTemplate.opsForHash<String, RoomRecord>().get(roomKey + roomId, roomInfo)
                .zipWith(redisTemplate.opsForHash<String, RoomRecord>().delete(roomKey + roomId)) { t1, _ -> t1 }
    }

    fun getAllRoom() {
        redisTemplate.keys("$roomKey*")
    }

    // -----------------------=====>>用户订单<<=====----------------------------

    fun getOriginalOrder(order: OrderParam): OrderParam {
        return OrderParam(order.userId,
                order.price,
                order.roomId,
                order.isBuy,
                order.number,
                order.flag,
                order.time)
    }

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
        val temp = getOriginalOrder(order)
        return redisTemplate.opsForList().remove("$userOrderKey${order.roomId}:${order.userId}", 0, temp)
                .flatMap { redisTemplate.opsForList().rightPush("$userOrderKey${order.roomId}:${order.userId}", order) }
                .flatMap { redisTemplate.getExpire("$userOrderKey${order.roomId}:${order.userId}") }
                .flatMap { redisTemplate.expire("$userOrderKey${order.roomId}:${order.userId}", it) }
    }

    /**
     * 删除匹配的订单
     */
    fun deleteUserOrder(userId: Int, roomId: String): Mono<Void> {
        return getUserOrder(userId, roomId)
                .filter { userId == it.userId && roomId == it.roomId }
                .flatMap { redisTemplate.opsForList().remove("$userOrderKey${roomId}:${userId}", 0, it) }
                .then()
    }

    /**
     * 删除匹配的订单
     */
    fun deleteUserOrder(order: CancelOrder): Mono<Void> {
        return deleteUserOrder(order.userId!!, order.roomId!!)
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


    // ------------------------=======>>>推送<<<=====----------------------

    /**
     * 推送房间开启/关闭事件
     * 注意房间开启通知需要在房间记录写入到redis之后再发送
     */
    fun publishRoomEvent(event: RoomEvent): Mono<Long> {
        return redisTemplate.convertAndSend(closeTopic.topic, event)
    }
}
