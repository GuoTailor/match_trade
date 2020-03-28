package com.mt.mtuser.dao.room

import com.mt.mtuser.entity.room.TimingMatch
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

/**
 * Created by gyh on 2020/3/23.
 * 定时撮合
 */
interface TimingRoomDao : CoroutineCrudRepository<TimingMatch, Int>, BaseRoomDao<TimingMatch> {

    @Query("select count(1) from mt_room_timing where room_number = :roomNumber limit 1")
    override suspend fun existsByRoomNumber(roomNumber: String): Int

    @Modifying
    @Query("update mt_room_timing set enable = :enable where room_number = :roomNumber")
    override suspend fun enableRoomByRoomNumber(roomNumber: String, enable: String): Int

    @Modifying
    @Query("update mt_room_timing set enable = :enable where id = :id")
    override suspend fun enableRoomById(id: Int, enable: String): Int

    @Query("select count(1) from mt_room_timing where company_id = :companyId")
    suspend fun countByCompanyId(companyId: Int): Int

    @Query("select * from mt_room_timing where room_number = ：roomNumber")
    override suspend fun findByRoomNumber(roomNumber: String): TimingMatch

    @Query("select room_number from mt_room_timing order by room_number desc limit 1")
    override suspend fun findLastRoomNumber(): String
}