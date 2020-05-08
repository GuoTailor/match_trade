package com.mt.mtsocket.socket

import com.mt.mtcommon.RoomRecord
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

    fun addUser(session: WebSocketSessionHandler, roomRecord: RoomRecord): Mono<Unit> {
        return BaseUser.getcurrentUser()    // TODO 用户多地登陆会存在问题，同一userId会有不同的roomId，导致撤单操作混乱
                .map {
                    val userInfo = UserRoomInfo(session, it.id!!, roomRecord)
                    userInfoMap[it.id!!] = userInfo
                    logger.info("添加用户 $it")
                }
    }

    fun removeUser(userId: Int) {
        userInfoMap.remove(userId)
        logger.info("移除用户 $userId")
    }

    fun getRoom(userId: Int): UserRoomInfo? {
        return userInfoMap[userId]
    }

    data class UserRoomInfo(val session: WebSocketSessionHandler,
                            val userId: Int,
                            val roomRecord: RoomRecord)     // TODO 可能的内存瓶颈，不应该为每个用户保存一个房间记录，而是一个房间号保存一个房间记录
}
