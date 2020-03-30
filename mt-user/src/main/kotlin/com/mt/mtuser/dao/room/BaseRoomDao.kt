package com.mt.mtuser.dao.room

import com.mt.mtuser.entity.room.BaseRoom
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * Created by gyh on 2020/3/25.
 */
interface BaseRoomDao<T : BaseRoom> : CoroutineCrudRepository<T, Int> {
    suspend fun existsByRoomNumber(roomNumber: String): Int

    /**
     * 通过房间号启用/禁用房间
     */
    suspend fun enableRoomByRoomNumber(roomNumber: String, enable: String): Int

    suspend fun enableRoomById(id: Int, enable: String): Int

    suspend fun findByRoomNumber(roomNumber: String): T?

    suspend fun findLastRoomNumber(): String?
}