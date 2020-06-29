package com.mt.mtsocket.service

import com.mt.mtcommon.TradeState
import com.mt.mtsocket.common.NotifyReq
import com.mt.mtsocket.entity.PushInfo
import com.mt.mtsocket.entity.ResponseInfo
import com.mt.mtsocket.entity.buyOrderUpdateEvent
import com.mt.mtsocket.entity.tradeUpdateEvent
import com.mt.mtsocket.socket.SocketSessionStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by gyh on 2020/5/19.
 */
@Service
class PeekPushService {
    private val store = SocketSessionStore
    private val events = Collections.newSetFromMap(ConcurrentHashMap<PushInfo, Boolean>())
    private val pushThread = PushThread()

    @Autowired
    private lateinit var redisUtil: RedisUtil

    init {
        pushThread.start(this)
    }

    fun addTradeInfoEvent(roomId: String, model: String) {
        events.add(PushInfo(roomId, model, tradeUpdateEvent))
    }

    fun addOrderEvent(roomId: String, model: String) {
        events.add(PushInfo(roomId, model, buyOrderUpdateEvent))
    }

    private fun pushTradeInfo(roomId: String) {
        val idList = store.peekInfoMap.entries.stream().filter { it.value.roomId == roomId }.map { it.value }
        redisUtil.getTradeInfo(roomId).collectList()
                .flatMap {
                    Flux.fromStream(idList).flatMap { info ->
                        info.session.send(ResponseInfo.ok("订单发生变化", it), NotifyReq.pushTradeInfo)
                    }.then()
                }.subscribeOn(Schedulers.elastic()).subscribe()
    }

    private fun pushOrder(roomId: String) {
        val idList = store.peekInfoMap.entries.stream().filter { it.value.roomId == roomId }.map { it.value }
        redisUtil.getUserOrder(roomId)
                .filter { it.tradeState == TradeState.STAY }
                .collectList()
                .flatMap {
                    Flux.fromStream(idList).flatMap { info ->
                        info.session.send(ResponseInfo.ok("报价发生变化", it), NotifyReq.pushBuyOrder)
                    }.then()
                }.subscribeOn(Schedulers.elastic()).subscribe()
    }

    class PushThread : Thread() {
        private val logger = LoggerFactory.getLogger(this.javaClass)
        private lateinit var peekPushService: PeekPushService
        private var time = 0L

        fun start(peekPushService: PeekPushService) {
            this.peekPushService = peekPushService
            name = "pushThread"
            super.start()
        }

        override fun run() {
            while (true) {
                if (System.currentTimeMillis() - time < 1000) {
                    sleep(1000 - (System.currentTimeMillis() - time))
                }
                val size = peekPushService.events.size
                if (size > 0) {
                    logger.info("push {}", size)
                }
                time = System.currentTimeMillis()
                val it = peekPushService.events.iterator()
                while (it.hasNext()) {
                    val info = it.next()
                    when (info.event) {
                        tradeUpdateEvent -> peekPushService.pushTradeInfo(info.roomId)
                        buyOrderUpdateEvent -> peekPushService.pushOrder(info.roomId)
                    }
                    it.remove()
                }
            }
        }
    }


}