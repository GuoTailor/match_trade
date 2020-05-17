package com.mt.mtengine.match.strategy

import com.mt.mtcommon.*
import com.mt.mtengine.match.MatchUtil
import com.mt.mtengine.match.MatchUtil.contain
import com.mt.mtengine.mq.MatchSink
import com.mt.mtengine.service.MatchService
import com.mt.mtengine.service.PositionsService
import com.mt.mtengine.service.RoomService
import com.mt.mtengine.service.TradeInfoService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
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
            if (MatchUtil.verify(buyOrder, sellOrder) && buyOrder.price!! > sellOrder.price) {
                matchService.onMatchSuccess(roomInfo.roomId, roomInfo.flag, buyOrder, sellOrder)
                        .subscribeOn(Schedulers.elastic()).subscribe()
            } else {
                matchService.onMatchError(buyOrder, sellOrder, "失败:" + MatchUtil.getVerifyInfo(buyOrder, sellOrder))
                        .subscribeOn(Schedulers.elastic()).subscribe()
            }
        }
        roomInfo.buyOrderList.forEach {
            matchService.onMatchError(it, null, "失败:" + MatchUtil.getVerifyInfo(it, null))
                    .subscribeOn(Schedulers.elastic()).subscribe()
        }
        roomInfo.sellOrderList.forEach {
            matchService.onMatchError(null, it, "失败:" + MatchUtil.getVerifyInfo(null, it))
                    .subscribeOn(Schedulers.elastic()).subscribe()
        }
        return isMatch
    }

    class TimingRoomInfo(record: RoomRecord) :
            MatchStrategy.RoomInfo(record.roomId!!, record.model!!, record.endTime!!.time, record.endTime
                    ?: LocalTime.MAX.toDate()) {
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
            return if (!buyOrderList.contain(data) && !sellOrderList.contain(data)) {
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
        override fun updateTopThree(data: OrderParam): Boolean = false
        override fun updateTopThree(order: CancelOrder): Boolean = false
        override fun updateTopThree(): Boolean = false
    }

    override fun createRoomInfo(record: RoomRecord): TimingRoomInfo {
        return TimingRoomInfo(record)
    }
}