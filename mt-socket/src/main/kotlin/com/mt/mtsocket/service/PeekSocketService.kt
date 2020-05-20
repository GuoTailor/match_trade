package com.mt.mtsocket.service

import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.TradeInfo
import com.mt.mtcommon.TradeState
import com.mt.mtsocket.entity.BaseUser
import com.mt.mtsocket.entity.ResponseInfo
import com.mt.mtsocket.socket.SocketSessionStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.stream.Collectors

/**
 * Created by gyh on 2020/5/19.
 */
@Service
class PeekSocketService {
    private val store = SocketSessionStore
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private lateinit var redisUtil: RedisUtil

    /**
     * 获取交易信息
     */
    fun getTradeInfo(): Mono<MutableList<TradeInfo>> {
        return BaseUser.getcurrentUser().flatMap {
            val userInfo = store.getPeekInfo(it.id!!)
                    ?: return@flatMap Mono.error<MutableList<TradeInfo>>(IllegalStateException("错误，用户没有进入房间"))
            redisUtil.getTradeInfo(userInfo.roomId).collectList()
        }
    }

    /**
     * 获取全部房间订单
     */
    fun getOrder(): Mono<MutableList<OrderParam>> {
        return BaseUser.getcurrentUser().flatMap { user ->
            val userInfo = store.getPeekInfo(user.id!!)
                    ?: return@flatMap Mono.error<MutableList<OrderParam>>(IllegalStateException("错误，用户没有进入房间"))
            redisUtil.getUserOrder(userInfo.roomId)
                    .filter { it.tradeState == TradeState.STAY }
                    .collectList()
        }
    }

}