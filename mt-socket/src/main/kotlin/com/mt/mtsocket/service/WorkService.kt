package com.mt.mtsocket.service

import com.mt.mtcommon.*
import com.mt.mtsocket.entity.BaseUser
import com.mt.mtsocket.mq.MatchSink
import com.mt.mtsocket.socket.SocketSessionStore
import org.apache.rocketmq.common.message.MessageConst
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/4/14.
 */
@Service
class WorkService {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private lateinit var redisUtil: RedisUtil
    private val store = SocketSessionStore

    @Autowired
    lateinit var matchSink: MatchSink

    /**
     * 进入房间
     */
    fun enterRoom(roomId: String): Mono<RoomRecord> {
        return redisUtil.getRoomRecord(roomId).switchIfEmpty(Mono.error(IllegalStateException("房间未开启")))
    }

    /**
     * 报价
     */
    fun addOrder(price: OrderParam): Mono<Boolean> {
        return if (price.verify()) {
            BaseUser.getcurrentUser()
                    .flatMap {
                        val roomId = store.getRoom(it.id!!)
                                ?: return@flatMap Mono.error<RoomRecord>(IllegalStateException("错误，用户没有加入房间"))
                        redisUtil.getRoomRecord(roomId)
                    }.filter { it.quoteTime.toMillisOfDay() + it.startTime!!.time < System.currentTimeMillis() }
                    .filter { it.endTime!!.time > System.currentTimeMillis() }
                    .switchIfEmpty(Mono.error(IllegalStateException("房间未开启")))
                    .flatMap { BaseUser.getcurrentUser() }
                    .map { user ->
                        val roomId = store.getRoom(user.id!!) ?: throw IllegalStateException("错误，用户没有加入房间")
                        price.userId = user.id
                        price.roomId = roomId
                        price.flag = RoomEnum.getRoomModel(roomId).flag
                        matchSink.outOrder().send(MessageBuilder.withPayload(price).build())
                    }
        } else Mono.error(IllegalStateException("报价错误"))
    }

    /**
     * 添加对手
     */
    fun addRival(rival: RivalInfo): Mono<Boolean> {
        return BaseUser.getcurrentUser()
                .map {
                    val roomId = store.getRoom(it.id!!)?: throw IllegalStateException("错误，用户没有加入房间")
                    if (RoomEnum.getRoomModel(roomId) == RoomEnum.CLICK) {
                        rival.userId = it.id!!
                        rival.roomId = roomId
                        matchSink.outRival().send(MessageBuilder.withPayload(rival).build())
                    } else {
                        throw IllegalStateException("错误，点选成交才能选择对手")
                    }
                }
    }

}
