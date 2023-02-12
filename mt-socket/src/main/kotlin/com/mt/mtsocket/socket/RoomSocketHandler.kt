package com.mt.mtsocket.socket

import com.mt.mtsocket.common.NotifyOrder
import com.mt.mtsocket.entity.BaseUser
import com.mt.mtsocket.entity.ResponseInfo
import com.mt.mtsocket.service.RoomSocketService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/4/5.
 */
@WebSocketMapping("/room")
class RoomSocketHandler : SocketHandler() {
    private val logger = LoggerFactory.getLogger(this.javaClass)


    @Autowired
    private lateinit var roomSocketService: RoomSocketService


    override fun onConnect(queryMap: Map<String, String>, sessionHandler: SessionHandler): Mono<*> {
        val userName = queryMap["userName"] ?: return sessionHandler
            .send(Mono.just("错误，不支持的参数列表$queryMap"), NotifyOrder.errorNotify)
            .map { sessionHandler.tryEmitComplete() }
        val roomId = queryMap["roomId"] ?: return sessionHandler
            .send(Mono.just("错误，不支持的参数列表$queryMap"), NotifyOrder.errorNotify)
            .map { sessionHandler.tryEmitComplete() }
        return roomSocketService.enterRoom(roomId)
            .flatMap { SocketSessionStore.addUser(sessionHandler, it.roomId!!, it.mode!!, userName) }
            .flatMap { BaseUser.getcurrentUser() }
            .map { sessionHandler.dataMap["id"] = it.id!! }
            .map { roomSocketService.onNumberChange(roomId) }
            .onErrorResume {
                sessionHandler.send(ResponseInfo.failed("错误: ${it.message}"), NotifyOrder.errorNotify)
                    .doOnNext { msg -> logger.info("send $msg") }.flatMap { Mono.empty() }
            }
    }

    override fun onDisconnected(queryMap: Map<String, String>, sessionHandler: SessionHandler) {
        val roomId = queryMap["roomId"].toString()
        val id = sessionHandler.dataMap["id"] as Int
        SocketSessionStore.removeUser(id)
        roomSocketService.onNumberChange(roomId)
    }

}
