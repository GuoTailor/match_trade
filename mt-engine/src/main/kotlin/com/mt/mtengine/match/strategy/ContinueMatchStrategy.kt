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
    override fun match(roomInfo: ContinueRoomInfo) {
        val buyFailedList = mutableListOf<OrderParam>()
        val sellFailedList = mutableListOf<OrderParam>()
        while (roomInfo.buyOrderList.size >= 1 && roomInfo.sellOrderList.size >= 1) {
            val buyOrder = roomInfo.buyOrderList.pollLast()!!       // 最后一个报价最高
            val sellOrder = roomInfo.buyOrderList.pollFirst()!!     // 第一个报价最低
            if (MatchUtil.verify(buyOrder, sellOrder)) {
                if (buyOrder.price!! > sellOrder.price) {
                    matchService.onMatchSuccess(roomInfo.roomId, buyOrder, sellOrder)
                            .subscribeOn(Schedulers.elastic()).subscribe()
                } else {
                    buyFailedList.add(buyOrder)
                    sellFailedList.add(sellOrder)
                }
            } else {
                matchService.onMatchError(buyOrder, sellOrder, "失败:" + MatchUtil.getVerifyInfo(buyOrder, sellOrder))
                        .subscribeOn(Schedulers.elastic()).subscribe()
            }
        }
        // 戳和失败的放到下一次撮合
        roomInfo.buyOrderList.addAll(buyFailedList)
        roomInfo.sellOrderList.addAll(sellFailedList)
    }

    class ContinueRoomInfo(record: RoomRecord) :
            MatchStrategy.RoomInfo(record.roomId!!, record.cycle!!.toMillisOfDay(), record.endTime
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

        override fun add(data: Any): Boolean {
            return if (data is OrderParam && !buyOrderList.contains(data) && !sellOrderList.contains(data)) {
                if (data.isBuy!!) {
                    buyOrderList.add(data)
                } else {
                    sellOrderList.add(data)
                }
            } else if (data is CancelOrder) {
                buyOrderList.removeIf { it.userId == data.userId } || sellOrderList.removeIf { it.userId == data.userId }
            } else false
        }
    }

    override fun createRoomInfo(record: RoomRecord): ContinueRoomInfo {
        return ContinueRoomInfo(record)
    }
}