package com.mt.mtengine.service

import com.mt.mtcommon.RoomEnum
import com.mt.mtengine.dao.RoomRecordDao
import com.mt.mtengine.dao.room.*
import com.mt.mtengine.entity.room.BaseRoom
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

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

    // 必须open
    open class CompanyStockId(val companyId: Int, val stockId: Int)

    fun findCompanyIdByRoomId(roomId: String, flag: String): Mono<CompanyStockId> {
        val dao = getBaseRoomDao(flag)
        return dao.findBaseByRoomId(roomId)
            .map { CompanyStockId(it.companyId!!, it.stockId!!) }
    }

    /**
     * 通过房间号号获取对应的dao
     */
    fun getBaseRoomDao(flag: String): BaseRoomDao<out BaseRoom, String> {
        return when (flag) {
            RoomEnum.CLICK.mode -> clickRoomDao
            RoomEnum.BICKER.mode -> bickerRoomDao
            RoomEnum.DOUBLE.mode -> doubleRoomDao
            RoomEnum.CONTINUE.mode -> timelyRoomDao
            RoomEnum.TIMING.mode -> timingRoomDao
            else -> throw IllegalStateException("不支持的房间号")
        }
    }
}