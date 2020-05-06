package com.mt.mtengine.service

import com.mt.mtcommon.*
import com.mt.mtengine.dao.RoomRecordDao
import com.mt.mtengine.dao.room.*
import com.mt.mtengine.entity.room.BaseRoom
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.relational.core.query.Criteria
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Duration

/**
 * Created by gyh on 2020/5/2.
 */
@Service
class RoomService {
    val logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    @Autowired
    private lateinit var clickRoomDao: ClickRoomDao

    @Autowired
    private lateinit var bickerRoomDao: BickerRoomDao

    @Autowired
    private lateinit var r2dbc: R2dbcService

    @Autowired
    private lateinit var roomRecordDao: RoomRecordDao

    @Autowired
    private lateinit var doubleRoomDao: DoubleRoomDao

    @Autowired
    private lateinit var timelyRoomDao: TimelyRoomDao

    @Autowired
    private lateinit var timingRoomDao: TimingRoomDao

    @Autowired
    private lateinit var redisUtil: RedisUtil

    // 必须open
    open class CompanyStockId(val companyId: Int, val stockId: Int)

    @Cacheable("companyAndStockId")
    fun findCompanyIdByRoomId(roomId: String): Mono<CompanyStockId> {
        val dao = getBaseRoomDao(roomId)
        return dao.findBaseByRoomId(roomId)
                .map { CompanyStockId(it.companyId!!, it.stockId!!) }
                .cache(Duration.ofHours(12))
    }

    /**
     * 通过房间号号获取对应的dao
     */
    fun getBaseRoomDao(roomId: String): BaseRoomDao<out BaseRoom, String> {
        return when (roomId.substring(0, 1)) {
            RoomEnum.CLICK.flag -> clickRoomDao
            RoomEnum.BICKER.flag -> bickerRoomDao
            RoomEnum.DOUBLE.flag -> doubleRoomDao
            RoomEnum.CONTINUE.flag -> timelyRoomDao
            RoomEnum.TIMING.flag -> timingRoomDao
            else -> throw IllegalStateException("不支持的房间号")
        }
    }

    /**
     * 使能一个房间
     * @param value true：启用一个房间 false 关闭一个房间
     */
    fun enableRoom(roomId: String, value: Boolean) = r2dbc.withTransaction {
        val dao = getBaseRoomDao(roomId)
        dao.findByRoomId(roomId)
                .filter { isAfterToday(it.time!!) }
                .flatMap { room ->
                    val roomRecord = room.toRoomRecord()
                    val result = if (value) {
                        val startTime = System.currentTimeMillis()
                        roomRecord.startTime = startTime.toDate()
                        roomRecord.endTime = (room.time!!.toMillisOfDay() + startTime).toDate()
                        roomRecordDao.save(roomRecord)
                                .flatMap { redisUtil.saveRoomRecord(it) }
                    } else {
                        redisUtil.deleteAndGetRoomRecord(roomId)
                                .flatMap {
                                    it.endTime = System.currentTimeMillis().toDate()
                                    it.computingTime()
                                    r2dbc.dynamicUpdate(it)
                                            .matching(Criteria.where("id").`is`(it.id!!))
                                            .fetch().rowsUpdated()
                                }
                    }
                    result.flatMap { dao.enableRoomById(roomId, room.isEnable<BaseRoom>(value).enable!!) }
                }.flatMap { redisUtil.publishRoomEvent(RoomEvent(roomId, value)) }
    }
}