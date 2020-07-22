package com.mt.mtuser.service

import com.mt.mtcommon.toLocalDateTime
import com.mt.mtuser.dao.RoomRecordDao
import com.mt.mtuser.entity.Stockholder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate
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

    @Autowired
    private lateinit var connect: DatabaseClient

    suspend fun countByStartTime(time: LocalDateTime = LocalTime.MIN.toLocalDateTime()) = roomRecordDao.countByStartTime(time)

    suspend fun countByStartTimeAndCompanyId(time: LocalDateTime = LocalTime.MIN.toLocalDateTime()): Int {
        val companyId = roleService.getCompanyList(Stockholder.ADMIN)[0]
        return countByStartTimeAndCompanyId(time, companyId)
    }

    suspend fun countByStartTimeAndCompanyId(time: LocalDateTime, companyId: Int) =
            roomRecordDao.countByStartTimeAndCompanyId(time, companyId)

    suspend fun countByStartTimeAndEndTimeAndCompanyId(startTime: LocalDateTime, endTime: LocalDateTime, companyId: Int) =
            roomRecordDao.countByStartTimeAndEndTimeAndCompanyId(startTime, endTime, companyId)

    suspend fun countByCompanyId(companyId: Int) = roomRecordDao.countByCompanyId(companyId)

    suspend fun countCompanyIdByStartTime(startTime: LocalDateTime = LocalTime.MIN.toLocalDateTime()) =
            roomRecordDao.countCompanyIdByStartTime(startTime)

    suspend fun count() = roomRecordDao.count()

    fun countTopCompany(): Mono<MutableList<Map<String, Any?>>> {
        return connect.execute("SELECT rr.company_id,  COUNT(1), " +
                " ( SELECT C.name FROM mt_company C WHERE C.id = rr.company_id ), " +
                " ( SELECT SUM ( ti.trade_money ) AS money FROM mt_trade_info ti WHERE ti.company_id = rr.company_id ) , " +
                " ( SELECT sum(ti.trade_amount) as amount FROM mt_trade_info ti WHERE ti.company_id = rr.company_id ) " +
                "FROM mt_room_record rr  " +
                "WHERE rr.start_time > :time " +
                "GROUP BY  rr.company_id" +
                " limit 10")
                .bind("time", LocalDate.now())
                .map { r, _ ->
                    mapOf("companyId" to r.get("company_id", java.lang.Integer::class.java),
                            "openNumber" to r.get("count", java.lang.Integer::class.java),
                            "name" to r.get("name", String::class.java),
                            "money" to r.get("money", BigDecimal::class.java),
                            "amount" to r.get("amount", java.lang.Integer::class.java))
                }.all().collectList()
    }

}