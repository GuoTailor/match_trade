package com.mt.mtengine.match.strategy

import com.mt.mtcommon.*
import com.mt.mtengine.match.MatchUtil
import com.mt.mtengine.match.MatchUtil.contain
import com.mt.mtengine.service.MatchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import reactor.core.scheduler.Schedulers
import java.time.LocalTime
import java.util.*

/**
 * Created by gyh on 2020/5/4.
 * 抬杠交易
 */
@Component
class BickerMatchStrategy : MatchStrategy<BickerMatchStrategy.BickerRoomInfo>() {
    override val roomType = RoomEnum.BICKER

    @Autowired
    private lateinit var matchService: MatchService

    /**
     * 抬杠交易
     * 不选择交易身份，时间一到就开始撮合
     * 报价从高到低依次排列
     * 最高和最低撮合，成交价取平均
     * 报价人数是奇数舍去中间的报价
     */
    override fun match(roomInfo: BickerRoomInfo): Boolean {
        // 抬杠撮合报价没有身份，全部存在buy队列里面
        val isMatch = roomInfo.orderList.size >= 2
        while (roomInfo.orderList.size >= 2) {
            val buyOrder = roomInfo.orderList.pollLast()!!    // 报价最高的为买家
            val sellOrder = roomInfo.orderList.pollFirst()!!
            buyOrder.isBuy = true
            sellOrder.isBuy = false
            if (MatchUtil.verify(buyOrder, sellOrder) && buyOrder.price != sellOrder.price) {
                matchService.onMatchSuccess(roomInfo, buyOrder, sellOrder)
                        .subscribeOn(Schedulers.boundedElastic()).subscribe()    // 弹性线程池可能会创建大量线程
            } else {
                matchService.onMatchError(roomInfo, buyOrder, sellOrder,
                        "失败:" + MatchUtil.getVerifyInfo(buyOrder, sellOrder))
                        .subscribeOn(Schedulers.boundedElastic()).subscribe()
            }
        }
        roomInfo.orderList.forEach {
            matchService.onMatchError(roomInfo, it, null, "失败: 没有可以匹配的报价")
                    .subscribeOn(Schedulers.boundedElastic()).subscribe()
        }
        roomInfo.orderList.clear()
        return isMatch
    }

    class BickerRoomInfo(record: RoomRecord) :
            MatchStrategy.RoomInfo(record.roomId!!, record.mode!!, record.endTime!!.toEpochMilli(), record.endTime
                    ?: LocalTime.MAX.toLocalDateTime()) {
        val orderList = TreeSet(MatchUtil.sortPriceAndTime)
        private var count = 0

        override fun canStart(): Boolean {
            return System.currentTimeMillis() >= cycle && count == 0
        }

        override fun isEnd() = count > 0

        /**
         * 抬杠撮合只撮合一次
         */
        override fun setNextCycle() {
            count++
        }

        override fun addOrder(data: OrderParam): Boolean {
            return if (!orderList.contain(data)) {
                orderList.add(data)
            } else false
        }

        override fun cancelOrder(order: CancelOrder) = orderList.removeIf { it.userId == order.userId }

        override fun addRival(rival: RivalInfo): Boolean = false
        override fun updateTopThree(data: OrderParam): Boolean = false
        override fun updateTopThree(order: CancelOrder): Boolean = false
        override fun updateTopThree(): Boolean = false
    }

    override fun createRoomInfo(record: RoomRecord): BickerRoomInfo {
        return BickerRoomInfo(record)
    }

}