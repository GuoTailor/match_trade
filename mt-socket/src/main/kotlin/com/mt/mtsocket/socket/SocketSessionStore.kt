package com.mt.mtsocket.socket

import com.mt.mtsocket.entity.BaseUser
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by gyh on 2020/4/12.
 */
object SocketSessionStore {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    internal val userSession = ConcurrentHashMap<Int, WebSocketSessionHandler>()
    internal val userRoom = ConcurrentHashMap<Int, String>() // todo 整合到一起

    fun addUser(session: WebSocketSessionHandler, roomId: String): Mono<Unit> {
        return BaseUser.getcurrentUser()
                .map {
                    userSession[it.id!!] = session
                    userRoom[it.id!!] = roomId
                    logger.info("添加用户 ${it.id}")
                }
    }

    fun removeUser(userId: Int) {
        userSession.remove(userId)
        userRoom.remove(userId)
        logger.info("移除用户 $userId")
    }

    fun getSession(id: Int): WebSocketSessionHandler? {
        return userSession[id]
    }

    fun getAllSession(): MutableCollection<WebSocketSessionHandler> {
        return userSession.values
    }

    fun getRoom(userId: Int): String? {
        return userRoom[userId]
    }
}
