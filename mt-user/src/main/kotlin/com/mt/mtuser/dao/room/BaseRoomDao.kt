package com.mt.mtuser.dao.room

/**
 * Created by gyh on 2020/3/25.
 */
interface BaseRoomDao {
    suspend fun existsByRoomNumber(roomNumber: String): Int

    suspend fun enableRoomByRoomNumber(roomNumber: String, enable:String): Int

    suspend fun enableRoomById(id: Int, enable: String): Int
}