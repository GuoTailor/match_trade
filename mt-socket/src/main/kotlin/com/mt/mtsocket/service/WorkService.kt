package com.mt.mtsocket.service

import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.RoomEnum
import com.mt.mtcommon.RoomRecord
import com.mt.mtcommon.toMillisOfDay
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
    // TODO 去掉存redis，直接mq发出去
    fun offer(price: OrderParam): Mono<Boolean> {
        return if (price.verify()) {
            val verify = BaseUser.getcurrentUser()
                    .flatMap { user ->
                        val roomId = store.getRoom(user.id!!)
                                ?: return@flatMap Mono.error<RoomRecord>(IllegalStateException("错误，用户没有加入房间"))
                        if (RoomEnum.getRoomEnum(roomId) == RoomEnum.CLICK) {
                            redisUtil.getRoomRecord(roomId)
                        } else Mono.empty()
                    }.flatMap {
                        if (it.quoteTime.toMillisOfDay() + it.startTime!!.time < System.currentTimeMillis()) {
                            Mono.error(IllegalStateException("错误，房间还没开始报价"))
                        } else Mono.empty<Unit>()
                    }
            // TODO 只允许报一次价
            verify.then(BaseUser.getcurrentUser()
                    .flatMap { user ->
                        val roomId = store.getRoom(user.id!!)
                                ?: return@flatMap Mono.error<Boolean>(IllegalStateException("错误，用户没有加入房间"))
                        price.userId = user.id
                        price.roomId = roomId
                        price.flag = RoomEnum.getRoomModel(roomId).flag
                        redisUtil.putUserOrder(price)
                    })
        } else Mono.error(IllegalStateException("报价错误"))
    }

    fun match(roomId: String) {
        val size = redisUtil.getUserOrderSize(roomId).block() ?: 0
        for (i in 0 until size) {
            redisUtil.popUserOrder(roomId)
                    .doOnError { log.error("错误", it) }
                    .subscribe {
                        val message = MessageBuilder.withPayload(it)
                                .setHeader(MessageConst.PROPERTY_TAGS, "testTag")
                                .build()
                        val result = matchSink.outOrder().send(message)
                        log.info("{} {}", it, result)
                    }
        }
    }
}
