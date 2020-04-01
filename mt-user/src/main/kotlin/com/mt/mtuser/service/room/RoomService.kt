package com.mt.mtuser.service.room

import com.mt.mtuser.common.isAfterToday
import com.mt.mtuser.common.toDate
import com.mt.mtuser.common.toMillis
import com.mt.mtuser.dao.CompanyDao
import com.mt.mtuser.dao.RoomRecordDao
import com.mt.mtuser.dao.room.*
import com.mt.mtuser.entity.BaseUser
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
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.awaitRowsUpdated
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.stereotype.Service

/**
 * Created by gyh on 2020/3/23.
 */
@Service
class RoomService {
    val logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    @Autowired
    private lateinit var baseRoomService: BaseRoomService

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
    private val roomEnableMutex = Mutex()   // 房间启用和禁用的互斥锁
    private val roomEnterMutex = Mutex()   // 房间进入和退出的互斥锁

    /**
     * 使能一个房间
     * @param value 1：启用一个房间 0 关闭一个房间
     */
    //@Transactional 由于事务不支持挂起函数，所以注解只能打在普通函数上面并且一定要让报错抛出去，不然事务不会回退
    // TODO 提供事务支持 考虑手动调用事务回滚
    suspend fun enableRoom(roomId: String, value: String): Int {
        val dao = getBaseRoomDao<BaseRoom>(roomId)
        val rest = dao.enableRoomById(roomId, value)
        val room: BaseRoom = dao.findByRoomId(roomId) ?: throw IllegalStateException("房间号不存在")
        if (isAfterToday(room.time!!)) throw IllegalStateException("时长${room.time}超过今天结束时间：23:59:59.999999999")
        var roomRecord = RoomRecord(room)
        roomEnableMutex.withLock {
            if (value == BaseRoom.ENABLE) {
                val startTime = System.currentTimeMillis()
                roomRecord.startTime = startTime.toDate()
                roomRecord.endTime = (room.time!!.toMillis() + startTime).toDate()
                val newRecord = roomRecordDao.save(roomRecord)
                redisUtil.saveRoomRecord(newRecord)
            } else if (value == BaseRoom.DISABLED) {
                roomRecord = redisUtil.deleteAndGetRoomRecord(roomId)
                        ?: throw IllegalStateException("房间不存在：$roomId")
                roomRecord.endTime = System.currentTimeMillis().toDate()
                roomRecord.getDuration()
                dynamicSql.dynamicUpdate(roomRecord)
                        .matching(where("id").`is`(roomRecord.id!!))
                        .fetch().awaitRowsUpdated()
                // TODO 通知用户退出房间
            }
            return rest
        }
    }

    /**
     * 进入房间通过房间号
     */
    suspend fun enterRoom(roomId: String) {
        redisUtil.getRoomRecord(roomId) ?: throw IllegalStateException("房间已结束")
        val userId = BaseUser.getcurrentUser().awaitSingle().id!!
        roomEnterMutex.withLock {
            val list = redisUtil.getRoomPeople(roomId)
            list.add(userId)
            redisUtil.updateRoomPeople(roomId, list)
        }
    }

    /**
     * 退出房间过房间号
     */
    suspend fun quitRoom(roomId: String) {
        val userId = BaseUser.getcurrentUser().awaitSingle().id!!
        roomEnterMutex.withLock {
            val list = redisUtil.getRoomPeople(roomId)
            if (list.remove(userId))
                redisUtil.updateRoomPeople(roomId, list)
        }
    }

    /**
     * 更改房间模式
     */
    suspend fun <T : BaseRoom> changeModel(newRoom: T): T {
        // TODO 判断是否具有设置该模式的权限
        val oldRoomId = newRoom.roomId!!
        val oldDao = getBaseRoomDao<BaseRoom>(oldRoomId)
        val newDao = getBaseRoomDao(newRoom)
        roomEnableMutex.withLock {
            val room = oldDao.findByRoomId(oldRoomId) ?: throw IllegalStateException("房间号不存在")
            if (room.enable == BaseRoom.ENABLE) throw IllegalStateException("房间正在交易，不能切换模式")
            newRoom.roomId = RoomExtend.replaceRoomFlag(oldRoomId, newRoom)
            return coroutineScope {
                val one = async { oldDao.deleteById(oldRoomId) }
                val tew = async { newDao.save(newRoom) }
                one.await()
                tew.await()
            }
        }
    }

    /**
     * 通过房间id更新一个房间的配置
     */
    suspend fun <T : BaseRoom> updateRoomByRoomId(room: T): Int {
        // TODO 修改房间模式
        val roomId = room.roomId ?: throw IllegalStateException("请指定房间id")
        room.roomId = null
        room.enable = null
        return dynamicSql.dynamicUpdate(room)
                .matching(where("room_id").`is`(roomId))
                .fetch().awaitRowsUpdated()
    }

    /**
     * 检查房间数量，异步同时获取四种房间的数量
     */
    suspend fun checkRoomCount(companyId: Int): Boolean = coroutineScope {
        val company = async { companyDao.findById(companyId) }
        val countClick = async { clickRoomDao.countByCompanyId(companyId) }
        val countDouble = async { doubleRoomDao.countByCompanyId(companyId) }
        val countTimely = async { timelyRoomDao.countByCompanyId(companyId) }
        val countTiming = async { timingRoomDao.countByCompanyId(companyId) }
        company.await() ?: throw IllegalStateException("公司不存在：$companyId")
        company.await()?.roomCount!! > (countClick.await() + countDouble.await() + countTimely.await() + countTiming.await())
    }

    /**
     * 获取总的房间数量
     */
    suspend fun getAllRoomCount(): Long = coroutineScope {
        val countClick = async { clickRoomDao.count() }
        val countDouble = async { doubleRoomDao.count() }
        val countTimely = async { timelyRoomDao.count() }
        val countTiming = async { timingRoomDao.count() }
        countClick.await() + countDouble.await() + countTimely.await() + countTiming.await()
    }

    /**
     * 创建房间
     */
    suspend fun <T : BaseRoom> createRoom(room: T): T {
        val company = companyDao.findById(room.companyId!!)
        val dao = getBaseRoomDao<T>(room.flag)
        room.validNull()
        if (RoomExtend.getRoomModels(company?.mode!!).contains(room.flag)) { // 判断房间模式
            room.roomId = baseRoomService.getNextRoomId(room)               // 获取全局唯一的房间id
            room.isEnable<T>(false)
            if (checkRoomCount(room.companyId!!)) {
                logger.info((room as ClickMatch).toString())
                return dao.save(room)
            } else throw IllegalStateException("公司房间已满")
        } else throw IllegalStateException("不能创建该模式${room.flag}的房间")
    }

    suspend fun getAllRoomList() {

    }

    /**
     * 通过房间号号获取对应的dao
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : BaseRoom> getBaseRoomDao(roomId: String): BaseRoomDao<T, String> {
        return when (roomId.substring(0, 1)) {
            RoomEnum.CLICK.flag -> clickRoomDao as BaseRoomDao<T, String>
            RoomEnum.DOUBLE.flag -> doubleRoomDao as BaseRoomDao<T, String>
            RoomEnum.TIMELY.flag -> timelyRoomDao as BaseRoomDao<T, String>
            RoomEnum.TIMING.flag -> timingRoomDao as BaseRoomDao<T, String>
            else -> throw IllegalStateException("不支持的房间号")
        }
    }

    fun <T : BaseRoom> getBaseRoomDao(room: T): BaseRoomDao<T, String> = getBaseRoomDao(room.flag)

}
