package com.mt.mtuser.service.room

import com.mt.mtcommon.*
import com.mt.mtuser.dao.CompanyDao
import com.mt.mtuser.dao.RoomRecordDao
import com.mt.mtuser.dao.room.*
import com.mt.mtuser.entity.Stockholder
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.room.BaseRoom
import com.mt.mtuser.schedule.*
import com.mt.mtuser.service.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.core.awaitRowsUpdated
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

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
    private lateinit var bickerRoomDao: BickerRoomDao

    @Autowired
    private lateinit var companyDao: CompanyDao

    @Autowired
    private lateinit var doubleRoomDao: DoubleRoomDao

    @Autowired
    private lateinit var continueRoomDao: ContinueRoomDao

    @Autowired
    private lateinit var r2dbc: R2dbcService

    @Autowired
    private lateinit var timingRoomDao: TimingRoomDao

    @Autowired
    private lateinit var tradeInfoService: TradeInfoService

    @Autowired
    lateinit var roomRecordService: RoomRecordService

    @Autowired
    private lateinit var roomRecordDao: RoomRecordDao

    @Autowired
    private lateinit var redisUtil: RedisUtil

    @Autowired
    private lateinit var roleService: RoleService

    @Autowired
    private lateinit var quartzManager: QuartzManager
    private val roomEnableMutex = Mutex()   // 房间启用和禁用的互斥锁
    private val roomCreateMutex = Mutex()   // 房间进入和退出的互斥锁

    /**
     * 使能一个房间
     * @param value true：启用一个房间 false:关闭一个房间
     */
    fun enableRoom(roomId: String, value: Boolean, flag: String) = r2dbc.withTransaction {
        val dao = getBaseRoomDao<BaseRoom>(flag)
        val room: BaseRoom = dao.findByRoomId(roomId) ?: throw IllegalStateException("房间号不存在")
        val rest = dao.enableRoomById(roomId, room.isEnable<BaseRoom>(value).enable!!)
        if (rest > 0) {
            var roomRecord = room.toRoomRecord()
            roomEnableMutex.withLock {
                if (value) {
                    roomRecordDao.save(roomRecord)
                    redisUtil.saveRoomRecord(roomRecord)
                } else {
                    roomRecord = redisUtil.getRoomRecord(roomId) ?: roomRecordDao.findLastRecordByRoomId(roomId)
                            ?: error("房间不存在：$roomId")
                    val previousRecord = roomRecordDao.findLastRecordByRoomId(roomRecord.startTime!!, roomId)
                    roomRecord.endTime = LocalDateTime.now()
                    roomRecord.computingTime()
                    roomRecord.closePrice = tradeInfoService.getClosingPriceByRoomId(roomId, roomRecord.startTime!!, roomRecord.endTime!!)
                    if (roomRecord.closePrice == null) {
                        roomRecord.maxPrice = previousRecord?.maxPrice ?: BigDecimal(0)
                        roomRecord.minPrice = previousRecord?.minPrice ?: BigDecimal(0)
                        roomRecord.openPrice = previousRecord?.openPrice ?: BigDecimal(0)
                        roomRecord.closePrice = previousRecord?.closePrice ?: BigDecimal(0)
                    } else {
                        val map = tradeInfoService.getMaxMinPrice(roomId, roomRecord.startTime!!, roomRecord.endTime!!)
                        roomRecord.maxPrice = map["maxPrice"]
                        roomRecord.minPrice = map["minPrice"]
                        roomRecord.openPrice = previousRecord?.closePrice
                                ?: tradeInfoService.getOpenPriceByRoomId(roomRecord.startTime!!, roomRecord.endTime!!, roomId)
                    }
                    r2dbc.dynamicUpdate(roomRecord)
                            .matching(where("id").`is`(roomRecord.id!!))
                            .fetch().awaitRowsUpdated()
                }
                redisUtil.publishRoomEvent(RoomEvent(roomId, value))
            }
        }
        logger.info("房间 {} {} 成功", roomId, if (value) "启动" else "关闭")
    }

    /**
     * 更改房间模式
     * 注意该方法没有检查权限，请调用时检查权限
     */
    suspend fun <T : BaseRoom> changeModel(room: T, oldFlag: String): T {
        val roomId = room.roomId!!
        val oldDao = getBaseRoomDao<T>(oldFlag)
        val newDao = getBaseRoomDao<T>(room.flag)
        roomEnableMutex.withLock {
            val oldRoom = oldDao.findByRoomId(roomId) ?: throw IllegalStateException("房间号不存在: $roomId")
            if (oldRoom.enable == BaseRoom.ENABLE) throw IllegalStateException("房间正在交易，不能切换模式")
            return coroutineScope {
                val one = async { oldDao.deleteById(roomId) }
                val tew = async { newDao.save(room) }
                one.await()
                tew.await()
            }
        }
    }

    /**
     * 通过房间id更新一个房间的配置
     */
    suspend fun <T : BaseRoom> updateRoomByRoomId(room: T, oldFlag: String): T {
        val roomId = room.roomId ?: throw IllegalStateException("请指定房间id")
        if ((room.startTime!!.toSecondOfDay() + room.time!!.toSecondOfDay()) > LocalTime.MAX.toSecondOfDay())
            throw IllegalStateException("时长${room.time}超过今天结束时间：23:59:59.999999999")
        val companyList = roleService.getCompanyList(Stockholder.ADMIN)
        return if (companyList.contains(room.companyId)) {
            if (oldFlag == room.flag) {
                room.roomId = null
                room.enable = null
                val result = r2dbc.dynamicUpdate(room)
                        .matching(where("room_id").`is`(roomId))
                        .fetch().awaitRowsUpdated()
                logger.info("更新结果 {}", result)
                val newRoom = getBaseRoomDao<T>(room.flag).findByRoomId(roomId)!!
                // 修改定时任务开始和结束的时间
                quartzManager.modifyJobTime(RoomStartJobInfo(newRoom))
                quartzManager.modifyJobTime(RoomEndJobInfo(newRoom))
                newRoom
            } else {
                // TODO 判断是否能更改为该模式的房间
                val newRoom = changeModel(room, oldFlag)
                // 修改任务的开始和结束任务名
                quartzManager.modifyJob(RoomStartJobInfo(newRoom), RoomTask.jobStartGroup, roomId)
                quartzManager.modifyJob(RoomEndJobInfo(newRoom), RoomTask.jobEndGroup, roomId)
                newRoom
            }
        } else throw IllegalStateException("没有该房间的修改权限")
    }

    /**
     * 创建房间
     */
    suspend fun <T : BaseRoom> createRoom(room: T): T {
        val company = companyDao.findById(room.companyId!!)
        val dao = getBaseRoomDao<T>(room.flag)
        room.validNull()
        if ((room.startTime!!.toSecondOfDay() + room.time!!.toSecondOfDay()) > LocalTime.MAX.toSecondOfDay())
            throw IllegalStateException("时长${room.time}超过今天结束时间：23:59:59.999999999")
        if (company!!.getModes().contains(room.flag)) {         // 判断房间模式(权限)
            room.roomId = baseRoomService.getNextRoomId(room)   // 获取全局唯一的房间id
            room.isEnable<T>(false)
            roomCreateMutex.withLock {
                if (checkRoomCount(room.companyId!!)) {
                    logger.info(room.toString())
                    addTimingTask(room)
                    return dao.save(room)
                } else throw IllegalStateException("公司房间已满")
            }
        } else throw IllegalStateException("不能创建该模式${room.flag}的房间")
    }

    /**
     * 获取所有能加入的房间
     */
    suspend fun getAllRoomList() = getRoomList()

    /**
     * 获取能编辑的房间，就是自己管理的房间
     */
    suspend fun getEditableRoomList() = getRoomList(Stockholder.ADMIN)

    suspend fun getHomepageData(roomId: String): Map<String, Any> {
        val roomRecord = roomRecordDao.findLastRecordByRoomIdAndEndTime(roomId, LocalDateTime.now())
                ?: return mapOf("minPrice" to 0, "maxPrice" to 0, "closePrice" to 0,
                        "tradesNumber" to 0, "difference" to 0)
        val secondRecord = roomRecordDao.findSecondRecordByRoomId(roomId, LocalDateTime.now())
        val closePrice = if (roomRecord.closePrice == null || roomRecord.closePrice?.compareTo(BigDecimal(0)) == 0) null else roomRecord.closePrice
        val tradesNumber = roomRecordService.countByCompanyId(roomRecord.companyId!!)
        val secondClosingPrice = secondRecord?.closePrice ?: BigDecimal(0)
        return mutableMapOf(
                "closePrice" to (closePrice ?: BigDecimal(0)).toPlainString(),
                "tradesNumber" to tradesNumber,
                "difference" to (closePrice?.subtract(secondClosingPrice)?.divide(closePrice, 4, BigDecimal.ROUND_HALF_EVEN)
                        ?: BigDecimal(0)).toPlainString(),
                "minPrice" to (roomRecord.minPrice ?: BigDecimal(0)).toPlainString(),
                "maxPrice" to (roomRecord.maxPrice ?: BigDecimal(0)).toPlainString()
        )
    }

    /**
     * 查找指定房间的历史订单
     */
    suspend fun findOrder(roomId: String, query: PageQuery): PageView<TradeInfo> {
        return tradeInfoService.findOrder(roomId, query, LocalDateTime.now())
    }

    suspend fun getRoomList(role: String? = null) = coroutineScope {
        val companyList = roleService.getCompanyList(role)
        if (companyList.isEmpty()) throw IllegalStateException("错误：没有绑定公司，没有可用房间")
        // TODO 不支持分页 可以考虑禁止跳页查询
        val clickList = async { clickRoomDao.findByCompanyIdAll(companyList) }
        val bickerList = async { bickerRoomDao.findByCompanyIdAll(companyList) }
        val doubleList = async { doubleRoomDao.findByCompanyIdAll(companyList) }
        val timelyList = async { continueRoomDao.findByCompanyIdAll(companyList) }
        val timingList = async { timingRoomDao.findByCompanyIdAll(companyList) }
        val restList = LinkedList<BaseRoom>()
        clickList.await().toList(restList)
        bickerList.await().toList(restList)
        doubleList.await().toList(restList)
        timelyList.await().toList(restList)
        timingList.await().toList(restList)
        restList
    }

    /**
     * 获取房间报价范围
     */
    suspend fun getRoomScope(roomId: String): Map<String, String> {
        val roomRecord = roomRecordDao.findLastRecordByRoomIdAndEndTime(roomId, LocalDateTime.now())
        var highScope = "0"
        var lowScope = "0"
        if (roomRecord != null) {
            val closePrice = roomRecord.closePrice
            highScope = closePrice?.multiply(BigDecimal(2.0))?.toPlainString() ?: "0"
            lowScope = closePrice?.multiply(BigDecimal(0.5))?.toPlainString() ?: "0"
        }
        return mapOf("highScope" to highScope, "lowScope" to lowScope)
    }

    /**
     * 服务启动的时候调用
     */
    suspend fun loadTimingTask() {
        clickRoomDao.findTimeAll().collect(::addTimingTask)
        bickerRoomDao.findTimeAll().collect(::addTimingTask)
        doubleRoomDao.findTimeAll().collect(::addTimingTask)
        continueRoomDao.findTimeAll().collect(::addTimingTask)
        timingRoomDao.findTimeAll().collect(::addTimingTask)
    }

    suspend fun addTimingTask(room: BaseRoom) {
        if (room.enable == BaseRoom.DISABLED
                && room.startTime!! <= LocalTime.now()
                && (room.startTime!! + room.time!!) > LocalTime.now()) {
            enableRoom(room.roomId!!, true, room.flag).awaitSingle()
        }
        if (room.enable == BaseRoom.ENABLE && room.startTime!! + room.time!! <= LocalTime.now()) {
            enableRoom(room.roomId!!, false, room.flag).awaitSingle()
        }
        quartzManager.addJob(RoomStartJobInfo(room))
        quartzManager.addJob(RoomEndJobInfo(room))
    }

    /**
     * 检查房间数量，异步同时获取四种房间的数量
     */
    suspend fun checkRoomCount(companyId: Int): Boolean = coroutineScope {
        val company = async { companyDao.findById(companyId) }
        val countClick = async { clickRoomDao.countByCompanyId(companyId) }
        val bickerClick = async { bickerRoomDao.countByCompanyId(companyId) }
        val countDouble = async { doubleRoomDao.countByCompanyId(companyId) }
        val countTimely = async { continueRoomDao.countByCompanyId(companyId) }
        val countTiming = async { timingRoomDao.countByCompanyId(companyId) }
        company.await() ?: throw IllegalStateException("公司不存在：$companyId")
        company.await()?.roomCount!! > (countClick.await() + bickerClick.await() + countDouble.await() + countTimely.await() + countTiming.await())
    }

    /**
     * 获取总的房间数量
     */
    suspend fun getAllRoomCount(): Long = coroutineScope {
        val countClick = async { clickRoomDao.count() }
        val countBicker = async { bickerRoomDao.count() }
        val countDouble = async { doubleRoomDao.count() }
        val countTimely = async { continueRoomDao.count() }
        val countTiming = async { timingRoomDao.count() }
        countClick.await() + countBicker.await() + countDouble.await() + countTimely.await() + countTiming.await()
    }

    /**
     * 通过房间号号获取对应的dao
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : BaseRoom> getBaseRoomDao(flag: String): BaseRoomDao<T, String> {
        return when (flag) {
            RoomEnum.CLICK.mode -> clickRoomDao as BaseRoomDao<T, String>
            RoomEnum.BICKER.mode -> bickerRoomDao as BaseRoomDao<T, String>
            RoomEnum.DOUBLE.mode -> doubleRoomDao as BaseRoomDao<T, String>
            RoomEnum.CONTINUE.mode -> continueRoomDao as BaseRoomDao<T, String>
            RoomEnum.TIMING.mode -> timingRoomDao as BaseRoomDao<T, String>
            else -> throw IllegalStateException("不支持的房间号")
        }
    }

    fun <T : BaseRoom> getBaseRoomDao(room: T): BaseRoomDao<T, String> = getBaseRoomDao(room.flag)

}
