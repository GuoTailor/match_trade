package com.mt.mtsocket.config.socket

import com.mt.mtsocket.entity.BaseUser
import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.server.Session
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashSet

/**
 * Created by gyh on 2020/4/12.
 */
object SocketSessionStore {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val userSession = ConcurrentHashMap<Int, WebSocketSession>()
    private val userRoom = ConcurrentHashMap<Int, String>() // todo 整合到一起

    fun addUser(session: WebSocketSession, roomId: String): Mono<Unit> {
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

    fun getSession(id: Int): WebSocketSession? {
        return userSession[id]
    }

    fun getAllSession(): MutableCollection<WebSocketSession> {
        return userSession.values
    }

    fun getRoom(userId: Int): String? {
        return userRoom[userId]
    }
}
