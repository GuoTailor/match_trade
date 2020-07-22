package com.mt.mtuser.dao

import com.mt.mtcommon.RoomRecord
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDateTime
import java.util.*

/**
 * Created by gyh on 2020/3/26.
 */
interface RoomRecordDao : CoroutineCrudRepository<RoomRecord, Int> {

    @Query("select count(1) from mt_room_record where start_time > :time")
    suspend fun countByStartTime(time: LocalDateTime): Int

    @Query("select count(1) from mt_room_record where start_time > :time and company_id = :companyId")
    suspend fun countByStartTimeAndCompanyId(time: LocalDateTime, companyId: Int): Int

    @Query("select count(1) from mt_room_record where company_id = :companyId")
    suspend fun countByCompanyId(companyId: Int): Int

    @Query("select count(1) form mt_room_record where start_time between :startTime and :endTime and company_id = :companyId")
    suspend fun countByStartTimeAndEndTimeAndCompanyId(startTime: LocalDateTime, endTime: LocalDateTime, companyId: Int): Long

    @Query("select count(*) from (select count(1) from mt_room_record where start_time > :time group by company_id) a")
    suspend fun countCompanyIdByStartTime(time: LocalDateTime): Long?

    @Query("select * from mt_room_record where room_id = :roomId order by start_time desc limit 1")
    suspend fun findLastRecordByRoomId(roomId: String): RoomRecord?

    @Query("select * from mt_room_record where room_id = :roomId and end_time < :endTime order by start_time desc limit 1")
    suspend fun findLastRecordByRoomIdAndEndTime(roomId: String, endTime: LocalDateTime): RoomRecord?

    @Query("select * from mt_room_record where room_id = :roomId and start_time < :endTime order by start_time desc limit 1")
    suspend fun findLastRecordByRoomId(endTime: LocalDateTime, roomId: String): RoomRecord?

    @Query("select * from mt_room_record where room_id = :roomId and end_time < :endTime order by start_time desc limit 1 offset 1")
    suspend fun findSecondRecordByRoomId(roomId: String, endTime: LocalDateTime): RoomRecord?
}