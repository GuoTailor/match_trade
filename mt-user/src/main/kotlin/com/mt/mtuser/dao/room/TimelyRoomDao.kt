package com.mt.mtuser.dao.room

import com.mt.mtuser.entity.room.TimelyMatch
import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query

/**
 * Created by gyh on 2020/3/23.
 * 及时撮合
 */
interface TimelyRoomDao :  BaseRoomDao<TimelyMatch, String> {

    @Query("select count(1) from mt_room_timely where room_id = :roomId limit 1")
    override suspend fun existsByRoomId(roomId: String): Int

    @Modifying
    @Query("update mt_room_timely set enable = :enable where room_id = :roomId")
    override suspend fun enableRoomById(roomId: String, enable: String): Int

    @Query("select count(1) from mt_room_timely where company_id = :companyId")
    suspend fun countByCompanyId(companyId: Int): Int

    @Query("select * from mt_room_timely where room_id = ：roomId")
    override suspend fun findByRoomId(roomId: String): TimelyMatch?

    @Query("select * from mt_room_timely where company_id in (:companyId)")
    override fun findByCompanyIdAll(companyId: Iterable<Int>): Flow<TimelyMatch>

}