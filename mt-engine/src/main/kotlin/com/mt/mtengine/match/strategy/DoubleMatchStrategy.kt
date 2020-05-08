package com.mt.mtengine.match.strategy

import com.mt.mtcommon.*
import com.mt.mtengine.match.MatchUtil
import com.mt.mtengine.service.MatchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.scheduler.Schedulers
import java.time.LocalTime
import java.util.*

/**
 * Created by gyh on 2020/4/30.
 * 两两成交
 */
@Component
class DoubleMatchStrategy : MatchStrategy<DoubleMatchStrategy.DoubleRoomInfo>() {
    override val roomType = RoomEnum.DOUBLE

    @Autowired
    private lateinit var matchService: MatchService

    /**
     * 两两成交
     * 用户不选择买卖方向，直接报价
     * 将用户报价按时间先后排序
     * 一秒钟一次，报价高者为买方，报价低者为卖方，相邻两笔成交，报价相同则该两笔报价作废
     */
    override fun match(roomInfo: DoubleRoomInfo) {
        while (roomInfo.orderList.size >= 2) {
            val order1 = roomInfo.orderList.pollLast()!!
            val order2 = roomInfo.orderList.pollLast()!!
            val result = if (MatchUtil.verify(order1, order2) && order1.price != order2.price) {
                if (order1.price!! > order2.price) {
                    matchService.onMatchSuccess(roomInfo.roomId, roomInfo.flag, order1, order2)
                } else {
                    matchService.onMatchSuccess(roomInfo.roomId, roomInfo.flag, order2, order1)
                }
            } else {
                matchService.onMatchError(order1, order2, "失败:" + MatchUtil.getVerifyInfo(order1, order2))
            }
            result.subscribeOn(Schedulers.elastic()).subscribe()    // 弹性线程池可能会创建大量线程，但对I/O密集型的任务来说很友好
        }
        // 未成交的订单保留直下一轮
    }

    class DoubleRoomInfo(record: RoomRecord) :
            MatchStrategy.RoomInfo(record.roomId!!, record.model!!, record.cycle!!.toMillisOfDay(), record.endTime
                    ?: LocalTime.MAX.toDate()) {
        private var nextCycleTime = System.currentTimeMillis() + cycle
        val orderList = TreeSet(MatchUtil.sortTime)

        override fun canStart(): Boolean {
            return System.currentTimeMillis() >= nextCycleTime && System.currentTimeMillis() < endTime.time
        }

        override fun isEnd() = System.currentTimeMillis() >= endTime.time

        override fun setNextCycle() {
            nextCycleTime += cycle
        }

        override fun add(data: Any): Boolean {
            return if (data is OrderParam && !orderList.contains(data)) {
                orderList.add(data)
            } else if (data is CancelOrder) {
                orderList.removeIf { it.userId == data.userId }
            } else false
        }

    }

    override fun createRoomInfo(record: RoomRecord): DoubleRoomInfo {
        return DoubleRoomInfo(record)
    }

}