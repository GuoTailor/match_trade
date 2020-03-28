package com.mt.mtuser.dao.room

import com.mt.mtuser.entity.room.BaseRoom

/**
 * Created by gyh on 2020/3/25.
 */
interface BaseRoomDao<T : BaseRoom> {
    suspend fun existsByRoomNumber(roomNumber: String): Int

    suspend fun enableRoomByRoomNumber(roomNumber: String, enable:String): Int

    suspend fun enableRoomById(id: Int, enable: String): Int

    suspend fun findByRoomNumber(roomNumber: String): T

    suspend fun findLastRoomNumber(): String
}