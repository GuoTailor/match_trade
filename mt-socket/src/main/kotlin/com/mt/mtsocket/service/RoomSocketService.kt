package com.mt.mtsocket.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mt.mtcommon.*
import com.mt.mtsocket.common.NotifyOrder
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
import java.math.BigDecimal
import java.time.LocalDateTime

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
     * note: 不能使用mono返回，必须手动订阅，因为当前用户断开后通道就会产生取消事件，
     * 于是通知其他用户人数变化的操作就得不到执行
     */
    fun onNumberChange(roomId: String) {
        store.userInfoMap.entries.forEach {
            if (it.value.roomId == roomId) {
                val size = store.getOnLineSize(roomId)
                it.value.session.send(ResponseInfo.ok("人数变化", size), NotifyOrder.notifyNumberChange, true)
                    .doOnNext { logger.info(size.toString()) }
                    .subscribeOn(Schedulers.elastic())
                    .subscribe()
            }
        }
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
                if (it.startTime!!.minusSeconds(2) < LocalDateTime.now()) {
                    if (it.endTime!!.minus(it.secondStage.toDuration()) >= LocalDateTime.now()) {
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
                userRoomInfo.toRivalInfo(rival)
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
            val rival = userRoomInfo.toRivalInfo()
            if (RoomEnum.getRoomEnum(userRoomInfo.mode) == RoomEnum.CLICK) {
                redisUtil.getUserRival(it.id!!, userRoomInfo.roomId)
                    .switchIfEmpty(Mono.just(arrayListOf()))
                    .map { rivals ->
                        rival.rivals = rivals
                        rival
                    }
            } else error("错误，点选成交才能选择对手")
        }
    }

    /**
     * 获取对手
     */
    fun getAllRival(): Mono<List<OrderParam>> {
        return BaseUser.getcurrentUser().flatMap { user ->
            val userRoomInfo = store.getRoomInfo(user.id!!) ?: error("错误，用户没有加入房间")
            val lostOrder = redisUtil.getLastUserOrder(user.id!!, userRoomInfo.roomId)
            val orderList = fun(price: BigDecimal, isBuy: Boolean): Mono<MutableList<OrderParam>> {
                return redisUtil.getUserOrder(userRoomInfo.roomId)
                    .filter { it.isBuy != isBuy && if (isBuy) price > it.price else price < it.price }
                    .collectList()
            }
            lostOrder.flatMap { orderList(it.price ?: BigDecimal(0), it.isBuy!!) }
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
                if (it.endTime!!.minus(it.secondStage.toDuration()).minusSeconds(60) <= LocalDateTime.now()) {
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
                redisUtil.getRoomTopThree(roomInfo.roomId).defaultIfEmpty(TopThree(roomInfo.roomId, roomInfo.mode))
            }
    }

    /**
     * 获取房间的最后一次成交价
     */
    fun getLastOrderInfo(): Mono<OrderInfo> {
        return BaseUser.getcurrentUser()
            .flatMap {
                val roomInfo = store.getRoomInfo(it.id!!)
                    ?: return@flatMap Mono.error<OrderInfo>(IllegalStateException("错误，用户没有加入房间"))
                redisUtil.getRoomLastOrder(roomInfo.roomId)
                    .defaultIfEmpty(OrderInfo(it.id!!, roomInfo.roomId, BigDecimal.ZERO, 0, LocalDateTime.now()))
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
            val roomCloseNotify = fun(userRoomInfo: SocketSessionStore.UserRoomInfo) {
                if (event.roomId == userRoomInfo.roomId) {
                    userRoomInfo.session.send(ResponseInfo.ok("房间将于一分钟后关闭。", event), NotifyOrder.notifyRoomClose, true)
                        .doOnNext { logger.info("房间将于一分钟后关闭。{}", event) }
                        .subscribeOn(Schedulers.elastic()).subscribe()
                }
            }
            SocketSessionStore.userInfoMap.forEach { _, userRoomInfo -> roomCloseNotify(userRoomInfo) }
            SocketSessionStore.peekInfoMap.forEach { _, userRoomInfo -> roomCloseNotify(userRoomInfo) }
        }
    }

}
