package com.mt.mtengine.match.strategy

import com.mt.mtcommon.*
import com.mt.mtengine.match.MatchUtil
import com.mt.mtengine.mq.MatchSink
import com.mt.mtengine.service.MatchService
import com.mt.mtengine.service.PositionsService
import com.mt.mtengine.service.RoomService
import com.mt.mtengine.service.TradeInfoService
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
    override fun match(roomInfo: TimingRoomInfo) {
        while (roomInfo.buyOrderList.size >= 1 && roomInfo.sellOrderList.size >= 1) {
            val buyOrder = roomInfo.buyOrderList.pollLast()!!
            val sellOrder = roomInfo.sellOrderList.pollFirst()!!
            if (MatchUtil.verify(buyOrder, sellOrder)) {
                if (buyOrder.price!! > sellOrder.price) {
                    matchService.onMatchSuccess(roomInfo.roomId, roomInfo.flag, buyOrder, sellOrder)
                            .subscribeOn(Schedulers.elastic()).subscribe()
                }
            } else {
                matchService.onMatchError(buyOrder, sellOrder, "失败:" + MatchUtil.getVerifyInfo(buyOrder, sellOrder))
                        .subscribeOn(Schedulers.elastic()).subscribe()
            }
        }
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
            nextCycleTime += cycle
        }

        override fun add(data: Any): Boolean {
            return if (data is OrderParam && !buyOrderList.contains(data) && !sellOrderList.contains(data)) {
                if (data.isBuy!!) {
                    buyOrderList.add(data)
                } else {
                    sellOrderList.add(data)
                }
            } else if (data is CancelOrder && nextCycleTime >= System.currentTimeMillis() + 60_000) {   // 结束前一分钟不允许撤单
                buyOrderList.removeIf { it.userId == data.userId } || sellOrderList.removeIf { it.userId == data.userId }
            } else false
        }
    }

    override fun createRoomInfo(record: RoomRecord): TimingRoomInfo {
        return TimingRoomInfo(record)
    }
}