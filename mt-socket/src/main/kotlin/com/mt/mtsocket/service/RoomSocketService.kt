package com.mt.mtsocket.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mt.mtcommon.*
import com.mt.mtsocket.common.NotifyReq
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
import reactor.core.scheduler.Schedulers

/**
 * Created by gyh on 2020/4/14.
 */
@Service
class RoomSocketService {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val json = jacksonObjectMapper()
    private val store = SocketSessionStore

    @Autowired
    private lateinit var redisUtil: RedisUtil

    @Autowired
    lateinit var matchSink: MatchSink

    /**
     * 进入房间
     */
    fun enterRoom(roomId: String): Mono<RoomRecord> {
        return redisUtil.getRoomRecord(roomId).switchIfEmpty(Mono.error(IllegalStateException("房间未开启")))
    }

    /**
     * 当有用户上下线是通知人数变化
     */
    fun onNumberChange(roomId: String): Mono<*> {
        var result: Mono<*>? = null
        store.userInfoMap.forEach { _, info ->
            if (info.roomId == roomId) {
                val size = store.getOnLineSize(roomId)
                val temp = info.session.send(ResponseInfo.ok("人数变化", size), NotifyReq.notifyNumberChange)
                        .doOnNext { logger.info(it) }
                result = if (result == null) temp else result!!.zipWith(temp)
            }
        }
        return result ?: Mono.empty<Any>()
    }

    /**
     * 报价
     */
    fun addOrder(price: OrderParam): Mono<Boolean> {
        return if (price.verify()) {
            BaseUser.getcurrentUser().flatMap {
                price.userId = it.id
                val userRoomInfo = store.getRoomInfo(it.id!!)
                        ?: return@flatMap Mono.error<RoomRecord>(IllegalStateException("错误，用户没有加入房间"))
                price.userName = userRoomInfo.userName
                logger.info(userRoomInfo.roomId)
                redisUtil.getRoomRecord(userRoomInfo.roomId)
            }.map {
                if (it.startTime!!.time - 2_000 < System.currentTimeMillis()) {
                    if (it.endTime!!.time - (it.secondStage?.toMillisOfDay() ?: 0) >= System.currentTimeMillis()) {
                        price.roomId = it.roomId
                        price.mode = it.mode
                        price.number = it.tradeAmount
                        price.tradeState = TradeState.STAY
                        matchSink.outOrder().send(MessageBuilder.withPayload(price).build())
                    } else error("报价已结束")
                } else error("房间未开启")
            }.switchIfEmpty(Mono.error(IllegalStateException("房间未开启")))
        } else Mono.error(IllegalStateException("报价错误"))
    }

    /**
     * 添加对手
     */
    fun addRival(rival: RivalInfo): Mono<Boolean> {
        return BaseUser.getcurrentUser().map {
            val userRoomInfo = store.getRoomInfo(it.id!!) ?: error("错误，用户没有加入房间")
            if (RoomEnum.getRoomEnum(userRoomInfo.mode) == RoomEnum.CLICK) {
                rival.userId = it.id!!
                rival.userName = it.username
                rival.roomId = userRoomInfo.roomId
                rival.flag = userRoomInfo.mode
            } else error("错误，点选成交才能选择对手")
            matchSink.outRival().send(MessageBuilder.withPayload(rival).build())
        }
    }

    /**
     * 获取自己选择的对手
     */
    fun getRival(): Mono<RivalInfo> {
        return BaseUser.getcurrentUser().flatMap {
            val userRoomInfo = store.getRoomInfo(it.id!!) ?: error("错误，用户没有加入房间")
            val rival = RivalInfo(userId = it.id!!, roomId = userRoomInfo.roomId)
            if (RoomEnum.getRoomEnum(userRoomInfo.mode) == RoomEnum.CLICK) {
                redisUtil.getUserRival(it.id!!, userRoomInfo.roomId).map { rivals ->
                    rival.rivals = rivals
                    rival
                }
            } else error("错误，点选成交才能选择对手")
        }
    }

    /**
     * 获取对手
     */
    fun getAllRival(isBuy: Boolean): Mono<List<OrderParam>> {
        return BaseUser.getcurrentUser().flatMap { user ->
            val userRoomInfo = store.getRoomInfo(user.id!!) ?: error("错误，用户没有加入房间")
            redisUtil.getUserOrder(userRoomInfo.roomId)
                    .filter { it.isBuy == isBuy }
                    .collectList()
        }
    }

    /**
     * 撤单
     */
    fun cancelOrder(): Mono<Boolean> {
        val cancelOrder = CancelOrder()
        return BaseUser.getcurrentUser().flatMap {
            cancelOrder.userId = it.id!!
            val userRoomInfo = store.getRoomInfo(it.id!!)
                    ?: return@flatMap Mono.error<RoomRecord>(IllegalStateException("错误，用户没有加入房间"))
            redisUtil.getRoomRecord(userRoomInfo.roomId)
        }.map {
            if (it.mode == RoomEnum.CLICK.mode || it.mode == RoomEnum.TIMING.mode || it.mode == RoomEnum.BICKER.mode) {
                if (it.endTime!!.time - (it.secondStage?.toMillisOfDay() ?: 0) - 60_000 >= System.currentTimeMillis()) {
                    error("定时撮合类房间结束前一分钟不能撤单")
                }
            }
            cancelOrder.roomId = it.roomId
            cancelOrder.mode = it.mode
            matchSink.outCancel().send(MessageBuilder.withPayload(cancelOrder).build())
        }
    }

    /**
     * 获取报价历史
     */
    fun getOrderRecord(): Mono<List<OrderParam>> {
        return BaseUser.getcurrentUser().flatMap {
            val userRoomInfo = store.getRoomInfo(it.id!!)
                    ?: return@flatMap Mono.error<List<OrderParam>>(IllegalStateException("错误，用户没有加入房间"))
            val roomId = userRoomInfo.roomId
            redisUtil.getUserOrder(it.id!!, roomId).collectList()
        }
    }

    /**
     * 获取自己当前房间的在线人数
     */
    fun getOnLineSize(): Mono<Int> {
        return BaseUser.getcurrentUser()
                .map { store.getRoomInfo(it.id!!) ?: error(IllegalStateException("错误，用户没有加入房间")) }
                .map { store.getOnLineSize(it.roomId) }
    }

    /**
     * 获取自己房间的报价前三档
     */
    fun getTopThree(): Mono<TopThree> {
        return BaseUser.getcurrentUser()
                .flatMap {
                    val roomInfo = store.getRoomInfo(it.id!!)
                            ?: return@flatMap Mono.error<TopThree>(IllegalStateException("错误，用户没有加入房间"))
                    redisUtil.getRoomTopThree(roomInfo.roomId).switchIfEmpty(Mono.just(TopThree(roomInfo.roomId, roomInfo.mode)))
                }
    }

    fun onRoomEvent(event: RoomEvent) {
        if (event.enable) {
            // TODO 添加定时任务通知第二阶段开始
            logger.info("收到房间开启通知 {}", event.roomId)
            // val roomRecord = redisUtil.getRoomRecord(event.roomId).block()!!
            // quartzManager.addJob(MatchStartJobInfo(roomRecord))
        } else {
            logger.info("收到房间关闭通知 {}", event.roomId)
            SocketSessionStore.userInfoMap.forEach { _, userRoomInfo ->
                if (event.roomId == userRoomInfo.roomId) {
                    userRoomInfo.session.send(ResponseInfo.ok("房间将于一分钟后关闭。", event), NotifyReq.notifyRoomClose)
                            .doOnNext { logger.info(it) }
                            .subscribeOn(Schedulers.elastic()).subscribe()
                }
            }
            SocketSessionStore.peekInfoMap.forEach { _, userRoomInfo ->
                if (event.roomId == userRoomInfo.roomId) {
                    userRoomInfo.session.send(ResponseInfo.ok("房间将于一分钟后关闭。", event), NotifyReq.notifyRoomClose)
                            .doOnNext { logger.info(it) }
                            .subscribeOn(Schedulers.elastic()).subscribe()
                }
            }
        }
    }

}
