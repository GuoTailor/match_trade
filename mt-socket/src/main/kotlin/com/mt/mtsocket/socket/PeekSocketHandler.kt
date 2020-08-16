package com.mt.mtsocket.socket

import com.mt.mtsocket.common.NotifyOrder
import com.mt.mtsocket.distribute.DispatcherServlet
import com.mt.mtsocket.entity.BaseUser
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

    @Autowired
    private lateinit var dispatcherServlet: DispatcherServlet

    override fun getServlet(): DispatcherServlet = dispatcherServlet

    override fun onConnect(queryMap: Map<String, String>, sessionHandler: WebSocketSessionHandler): Mono<*> {
        val roomId = queryMap["roomId"] ?: return sessionHandler.send("错误，不支持的参数列表$queryMap")
                .then(sessionHandler.connectionClosed())
        return redisUtil.getRoomRecord(roomId).flatMap {
            SocketSessionStore.addPeek(sessionHandler, it.roomId!!, it.mode!!)  // TODO 缺少权限判断
        }.switchIfEmpty(sessionHandler.send(ResponseInfo.failed("错误: 房间还没开启"), NotifyOrder.errorNotify)
                .doOnNext { msg -> logger.info("send $msg") }.flatMap { Mono.empty<Unit>() })
    }

    override fun onDisconnected(queryMap: Map<String, String>, sessionHandler: WebSocketSessionHandler): Mono<*> {
        return BaseUser.getcurrentUser()
                .map { SocketSessionStore.removePeek(it.id!!) }.then()
    }
}