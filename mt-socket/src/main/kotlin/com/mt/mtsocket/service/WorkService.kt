package com.mt.mtsocket.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mt.mtcommon.*
import com.mt.mtsocket.distribute.ServiceResponseInfo
import com.mt.mtsocket.entity.BaseUser
import com.mt.mtsocket.entity.ResponseInfo
import com.mt.mtsocket.mq.MatchSink
import com.mt.mtsocket.socket.SocketSessionStore
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
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val json = jacksonObjectMapper()

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
                    .map {
                        val userRoomInfo = store.getRoom(it.id!!) ?: throw IllegalStateException("错误，用户没有加入房间")
                        val roomRecord = userRoomInfo.roomRecord
                        price.userId = it.id
                        price.roomId = roomRecord.roomId
                        price.flag = roomRecord.model
                        price.number = roomRecord.tradeAmount
                        roomRecord
                    }.filter { it.quoteTime.toMillisOfDay() + it.startTime!!.time < System.currentTimeMillis() }
                    .filter { it.endTime!!.time > System.currentTimeMillis() }
                    .switchIfEmpty(Mono.error(IllegalStateException("房间未开启")))
                    .map { matchSink.outOrder().send(MessageBuilder.withPayload(price).build()) }
        } else Mono.error(IllegalStateException("报价错误"))
    }

    /**
     * 添加对手
     */
    fun addRival(rival: RivalInfo): Mono<Boolean> {
        return BaseUser.getcurrentUser()
                .map {
                    val userRoomInfo = store.getRoom(it.id!!) ?: throw IllegalStateException("错误，用户没有加入房间")
                    val roomRecord = userRoomInfo.roomRecord
                    if (RoomEnum.getRoomEnum(roomRecord.model!!) == RoomEnum.CLICK) {
                        rival.userId = it.id!!
                        rival.roomId = roomRecord.roomId
                        rival.flag = roomRecord.model
                        matchSink.outRival().send(MessageBuilder.withPayload(rival).build())
                    } else {
                        throw IllegalStateException("错误，点选成交才能选择对手")
                    }
                }
    }

    /**
     * 撤单
     */
    fun cancelOrder(): Mono<Boolean> {
        return BaseUser.getcurrentUser()
                .map {
                    val userRoomInfo = store.getRoom(it.id!!) ?: throw IllegalStateException("错误，用户没有加入房间")
                    val roomRecord = userRoomInfo.roomRecord
                    val cancelOrder = CancelOrder(it.id, roomRecord.roomId, roomRecord.model)
                    matchSink.outCancel().send(MessageBuilder.withPayload(cancelOrder).build())
                }
    }

    fun onRoomEvent(event: RoomEvent) {
        if (event.enable) {
            // TODO 添加定时任务通知第二阶段开始
            // val roomRecord = redisUtil.getRoomRecord(event.roomId).block()!!
            // quartzManager.addJob(MatchStartJobInfo(roomRecord))
        } else {
            SocketSessionStore.userInfoMap.forEach { _, userRoomInfo ->
                val roomRecord = userRoomInfo.roomRecord
                if (event.roomId == roomRecord.roomId) {
                    val data = ServiceResponseInfo(ResponseInfo.ok("房间关闭"), -1)
                    val msg = json.writeValueAsString(data)
                    val sessionHandler = userRoomInfo.session
                    sessionHandler.send(msg).and(sessionHandler.connectionClosed()).subscribe()
                }
            }
        }
    }

}
