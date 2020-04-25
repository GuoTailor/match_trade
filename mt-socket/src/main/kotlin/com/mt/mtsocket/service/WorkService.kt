package com.mt.mtsocket.service

import com.mt.mtsocket.config.socket.SocketSessionStore
import com.mt.mtsocket.entity.BaseUser
import com.mt.mtsocket.entity.OrderParam
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/4/14.
 */
@Service
class WorkService {
    @Autowired
    private lateinit var redisUtil: RedisUtil

    /**
     * 进入房间
     */
    fun enterRoom(roomId: String): Mono<Boolean> {
        return redisUtil.getRoomPeople(roomId)
                .switchIfEmpty(Mono.error(IllegalStateException("房间未开启")))
                .flatMap { BaseUser.getcurrentUser() }  // TODO 用户多处登陆解决
                .map {
                    // 同步互斥调用,我不知道是否有更好的api提供用于解决互斥问题
                    synchronized(this.javaClass) {
                        val list = redisUtil.getRoomPeople(roomId).block()
                        list!!.add(OrderParam(it.id!!))
                        redisUtil.updateRoomPeople(roomId, list).block()
                    }
                }
    }

    /**
     * 报价
     */
    fun offer(price: OrderParam): Mono<String> {
        if (price.verify()) {
            return BaseUser.getcurrentUser()
                    .flatMap { user ->
                        val roomId = SocketSessionStore.getRoom(user.id!!)
                                ?: return@flatMap Mono.error<String>(IllegalStateException("错误，用户没有加入房间"))
                        price.id = user.id
                        synchronized(this.javaClass) {
                            val set = redisUtil.getRoomPeople(roomId).block()!!
                            set.removeIf { it.id == user.id }
                            set.add(price)
                            redisUtil.updateRoomPeople(roomId, set).block()
                        }
                        Mono.just("成功")
                    }
        } else return Mono.error(IllegalStateException("报价错误"))
    }

    fun match() {

    }
}
