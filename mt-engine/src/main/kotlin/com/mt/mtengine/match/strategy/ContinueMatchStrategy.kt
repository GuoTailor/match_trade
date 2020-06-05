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
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by gyh on 2020/5/3.
 * 及时-连续撮合
 */
@Component
class ContinueMatchStrategy : MatchStrategy<ContinueMatchStrategy.ContinueRoomInfo>() {
    override val roomType = RoomEnum.CONTINUE

    @Autowired
    private lateinit var matchService: MatchService

    /**
     * 及时-连续撮合
     * 价格优先，时间优先
     * 由买价第一档开始向卖价第一档撮合
     * 一秒钟撮合一次
     * 未撮合订单保留至撮合持续时间结束
     */
    override fun match(roomInfo: ContinueRoomInfo): Boolean {
        val buyFailedList = mutableListOf<OrderParam>()
        val sellFailedList = mutableListOf<OrderParam>()
        var isMatch = false
        val atom = AtomicInteger()
        while (roomInfo.buyOrderList.size >= 1 && roomInfo.sellOrderList.size >= 1) {
            val buyOrder = roomInfo.buyOrderList.pollLast()!!       // 最后一个报价最高
            val sellOrder = roomInfo.sellOrderList.pollFirst()!!     // 第一个报价最低
            if (MatchUtil.verify(buyOrder, sellOrder)) {
                if (buyOrder.price!! >= sellOrder.price) {          // 特殊需求，报价相同也成交
                    matchService.onMatchSuccess(roomInfo.roomId, roomInfo.mode, buyOrder, sellOrder, roomInfo.endTime)
                            .subscribeOn(Schedulers.elastic()).subscribe {
                                roomInfo.topThree.lastOrder = it.toOrderInfo()
                                atom.getAndIncrement()
                            }
                    isMatch = true
                } else {
                    buyFailedList.add(buyOrder)
                    sellFailedList.add(sellOrder)
                }
            } else {
                matchService.onMatchError(roomInfo.roomId, roomInfo.mode, buyOrder, sellOrder,
                        "失败:" + MatchUtil.getVerifyInfo(buyOrder, sellOrder), roomInfo.endTime)
                        .subscribeOn(Schedulers.elastic()).subscribe()
                isMatch = true
            }
        }
        // 戳和失败的放到下一次撮合
        roomInfo.buyOrderList.addAll(buyFailedList)
        roomInfo.sellOrderList.addAll(sellFailedList)
        return isMatch
    }

    class ContinueRoomInfo(record: RoomRecord) :
            MatchStrategy.RoomInfo(record.roomId!!, record.mode!!, record.cycle!!.toMillisOfDay(), record.endTime
                    ?: LocalTime.MAX.toDate()) {
        private var nextCycleTime = System.currentTimeMillis() + cycle
        val buyOrderList = TreeSet(MatchUtil.sortPriceAndTime)
        val sellOrderList = TreeSet(MatchUtil.sortPriceAndTime)

        override fun canStart(): Boolean {
            return System.currentTimeMillis() >= nextCycleTime && System.currentTimeMillis() < endTime.time
        }

        override fun isEnd() = System.currentTimeMillis() >= endTime.time

        override fun setNextCycle() {
            nextCycleTime += cycle
        }

        override fun addOrder(data: OrderParam): Boolean {
            return if (!buyOrderList.contain(data) && !sellOrderList.contain(data)) {
                if (data.isBuy!!) {
                    buyOrderList.add(data)
                } else {
                    sellOrderList.add(data)
                }
            } else false
        }

        override fun cancelOrder(order: CancelOrder): Boolean {
            return buyOrderList.removeIf { it.userId == order.userId } || sellOrderList.removeIf { it.userId == order.userId }
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

    override fun createRoomInfo(record: RoomRecord): ContinueRoomInfo {
        return ContinueRoomInfo(record)
    }
}