package com.mt.mtengine.match.strategy

import com.mt.mtcommon.*
import com.mt.mtengine.mq.MatchSink
import com.mt.mtengine.service.RedisUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import reactor.core.scheduler.Schedulers
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.LockSupport

/**
 * Created by gyh on 2020/5/2.
 */
abstract class MatchStrategy<T : MatchStrategy.RoomInfo> {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    /** 单位，纳秒 */
    private val packTime = 1L
    private val roomMap = ConcurrentHashMap<String, T>() // key：房间号；value：RoomInfo
    private val matchWork: MatchTask<T> = MatchTask(packTime)

    /** 标识房间类型 */
    abstract val roomType: RoomEnum

    @Autowired
    lateinit var sink: MatchSink

    @Autowired
    lateinit var redisUtil: RedisUtil

    fun start() = matchWork.start(this)

    fun isCan(flag: String) = RoomEnum.getRoomEnum(flag) == roomType

    private fun startMatch(roomId: String) {
        val roomInfo = roomMap[roomId]
        if (roomInfo == null) {
            logger.error("交易匹配错误，不存在的房间号: {}", roomId)
        } else {
            if (match(roomInfo) && roomInfo.updateTopThree()) {
                // 更新前三档报价
                sink.outResult().send(MessageBuilder.withPayload(roomInfo.topThree.toNotifyResult()).build())
                redisUtil.setRoomTopThree(roomInfo.topThree)
            }
        }
    }

    fun tryAddOrder(order: OrderParam): Boolean {
        var roomInfo = roomMap[order.roomId]
        if (roomInfo == null) {
            synchronized(this) {
                if (roomInfo == null) {
                    // 房间只有第一次创建才会被锁
                    val roomRecord = redisUtil.getRoomRecord(order.roomId!!).block()
                    if (roomRecord == null) {
                        logger.error("交易订单添加错误，不存在的房间号: {}，或房间没开启", order.roomId)
                        return false
                    } else {
                        roomInfo = createRoomInfo(roomRecord)
                        roomMap[order.roomId!!] = roomInfo!!
                    }
                }
            }
        }
        roomInfo!!.tryAddOrder(order, packTime)
        return true
    }

    fun tryCancelOrder(order: CancelOrder): Boolean {
        var roomInfo = roomMap[order.roomId]
        if (roomInfo == null) {
            synchronized(this) {
                if (roomInfo == null) {
                    // 房间只有第一次创建才会被锁
                    val roomRecord = redisUtil.getRoomRecord(order.roomId!!).block()
                    if (roomRecord == null) {
                        logger.error("交易订单撤销错误，不存在的房间号: {}，或房间没开启", order.roomId)
                        return false
                    } else {
                        roomInfo = createRoomInfo(roomRecord)
                        roomMap[order.roomId!!] = roomInfo!!
                    }
                }
            }
        }
        roomInfo!!.tryCancelOrder(order, packTime)
        return true
    }

    fun tryAddRival(rival: RivalInfo): Boolean {
        var roomInfo = roomMap[rival.roomId]
        if (roomInfo == null) {
            synchronized(this) {
                if (roomInfo == null) {
                    // 房间只有第一次创建才会被锁
                    val roomRecord = redisUtil.getRoomRecord(rival.roomId!!).block()
                    if (roomRecord == null) {
                        logger.error("交易对手添加错误，不存在的房间号: {}，或房间没开启", rival.roomId)
                        return false
                    } else {
                        roomInfo = createRoomInfo(roomRecord)
                        roomMap[rival.roomId!!] = roomInfo!!
                    }
                }
            }
        }
        roomInfo!!.tryAddRival(rival, packTime)
        return true
    }

    // TODO 房间提前/延后结束通知

    /**
     * 开始撮合,返回true代表发生了撮合
     */
    abstract fun match(roomInfo: T): Boolean

    abstract fun createRoomInfo(record: RoomRecord): T

    abstract class RoomInfo(
            val roomId: String, // 房间号
            val flag: String,   // 房间模式
            var cycle: Long,    // 周期，单位毫秒
            val endTime: Date   // 不支持提前结束和延迟结束
    ) {
        val topThree = TopThree(roomId)
        private val tempAdd = AtomicReference<Any>()

        /** 判断是否可以开始撮合 */
        abstract fun canStart(): Boolean

        /** 判断房间是否结束 */
        abstract fun isEnd(): Boolean

        /*** 设置下一次的撮合周期，改方法只会在撮合后调用 */
        abstract fun setNextCycle()

        /**
         * [tryAddOrder]是生产者线程，会有多线程竞争，[addData]是消费者线程调用，永远只有一个消费者，不存在线程竞争
         */
        fun tryAddOrder(order: OrderParam, packTime: Long) {
            while (!tempAdd.compareAndSet(null, order)) {
                LockSupport.parkNanos(packTime)
            }
        }

        fun tryAddRival(rival: RivalInfo, packTime: Long) {
            while (!tempAdd.compareAndSet(null, rival)) {
                LockSupport.parkNanos(packTime)
            }
        }

        fun tryCancelOrder(rival: CancelOrder, packTime: Long) {
            while (!tempAdd.compareAndSet(null, rival)) {
                LockSupport.parkNanos(packTime)
            }
        }

        internal fun addData(redisUtil: RedisUtil, sink: MatchSink): Boolean {
            val data = tempAdd.getAndSet(null)
            return if (data != null) {
                if (data is OrderParam && addOrder(data)) {
                    redisUtil.putUserOrder(data, endTime).subscribeOn(Schedulers.elastic()).subscribe()
                    sink.outResult().send(MessageBuilder.withPayload(data.toNotifyResult(true)).build())
                    if (updateTopThree(data)) {
                        sink.outResult().send(MessageBuilder.withPayload(data.toTopThreeNotify(topThree)).build())
                        redisUtil.setRoomTopThree(topThree)
                    }
                    true
                } else if (data is CancelOrder && cancelOrder(data)) {
                    redisUtil.deleteUserOrder(data).subscribeOn(Schedulers.elastic()).subscribe()
                    sink.outResult().send(MessageBuilder.withPayload(data.toNotifyResult(true)).build())
                    if (updateTopThree(data)) {
                        sink.outResult().send(MessageBuilder.withPayload(data.toTopThreeNotify(topThree)).build())
                        redisUtil.setRoomTopThree(topThree)
                    }
                    true
                } else if (data is RivalInfo && addRival(data)) {
                    redisUtil.putUserRival(data, endTime).subscribeOn(Schedulers.elastic()).subscribe()
                    sink.outResult().send(MessageBuilder.withPayload(data.toNotifyResult(true)).build())
                    true
                } else false
            } else false
        }

        /**
         * 当放回true时代表添加成功
         */
        abstract fun addOrder(data: OrderParam): Boolean

        /**
         * 当放回true时代表撤单成功
         */
        abstract fun cancelOrder(order: CancelOrder): Boolean

        /**
         * 当放回true时代表添加成功
         */
        abstract fun addRival(rival: RivalInfo): Boolean

        /**
         * 添加元素时更新前三名
         */
        abstract fun updateTopThree(data: OrderParam): Boolean

        /**
         * 撤单时更新前三名
         */
        abstract fun updateTopThree(order: CancelOrder): Boolean

        /**
         * 发送有效撮合后更新前三名
         */
        abstract fun updateTopThree(): Boolean
    }

    /**
     * 每个撮合模式单独配置一个线程，也就是说每个戳和模式不管又多少个房间都用一个线程来调度撮合，
     * 这样做的好处是单线程避免线程的竞争，已达到高效撮合。这里面最耗时的是[match]方法，
     * 所以在[match]方法的具体实现中使用了Flux的响应式特性，让访问数据库等I/O操作在Flux的共享线程池里完成，
     * 以让[match]方法创建好订单后快速放回，这样就避免了在这个[MatchTask]线程中做耗时操作
     */
    private class MatchTask<T : RoomInfo>(val packTime: Long) : Thread() {
        private var count = 0L
        private var localPackTime = packTime
        private var strategy: MatchStrategy<T>? = null

        fun start(strategy: MatchStrategy<T>) {
            this.strategy = strategy
            name = "matchTask-" + strategy.roomType.flag
            super.start()
        }

        override fun run() {
            while (true) {
                count = 0   // 计数清零
                strategy!!.roomMap.forEach { k, v ->
                    // 单线程很快，因为撮合在reactor的享线程池里完成
                    try {
                        if (v.canStart()) {
                            strategy?.startMatch(k)
                            v.setNextCycle()
                            count++
                        }
                        if (v.addData(strategy!!.redisUtil, strategy!!.sink)) {
                            count++
                        }
                        if (v.isEnd()) {
                            strategy!!.roomMap.remove(k)
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
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