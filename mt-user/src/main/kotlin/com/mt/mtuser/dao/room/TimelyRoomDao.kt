package com.mt.mtuser.dao.room

import com.mt.mtuser.entity.room.TimelyMatch
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * Created by gyh on 2020/3/23.
 * 及时撮合
 */
interface TimelyRoomDao : CoroutineCrudRepository<TimelyMatch, Int>, BaseRoomDao {

    @Query("select count(1) from mt_room_timely where room_number = :roomNumber limit 1")
    override suspend fun existsByRoomNumber(roomNumber: String): Int

    @Modifying
    @Query("update mt_room_timely set enable = :enable where room_number = :roomNumber")
    override suspend fun enableRoomByRoomNumber(roomNumber: String, enable:String): Int

    @Modifying
    @Query("update mt_room_timely set enable = :enable where id = :id")
    override suspend fun enableRoomById(id: Int, enable: String): Int

    @Query("select count(1) from mt_room_timely where company_id = :companyId")
    suspend fun countByCompanyId(companyId: Int): Int
}