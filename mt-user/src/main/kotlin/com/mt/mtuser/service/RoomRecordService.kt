package com.mt.mtuser.service

import com.mt.mtcommon.toDate
import com.mt.mtuser.dao.RoomRecordDao
import com.mt.mtuser.entity.Stockholder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalTime
import java.util.*

/**
 * Created by gyh on 2020/5/7.
 */
@Service
class RoomRecordService {
    @Autowired
    private lateinit var roomRecordDao: RoomRecordDao

    @Autowired
    private lateinit var roleService: RoleService

    suspend fun countByStartTime(time: Date = LocalTime.MIN.toDate()) = roomRecordDao.countByStartTime(time)

    suspend fun countByStartTimeAndCompanyId(time: Date = LocalTime.MIN.toDate()): Int {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        return roomRecordDao.countByStartTimeAndCompanyId(time, companyId)
    }

}