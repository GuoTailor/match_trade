package com.mt.mtuser.service

import com.mt.mtcommon.toLocalDateTime
import com.mt.mtuser.dao.RoomRecordDao
import com.mt.mtuser.entity.Stockholder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Created by gyh on 2020/5/7.
 */
@Service
class RoomRecordService {
    @Autowired
    private lateinit var roomRecordDao: RoomRecordDao

    @Autowired
    private lateinit var roleService: RoleService

    suspend fun countByStartTime(time: LocalDateTime = LocalTime.MIN.toLocalDateTime()) = roomRecordDao.countByStartTime(time)

    suspend fun countByStartTimeAndCompanyId(time: LocalDateTime = LocalTime.MIN.toLocalDateTime()): Int {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        return roomRecordDao.countByStartTimeAndCompanyId(time, companyId)
    }

    suspend fun countByStartTimeAndEndTimeAndCompanyId(startTime: LocalDateTime, endTime: LocalDateTime, companyId: Int) =
            roomRecordDao.countByStartTimeAndEndTimeAndCompanyId(startTime, endTime, companyId)

    suspend fun countByCompanyId(companyId: Int) = roomRecordDao.countByCompanyId(companyId)

}