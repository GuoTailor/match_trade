package com.mt.mtuser.service.room

import com.mt.mtcommon.RoomEnum
import com.mt.mtcommon.RoomEvent
import com.mt.mtcommon.TradeInfo
import com.mt.mtcommon.exception.BusinessException
import com.mt.mtcommon.plus
import com.mt.mtuser.dao.CompanyDao
import com.mt.mtuser.dao.RoomRecordDao
import com.mt.mtuser.dao.room.*
import com.mt.mtuser.entity.Stockholder
import com.mt.mtuser.entity.page.PageQuery
import com.mt.mtuser.entity.page.PageView
import com.mt.mtuser.entity.room.BaseRoom
import com.mt.mtuser.schedule.QuartzManager
import com.mt.mtuser.schedule.RoomEndJobInfo
import com.mt.mtuser.schedule.RoomStartJobInfo
import com.mt.mtuser.schedule.RoomTask
import com.mt.mtuser.service.*
import com.mt.mtuser.service.kline.Compute1DKlineService
import com.mt.mtuser.service.kline.KlineService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Created by gyh on 2020/3/23.
 */
@Service
class RoomService {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

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

    @Autowired
    private lateinit var klineService: KlineService

    @Autowired
    private lateinit var compute1DKlineService: Compute1DKlineService
    private val roomEnableMutex = Mutex()   // 房间启用和禁用的互斥锁
    private val roomCreateMutex = Mutex()   // 房间进入和退出的互斥锁

    /**
     * 使能一个房间
     * @param value true：启用一个房间 false:关闭一个房间
     */
    @Transactional(rollbackFor = [Exception::class])
    suspend fun enableRoom(roomId: String, value: Boolean, flag: String) {
        val dao = getBaseRoomDao<BaseRoom>(flag)
        val room: BaseRoom = dao.findByRoomId(roomId) ?: throw BusinessException("房间号不存在")
        val enable = companyDao.findEnableById(room.companyId!!)
        if (enable == "0") {
            logger.info("房间{}已禁用", room.companyId)
            return
        }
        val rest = dao.enableRoomById(roomId, room.setEnable<BaseRoom>(value).enable!!)
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
                    roomRecord.closePrice =
                        tradeInfoService.getClosingPriceByRoomId(roomId, roomRecord.startTime!!, roomRecord.endTime!!)
                    if (roomRecord.closePrice == null) {
                        roomRecord.maxPrice = previousRecord?.maxPrice ?: BigDecimal.ZERO
                        roomRecord.minPrice = previousRecord?.minPrice ?: BigDecimal.ZERO
                        roomRecord.openPrice = previousRecord?.openPrice ?: BigDecimal.ZERO
                        roomRecord.closePrice = previousRecord?.closePrice ?: BigDecimal.ZERO
                    } else {
                        val map = tradeInfoService.getMaxMinPrice(roomId, roomRecord.startTime!!, roomRecord.endTime!!)
                        roomRecord.maxPrice = map["maxPrice"]
                        roomRecord.minPrice = map["minPrice"]
                        roomRecord.openPrice = previousRecord?.closePrice
                            ?: tradeInfoService.getOpenPriceByRoomId(
                                roomRecord.startTime!!,
                                roomRecord.endTime!!,
                                roomId
                            )
                    }
                    r2dbc.dynamicUpdate(roomRecord)
                        .awaitSingle()
                    compute1DKlineService.computeCurrent(roomRecord.stockId!!, roomRecord.companyId!!)
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
            val oldRoom = oldDao.findByRoomId(roomId) ?: throw BusinessException("房间号不存在: $roomId")
            if (oldRoom.enable == BaseRoom.ENABLE) throw BusinessException("房间正在交易，不能切换模式")
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
        val roomId = room.roomId ?: throw BusinessException("请指定房间id")
        if ((room.startTime!!.toSecondOfDay() + room.time!!.toSecondOfDay()) > LocalTime.MAX.toSecondOfDay())
            throw BusinessException("时长${room.time}超过今天结束时间：23:59:59.999999999")
        val enable = companyDao.findEnableById(room.companyId!!)
        if (enable == "0") error("房间${room.companyId}已禁用")
        val companyList = roleService.getCompanyList(Stockholder.ADMIN)
        return if (companyList.contains(room.companyId!!)) {
            if (oldFlag == room.flag) {
                room.enable = null
                val result = r2dbc.dynamicUpdate(room)
                    .awaitSingle()
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
        } else throw BusinessException("没有该房间的修改权限")
    }

    /**
     * 创建房间
     */
    @Transactional(rollbackFor = [Exception::class])
    suspend fun <T : BaseRoom> createRoom(room: T): T {
        val company = companyDao.findById(room.companyId!!)
        val dao = getBaseRoomDao<T>(room.flag)
        room.validNull()
        if ((room.startTime!!.toSecondOfDay() + room.time!!.toSecondOfDay()) > LocalTime.MAX.toSecondOfDay())
            throw BusinessException("时长${room.time}超过今天结束时间：23:59:59.999999999")
        if (company!!.getModes().contains(room.flag)) {         // 判断房间模式(权限)
            room.roomId = baseRoomService.getNextRoomId(room)   // 获取全局唯一的房间id
            room.setEnable<T>(false)
            roomCreateMutex.withLock {
                if (checkRoomCount(room.companyId!!)) {
                    logger.info(room.toString())
                    addTimingTask(room)
                    return dao.save(room)
                } else throw BusinessException("公司房间已满")
            }
        } else throw BusinessException("不能创建该模式${room.flag}的房间")
    }

    suspend fun deleteRoom(roomId: String, flag: String) {
        val dao = getBaseRoomDao<BaseRoom>(flag)
        dao.deleteById(roomId)
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
            ?: return mapOf(
                "minPrice" to 0, "maxPrice" to 0, "closePrice" to 0,
                "tradesNumber" to 0, "difference" to 0
            )
        val secondRecord = roomRecordDao.findSecondRecordByRoomId(roomId, LocalDateTime.now())
        val closePrice =
            if (roomRecord.closePrice == null || roomRecord.closePrice?.compareTo(BigDecimal(0)) == 0) null else roomRecord.closePrice
        val tradesNumber = roomRecordService.countByCompanyId(roomRecord.companyId!!)
        val secondClosingPrice = secondRecord?.closePrice ?: BigDecimal(0)
        return mutableMapOf(
            "closePrice" to (closePrice ?: BigDecimal(0)).toPlainString(),
            "tradesNumber" to tradesNumber,
            "difference" to (closePrice?.subtract(secondClosingPrice)?.divide(closePrice, 4,  RoundingMode.HALF_EVEN )
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
        if (companyList.isEmpty()) throw BusinessException("错误：没有绑定公司，没有可用房间")
        // TODO 不支持分页 可以考虑禁止跳页查询
        getRoomByCompanyId(companyList)
    }

    suspend fun getRoomByCompanyId(companyList: Iterable<Int>) = coroutineScope {
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
    @Transactional(rollbackFor = [Exception::class])
    suspend fun getRoomScope(roomId: String): Map<String, String> {
        val roomRecord = roomRecordDao.findLastRecordByRoomIdAndEndTime(roomId, LocalDateTime.now())
        var highScope = "0"
        var lowScope = "0"
        if (roomRecord != null) {
            val closePrice = klineService.getClosePriceByTableName(
                LocalDate.now().atStartOfDay(),
                roomRecord.stockId!!,
                "mt_1d_kline"
            )
            highScope = closePrice.multiply(BigDecimal("1.5")).toPlainString()
            lowScope = closePrice.multiply(BigDecimal("0.5")).toPlainString()
        }
        return mapOf("highScope" to highScope, "lowScope" to lowScope)
    }

    /**
     * 服务启动的时候调用
     */
    @Transactional(rollbackFor = [Exception::class])
    suspend fun loadTimingTask() {
        clickRoomDao.findTimeAll().collect(::addTimingTask)
        bickerRoomDao.findTimeAll().collect(::addTimingTask)
        doubleRoomDao.findTimeAll().collect(::addTimingTask)
        continueRoomDao.findTimeAll().collect(::addTimingTask)
        timingRoomDao.findTimeAll().collect(::addTimingTask)
    }

    @Transactional(rollbackFor = [Exception::class])
    suspend fun addTimingTask(room: BaseRoom) {
        if (room.enable == BaseRoom.DISABLED
            && room.startTime!! <= LocalTime.now()
            && (room.startTime!! + room.time!!) > LocalTime.now()
        ) {
            enableRoom(room.roomId!!, true, room.flag)
        }
        if (room.enable == BaseRoom.ENABLE && room.startTime!! + room.time!! <= LocalTime.now()) {
            enableRoom(room.roomId!!, false, room.flag)
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
        company.await() ?: throw BusinessException("公司不存在：$companyId")
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
            else -> throw BusinessException("不支持的房间号")
        }
    }

    fun <T : BaseRoom> getBaseRoomDao(room: T): BaseRoomDao<T, String> = getBaseRoomDao(room.flag)

}
