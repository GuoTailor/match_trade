package com.mt.mtsocket.socket

import com.mt.mtsocket.common.NotifyOrder
import com.mt.mtsocket.entity.ResponseInfo
import com.mt.mtsocket.service.RedisUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/5/18.
 */
@WebSocketMapping("/peek")
class PeekSocketHandler : SocketHandler() {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Autowired
    private lateinit var redisUtil: RedisUtil

    override fun onConnect(queryMap: Map<String, String>, sessionHandler: SessionHandler): Mono<*> {
        val roomId = queryMap["roomId"] ?: return sessionHandler
            .send(Mono.just("错误，不支持的参数列表$queryMap"), NotifyOrder.errorNotify)
            .map { sessionHandler.tryEmitComplete() }
        return redisUtil.getRoomRecord(roomId).flatMap {
            SocketSessionStore.addPeek(sessionHandler, it.roomId!!, it.mode!!)  // TODO 缺少权限判断
        }.switchIfEmpty(sessionHandler.send(ResponseInfo.failed("错误: 房间还没开启"), NotifyOrder.errorNotify)
            .doOnNext { msg -> logger.info("send $msg") }.flatMap { Mono.empty<Unit>() })
    }

    override fun onDisconnected(queryMap: Map<String, String>, sessionHandler: SessionHandler) {
        val id = sessionHandler.dataMap["id"] as Int
        SocketSessionStore.removeUser(id)
    }
}
