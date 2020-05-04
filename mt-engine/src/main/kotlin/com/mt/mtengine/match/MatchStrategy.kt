package com.mt.mtengine.match

import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.RivalInfo
import com.mt.mtcommon.RoomEnum
import com.mt.mtcommon.toMillisOfDay
import com.mt.mtengine.logger
import com.mt.mtengine.service.RedisUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.LockSupport

/**
 * Created by gyh on 2020/5/2.
 */
abstract class MatchStrategy {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /** 单位，纳秒 */
    private val packTime = 1L
    private val roomMap = ConcurrentHashMap<String, RoomInfo>() // key：房间号；value：RoomInfo
    private val matchWork: MatchTask = MatchTask(packTime)

    /** 标识房间类型 */
    abstract val roomType: RoomEnum
    @Autowired
    lateinit var redisUtil: RedisUtil

    fun start() = matchWork.start(this)

    fun isCanAdd(roomId: String?) = RoomEnum.getRoomModel(roomId ?: "") == roomType

    private fun startMatch(roomId: String) {
        val roomInfo = roomMap[roomId]
        if (roomInfo == null) {
            logger.error("交易匹配错误，不存在的房间号: {}", roomId)
        } else {
            match(roomInfo)
        }
    }

    fun tryAddOrder(order: OrderParam) {
        var roomInfo = roomMap[order.roomId]
        if (roomInfo == null) { // TODO 存在安全隐患，可以考虑双检锁，房间只有第一次创建才会被锁
            val roomRecord = redisUtil.getRoomRecord(order.roomId!!).block()
            if (roomRecord == null) {
                logger.error("交易订单添加错误，不存在的房间号: {}，或房间没开启", order.roomId)
                return
            } else {
                roomInfo = RoomInfo(order.roomId!!, roomRecord.cycle!!.toMillisOfDay())
                roomMap[order.roomId!!] = roomInfo  // TODO 存在并发修改隐患
            }
        }
        roomInfo.tryAddOrder(order, packTime)
    }

    fun tryAddRival(rival: RivalInfo) {
        var roomInfo = roomMap[rival.roomId]
        if (roomInfo == null) {
            val roomRecord = redisUtil.getRoomRecord(rival.roomId!!).block()
            if (roomRecord == null) {
                logger.error("交易对手添加错误，不存在的房间号: {}，或房间没开启", rival.roomId)
                return
            } else {
                roomInfo = RoomInfo(rival.roomId!!, roomRecord.cycle!!.toMillisOfDay())
                roomMap[rival.roomId!!] = roomInfo
            }
        }
        roomInfo.tryAddRival(rival, packTime)
    }

    /**
     * 更新房间的周期
     * @param secondCycle 单位豪秒
     */
    // TODO 添加修改周期的通知
    fun updateCycle(roomId: String, secondCycle: Long) {
        val roomInfo = roomMap[roomId]
        if (roomInfo == null) {
            logger.error("更新房间的周期错误，不存在的房间号: {}，或房间未开启", roomId)
        } else {
            roomInfo.updateCycle(secondCycle)
        }
    }

    /**
     * 开始撮合
     */
    abstract fun match(roomInfo: RoomInfo)

    /**
     * 每个撮合模式单独配置一个线程，也就是说每个戳和模式不管又多少个房间都用一个线程来调度撮合，
     * 这样做的好处是单线程避免线程的竞争，已达到高效撮合。这里面最耗时的是[match]方法，
     * 所以在[match]方法的具体实现中使用了Flux的响应式特性，让访问数据库等I/O操作在Flux的共享线程池里完成，
     * 以让[match]方法创建好订单后快速放回，这样就避免了在这个[MatchTask]线程中做耗时操作
     */
    private class MatchTask(val packTime: Long) : Thread() {
        private var count = 0L
        private var localPackTime = packTime
        private var strategy: MatchStrategy? = null

        fun start(strategy: MatchStrategy) {
            this.strategy = strategy
            super.start()
        }

        override fun run() {
            while (true) {
                count = 0   // 计数清零
                strategy!!.roomMap.forEach { k, v ->
                    if (v.isStart()) {
                        strategy?.startMatch(k)
                        v.setNextCycle()
                        count++
                    }
                    if (v.add()) {
                        count++
                    }
                }
                if (count == 0L) {  // 如果计数为0，说明没事可做，休眠一段时间以让出cpu
                    // 休眠一纳秒在64位linux中耗时大概60微妙
                    // see https://hazelcast.com/blog/locksupport-parknanos-under-the-hood-and-the-curious-case-of-parking/
                    // 如果想让戳和延时更低，可以替换为Thread.yield()，这样在我windows系统中会占用我30%的cpu，当5个撮合模式同时开启时可能会占用100%，这个我没测试
                    LockSupport.parkNanos(localPackTime)
                    if (localPackTime < 1_000000L) {        // 最长休眠1毫秒
                        localPackTime++
                    }
                } else {            // 否则不休眠，尽量压榨cpu
                    localPackTime = packTime
                }
            }
        }
    }

}