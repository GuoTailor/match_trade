package com.mt.mtuser.service.room

import com.mt.mtuser.dao.CompanyDao
import com.mt.mtuser.dao.room.*
import com.mt.mtuser.entity.room.BaseRoom
import com.mt.mtuser.entity.room.ClickMatch
import com.mt.mtuser.service.DynamicSqlService
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitRowsUpdated
import org.springframework.data.r2dbc.query.Criteria.where
import org.springframework.stereotype.Service
import java.util.*

/**
 * Created by gyh on 2020/3/23.
 */
@Service
class RoomService {
    @Autowired
    protected lateinit var clickRoomDao: ClickRoomDao
    @Autowired
    protected lateinit var connect: DatabaseClient
    @Autowired
    protected lateinit var companyDao: CompanyDao
    @Autowired
    protected lateinit var doubleRoomDao: DoubleRoomDao
    @Autowired
    protected lateinit var timelyRoomDao: TimelyRoomDao
    @Autowired
    protected lateinit var dynamicSql: DynamicSqlService
    @Autowired
    protected lateinit var timingRoomDao: TimingRoomDao
    protected val mutex = Mutex()

    suspend fun createClickRoom(clickRoom: ClickMatch): ClickMatch {
        clickRoom.id = null
        clickRoom.startTime = Date()
        return mutex.withLock {
            do {
                clickRoom.createRoomNumber()    // 可能将来会出现死循环
            } while (clickRoomDao.existsByRoomNumber(clickRoom.roomNumber!!) > 1)
            clickRoomDao.save(clickRoom)
        }
    }

    /**
     * 使能一个房间
     * @param enable true：启用一个房间 else 关闭一个房间
     */
    suspend fun enableRoom(roomNumber: String, enable: String): Int {
        val dao: BaseRoomDao = when (roomNumber.substring(0, 1)) {
            RoomEnum.CLICK.flag -> clickRoomDao
            RoomEnum.DOUBLE.flag -> doubleRoomDao
            RoomEnum.TIMELY.flag -> timelyRoomDao
            RoomEnum.TIMING.flag -> timingRoomDao
            else -> throw IllegalStateException("不支持的房间号")
        }
        // TODO 更新启用记录
        return dao.enableRoomByRoomNumber(roomNumber, enable)
    }

    suspend  fun <T: BaseRoom<T>> updateRoomById(room: BaseRoom<T>): Int {
        // TODO 不能修改房间状态
        room.roomNumber = null
        room.id ?: throw IllegalStateException("请指定id")
        return connect.update()
                .table(dynamicSql.getTable(room.javaClass))
                .using(dynamicSql.dynamicUpdate(room))
                .matching(where("id").`is`(room.id!!))
                .fetch().awaitRowsUpdated()
    }

    /**
     * 通过房间号更新一个房间的配置
     */
    suspend  fun <T: BaseRoom<T>> updateRoomByRoomNumber(room: BaseRoom<T>): Int {
        val roomNumber = room.roomNumber
        roomNumber ?: throw IllegalStateException("请指定房间号")
        room.roomNumber = null
        return connect.update()
                .table(dynamicSql.getTable(room.javaClass))
                .using(dynamicSql.dynamicUpdate(room))
                .matching(where("room_number").`is`(roomNumber))
                .fetch().awaitRowsUpdated()
    }


    suspend fun checkRoomCount(companyId: Int) {
        val company = companyDao.findById(companyId).awaitSingle()
        clickRoomDao
        doubleRoomDao
        timelyRoomDao
        timingRoomDao
    }
}