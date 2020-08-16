package com.mt.mtsocket.socket

import com.mt.mtsocket.common.NotifyOrder
import com.mt.mtsocket.distribute.DispatcherServlet
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
    private lateinit var dispatcherServlet: DispatcherServlet

    @Autowired
    private lateinit var roomSocketService: RoomSocketService

    override fun getServlet(): DispatcherServlet = dispatcherServlet

    override fun onConnect(queryMap: Map<String, String>, sessionHandler: WebSocketSessionHandler): Mono<*> {
        val userName = queryMap["userName"] ?: return sessionHandler.send("错误，不支持的参数列表$queryMap")
                .then(sessionHandler.connectionClosed())
        val roomId = queryMap["roomId"] ?: return sessionHandler.send("错误，不支持的参数列表$queryMap")
                .then(sessionHandler.connectionClosed())
        return roomSocketService.enterRoom(roomId)
                .flatMap { SocketSessionStore.addUser(sessionHandler, it.roomId!!, it.mode!!, userName) }
                .map { roomSocketService.onNumberChange(roomId) }
                .onErrorResume {
                    sessionHandler.send(ResponseInfo.failed("错误: ${it.message}"), NotifyOrder.errorNotify)
                            .doOnNext { msg -> logger.info("send $msg") }.flatMap { Mono.empty<Unit>() }
                }
    }

    override fun onDisconnected(queryMap: Map<String, String>, sessionHandler: WebSocketSessionHandler): Mono<*> {
        val roomId = queryMap["roomId"].toString()
        return BaseUser.getcurrentUser()
                .map { SocketSessionStore.removeUser(it.id!!) }
                .map { roomSocketService.onNumberChange(roomId) }
    }

}
