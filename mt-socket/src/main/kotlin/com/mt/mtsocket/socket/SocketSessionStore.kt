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
    internal val userInfoMap = ConcurrentHashMap<Int, UserRoomInfo>()
    internal val peekInfoMap = ConcurrentHashMap<Int, UserRoomInfo>()

    fun addUser(session: WebSocketSessionHandler, roomId: String, model: String, userName: String): Mono<Unit> {
        return BaseUser.getcurrentUser()    // TODO 用户多地登陆会存在问题，同一userId会有不同的roomId，导致撤单操作混乱
                .map {
                    val userInfo = UserRoomInfo(session, it.id!!, userName, roomId, model)
                    userInfoMap[it.id!!] = userInfo
                    logger.info("添加用户 $it")
                }
    }

    fun removeUser(userId: Int) {
        userInfoMap.remove(userId)
        logger.info("移除用户 $userId")
    }

    fun getRoomInfo(userId: Int): UserRoomInfo? {
        return userInfoMap[userId]
    }

    fun getOnLineSize(roomId: String): Int {
        return userInfoMap.count { entry -> entry.value.roomId == roomId }
    }

    fun addPeek(session: WebSocketSessionHandler, roomId: String, model: String): Mono<Unit> {
        return BaseUser.getcurrentUser().map {
            peekInfoMap[it.id!!] = UserRoomInfo(session, it.id!!, "", roomId, model)
            logger.info("添加监视 ${it.id}")
        }
    }

    fun getPeekInfo(userId: Int): UserRoomInfo? {
        return peekInfoMap[userId]
    }

    fun removePeek(userId: Int) {
        peekInfoMap.remove(userId)
        logger.info("移除监视 $userId")
    }

    data class UserRoomInfo(val session: WebSocketSessionHandler,
                            val userId: Int,
                            val userName: String,
                            val roomId: String,
                            val mode: String)
}
