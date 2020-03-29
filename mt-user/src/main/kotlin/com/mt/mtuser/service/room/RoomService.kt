package com.mt.mtuser.service.room

import com.mt.mtuser.common.Util
import com.mt.mtuser.common.toDate
import com.mt.mtuser.dao.CompanyDao
import com.mt.mtuser.dao.RoomRecordDao
import com.mt.mtuser.dao.room.*
import com.mt.mtuser.entity.RoomRecord
import com.mt.mtuser.entity.room.BaseRoom
import com.mt.mtuser.entity.room.ClickMatch
import com.mt.mtuser.service.DynamicSqlService
import com.mt.mtuser.service.RedisUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.awaitRowsUpdated
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Created by gyh on 2020/3/23.
 */
@Service
class RoomService {
    @Autowired
    private lateinit var clickRoomDao: ClickRoomDao

    @Autowired
    private lateinit var companyDao: CompanyDao

    @Autowired
    private lateinit var doubleRoomDao: DoubleRoomDao

    @Autowired
    private lateinit var timelyRoomDao: TimelyRoomDao

    @Autowired
    private lateinit var dynamicSql: DynamicSqlService

    @Autowired
    private lateinit var timingRoomDao: TimingRoomDao

    @Autowired
    private lateinit var roomRecordDao: RoomRecordDao

    @Autowired
    private lateinit var redisUtil: RedisUtil
    private val mutex = Mutex()

    suspend fun createClickRoom(clickRoom: ClickMatch): ClickMatch {
        // TODO 校验bean的数据合法性
        val company = companyDao.findById(clickRoom.companyId!!).awaitSingle()
        if (RoomExtend.getRoomDome(company.mode!!).contains(clickRoom.flag)) { // 判断房间模式
            clickRoom.id = null
            clickRoom.isEnable<ClickMatch>(false)
            return mutex.withLock {
                if (checkRoomCount(clickRoom.companyId!!)) {
                    var oldNumber = clickRoomDao.findLastRoomNumber()   //TODO
                    do {
                        clickRoom.roomNumber = Util.createNewNumber(oldNumber)
                        oldNumber =  clickRoom.roomNumber!!
                    } while (clickRoomDao.existsByRoomNumber(clickRoom.roomNumber!!) > 0)
                    clickRoomDao.save(clickRoom)
                } else {
                    throw IllegalStateException("公司房间已满")
                }
            }
        } else {
            throw IllegalStateException("不能创建该模式${clickRoom.flag}的房间")
        }
    }

    /**
     * 使能一个房间
     * @param value 1：启用一个房间 0 关闭一个房间
     */
    @Transactional
    suspend fun enableRoom(roomNumber: String, value: String): Int {
        // TODO 验证时间是否超过24点
        val dao: BaseRoomDao<*> = when (roomNumber.substring(0, 1)) {
            RoomEnum.CLICK.flag -> clickRoomDao
            RoomEnum.DOUBLE.flag -> doubleRoomDao
            RoomEnum.TIMELY.flag -> timelyRoomDao
            RoomEnum.TIMING.flag -> timingRoomDao
            else -> throw IllegalStateException("不支持的房间号")
        }
        val rest = dao.enableRoomByRoomNumber(roomNumber, value)
        val room: BaseRoom = dao.findByRoomNumber(roomNumber)
        var roomRecord = RoomRecord(room)
        if (value == "1") {
            val startTime = System.currentTimeMillis()
            roomRecord.startTime = startTime.toDate()
            roomRecord.endTime = (room.time!!.toMillis() + startTime).toDate()
            val newRecord = roomRecordDao.save(roomRecord)
            redisUtil.saveRoomRecord(newRecord)
        } else if (value == "0") {
            roomRecord = redisUtil.deleteAndGetRoomRecord(roomNumber)
            roomRecord.endTime = System.currentTimeMillis().toDate()
            roomRecord.getDuration()
            dynamicSql.dynamicUpdate(roomRecord)
                    .matching(where("id").`is`(roomRecord.id!!))
                    .fetch().awaitRowsUpdated()
            // TODO
        }
        return rest
    }

    suspend fun <T : BaseRoom> updateRoomById(room: BaseRoom): Int {
        room.id ?: throw IllegalStateException("请指定id")
        room.enable = null
        room.roomNumber = null
        return dynamicSql.dynamicUpdate(room)
                .matching(where("id").`is`(room.id!!))
                .fetch().awaitRowsUpdated()
    }

    /**
     * 通过房间号更新一个房间的配置
     */
    suspend fun <T : BaseRoom> updateRoomByRoomNumber(room: T): Int {
        // TODO 修改结束时间
        val roomNumber = room.roomNumber ?: throw IllegalStateException("请指定房间号")
        room.roomNumber = null
        room.enable = null
        room.id = null
        return dynamicSql.dynamicUpdate(room)
                .matching(where("room_number").`is`(roomNumber))
                .fetch().awaitRowsUpdated()
    }

    /**
     * 检查房间数量，异步同时获取四种房间的数量
     */
    suspend fun checkRoomCount(companyId: Int): Boolean = coroutineScope {
        val company = async { companyDao.findById(companyId).awaitSingle() }
        val countClick = async { clickRoomDao.countByCompanyId(companyId) }
        val countDouble = async { doubleRoomDao.countByCompanyId(companyId) }
        val countTimely = async { timelyRoomDao.countByCompanyId(companyId) }
        val countTiming = async { timingRoomDao.countByCompanyId(companyId) }
        company.await().roomCount!! > (countClick.await() + countDouble.await() + countTimely.await() + countTiming.await())
    }
}