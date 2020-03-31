package com.mt.mtuser.dao.room

import com.mt.mtuser.entity.room.DoubleMatch
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * Created by gyh on 2020/3/23.
 * 两两撮合
 */
interface DoubleRoomDao :  BaseRoomDao<DoubleMatch, String> {

    @Query("select count(1) from mt_room_double where room_id = :roomId limit 1")
    override suspend fun existsByRoomId(roomId: String): Int

    @Modifying
    @Query("update mt_room_double set enable = :enable where room_id = :roomId")
    override suspend fun enableRoomById(roomId: String, enable: String): Int

    @Query("select count(1) from mt_room_double where company_id = :companyId")
    suspend fun countByCompanyId(companyId: Int): Int

    @Query("select * from mt_room_double where room_id = ：roomId")
    override suspend fun findByRoomId(roomId: String): DoubleMatch?

}