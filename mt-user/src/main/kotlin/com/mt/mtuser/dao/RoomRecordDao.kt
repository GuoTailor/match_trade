package com.mt.mtuser.dao

import com.mt.mtcommon.RoomRecord
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.*

/**
 * Created by gyh on 2020/3/26.
 */
interface RoomRecordDao : CoroutineCrudRepository<RoomRecord, Int> {

    @Query("select count(1) from mt_room_record where start_time > time")
    suspend fun countByStartTime(time: Date): Int

    @Query("select count(1) from mt_room_record where start_time > time and company_id = :companyId")
    suspend fun countByStartTimeAndCompanyId(time: Date, companyId: Int): Int
}