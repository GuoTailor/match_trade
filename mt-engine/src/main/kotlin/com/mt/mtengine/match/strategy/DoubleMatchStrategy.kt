package com.mt.mtengine.match.strategy

import com.mt.mtcommon.*
import com.mt.mtengine.match.MatchUtil
import com.mt.mtengine.match.MatchUtil.contain
import com.mt.mtengine.service.MatchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.scheduler.Schedulers
import java.time.LocalDate
import java.time.LocalDateTime
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
    override fun match(roomInfo: DoubleRoomInfo): Boolean {
        val isMatch = roomInfo.orderList.size >= 2
        var i = 0
        while (roomInfo.orderList.size >= 2) {
            val order1 = roomInfo.orderList.pollLast()!!
            val order2 = roomInfo.orderList.pollLast()!!
            val result = if (MatchUtil.verify(order1, order2) && order1.price != order2.price) {
                if (order1.price!! > order2.price) {
                    order1.isBuy = true
                    order2.isBuy = false
                    matchService.onMatchSuccess(roomInfo, order1, order2, i++ == 0)
                } else {
                    order1.isBuy = false
                    order2.isBuy = true
                    matchService.onMatchSuccess(roomInfo, order2, order1, i++ == 0)
                }
            } else {
                matchService.onMatchError(roomInfo, order1, order2, "失败:" + MatchUtil.getVerifyInfo(order1, order2), i++ == 0)
            }
            result.subscribeOn(Schedulers.elastic()).subscribe()    // 弹性线程池可能会创建大量线程，但对I/O密集型的任务来说很友好
        }
        return isMatch
    }

    class DoubleRoomInfo(record: RoomRecord) :
            MatchStrategy.RoomInfo(record.roomId!!, record.mode!!, record.cycle!!.toMillisOfDay(), record.endTime
                    ?: LocalTime.MAX.toLocalDateTime()) {
        private var nextCycleTime = System.currentTimeMillis() + cycle
        val orderList = TreeSet(MatchUtil.sortTime)

        override fun canStart(): Boolean {
            return System.currentTimeMillis() >= nextCycleTime && LocalDateTime.now() < endTime
        }

        override fun isEnd() = LocalDateTime.now() >= endTime

        override fun setNextCycle() {
            nextCycleTime += cycle      // TODO 可能最后一次撮合不会执行
        }

        override fun addOrder(data: OrderParam): Boolean {
            return if (!orderList.contain(data)) {
                orderList.add(data)
            } else false
        }

        override fun cancelOrder(order: CancelOrder): Boolean {
            return orderList.removeIf { it.userId == order.userId }
        }

        override fun addRival(rival: RivalInfo): Boolean = false
        override fun updateTopThree(data: OrderParam): Boolean = false
        override fun updateTopThree(order: CancelOrder): Boolean = false
        override fun updateTopThree(): Boolean = true
    }

    override fun createRoomInfo(record: RoomRecord): DoubleRoomInfo {
        return DoubleRoomInfo(record)
    }

}