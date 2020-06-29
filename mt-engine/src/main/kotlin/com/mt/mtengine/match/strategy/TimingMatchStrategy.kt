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
 * 定时撮合
 */
@Component
class TimingMatchStrategy : MatchStrategy<TimingMatchStrategy.TimingRoomInfo>() {
    override val roomType = RoomEnum.TIMING

    @Autowired
    private lateinit var matchService: MatchService

    /**
     * 定时撮合
     * 价格优先、时间优先.
     * 由买价第一档开始向卖价第一档撮合.
     * 报价相同作废
     * 只撮合一次
     */
    override fun match(roomInfo: TimingRoomInfo): Boolean {
        val isMatch = roomInfo.buyOrderList.size >= 1 && roomInfo.sellOrderList.size >= 1
        while (roomInfo.buyOrderList.size >= 1 && roomInfo.sellOrderList.size >= 1) {
            val buyOrder = roomInfo.buyOrderList.pollLast()!!
            val sellOrder = roomInfo.sellOrderList.pollFirst()!!
            if (MatchUtil.verify(buyOrder, sellOrder) && buyOrder.price!! >= sellOrder.price) {
                matchService.onMatchSuccess(roomInfo, buyOrder, sellOrder)
                        .subscribeOn(Schedulers.elastic()).subscribe()
            } else {
                matchService.onMatchError(roomInfo, buyOrder, sellOrder,
                        "失败:" + MatchUtil.getVerifyInfo(buyOrder, sellOrder))
                        .subscribeOn(Schedulers.elastic()).subscribe()
            }
        }
        roomInfo.buyOrderList.forEach {
            matchService.onMatchError(roomInfo, it, null, "失败:" + MatchUtil.getVerifyInfo(it, null))
                    .subscribeOn(Schedulers.elastic()).subscribe()
        }
        roomInfo.sellOrderList.forEach {
            matchService.onMatchError(roomInfo, null, it, "失败:" + MatchUtil.getVerifyInfo(null, it))
                    .subscribeOn(Schedulers.elastic()).subscribe()
        }
        return isMatch
    }

    class TimingRoomInfo(record: RoomRecord) :
            MatchStrategy.RoomInfo(record.roomId!!, record.mode!!, record.endTime!!.toEpochMilli(), record.endTime
                    ?: LocalTime.MAX.toLocalDateTime()) {
        private var nextCycleTime = System.currentTimeMillis() + cycle
        val buyOrderList = TreeSet(MatchUtil.sortPriceAndTime)
        val sellOrderList = TreeSet(MatchUtil.sortPriceAndTime)
        private var count = 0

        override fun canStart(): Boolean {
            return System.currentTimeMillis() >= cycle && count == 0
        }

        override fun isEnd() = count > 0

        override fun setNextCycle() {
            count++
        }

        override fun addOrder(data: OrderParam): Boolean {
            return if (!buyOrderList.contain(data) && !sellOrderList.contain(data) && data.isBuy != null) {
                if (data.isBuy!!) {
                    buyOrderList.add(data)
                } else {
                    sellOrderList.add(data)
                }
            } else false
        }

        override fun cancelOrder(order: CancelOrder): Boolean {
            return if (nextCycleTime >= System.currentTimeMillis() + 60_000) {  // 结束前一分钟不允许撤单
                buyOrderList.removeIf { it.userId == order.userId } || sellOrderList.removeIf { it.userId == order.userId }
            } else false
        }

        override fun addRival(rival: RivalInfo): Boolean = false

        /**
         * 添加元素时更新前三名
         */
        override fun updateTopThree(data: OrderParam): Boolean {
            return if (data.isBuy!!) {
                if (topThree.buyTopThree.size >= 3) {
                    topThree.buyTopThree.sort()
                    topThree.buyTopThree.removeAt(2)
                }
                topThree.buyTopThree.add(data.toOrderInfo())
            } else {
                if (topThree.sellTopThree.size >= 3) {
                    topThree.sellTopThree.sort()
                    topThree.sellTopThree.removeAt(2)
                }
                topThree.sellTopThree.add(data.toOrderInfo())
            }
        }

        /**
         * 撤单时更新前三名
         */
        override fun updateTopThree(order: CancelOrder): Boolean {
            val isRemove = topThree.buyTopThree.removeIf { it.userId == order.userId }
                    || topThree.sellTopThree.removeIf { it.userId == order.userId }
            if (isRemove) {
                updateTopThree()
            }
            return isRemove
        }

        /**
         * 发送有效撮合后更新前三名
         */
        override fun updateTopThree(): Boolean {
            topThree.buyTopThree.clear()
            topThree.sellTopThree.clear()
            buyOrderList.stream().limit(3).forEach { topThree.buyTopThree.add(it.toOrderInfo()) }
            sellOrderList.stream().limit(3).forEach { topThree.sellTopThree.add(it.toOrderInfo()) }
            return true
        }
    }

    override fun createRoomInfo(record: RoomRecord): TimingRoomInfo {
        return TimingRoomInfo(record)
    }
}