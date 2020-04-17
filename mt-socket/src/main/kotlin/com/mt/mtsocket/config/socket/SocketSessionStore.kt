package com.mt.mtsocket.config.socket

import com.mt.mtsocket.entity.BaseUser
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
    private val userSession = ConcurrentHashMap<Int, WebSocketSession>()
    private val userRoom = ConcurrentHashMap<Int, String>() // todo 整合到一起

    fun addUser(session: WebSocketSession, roomId: String): Mono<Unit> {
        return BaseUser.getcurrentUser()
                .map {
                    userSession[it.id!!] = session
                    userRoom[it.id!!] = roomId
                }
    }

    fun getSession(id: Int): WebSocketSession? {
        return userSession[id]
    }

    fun removeSession(id: Int): WebSocketSession? {
        return userSession.remove(id)
    }

    fun getAllSession() : MutableCollection<WebSocketSession> {
        return userSession.values
    }

    fun getRoom(userId: Int): String? {
        return userRoom[userId]
    }
}
