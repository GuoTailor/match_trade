package com.mt.mtuser.dao.room

import com.mt.mtuser.entity.room.BaseRoom
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * Created by gyh on 2020/3/25.
 */
interface BaseRoomDao<T : BaseRoom, ID> : CoroutineCrudRepository<T, ID> {
    suspend fun existsByRoomId(roomId: String): Int

    /**
     * 通过房间id启用/禁用房间
     */
    suspend fun enableRoomById(roomId: String, enable: String): Int

    suspend fun findByRoomId(roomId: String): T?

    fun findByCompanyIdAll(companyId: Iterable<Int>): Flow<T>

    fun findTimeAll(): Flow<T>
}