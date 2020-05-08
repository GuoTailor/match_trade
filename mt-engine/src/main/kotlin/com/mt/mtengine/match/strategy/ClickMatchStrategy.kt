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
 * Created by gyh on 2020/5/4.
 * 点选撮合
 */
@Component
class ClickMatchStrategy : MatchStrategy<ClickMatchStrategy.ClickRoomInfo>() {
    override val roomType = RoomEnum.CLICK

    @Autowired
    private lateinit var matchService: MatchService

    /**
     * 点选撮合
     * 价格优先、时间优先
     * 从买单最高价开始撮合，在其所有双向选中单子中，卖价最低的优先成交，卖价相同则先提交的成交，只成交一次
     * 撮合完成后所有未成交订单作废
     */
    override fun match(roomInfo: ClickRoomInfo) {
        while (roomInfo.buyOrderList.size >= 1) {
            val buyOrder = roomInfo.buyOrderList.pollLast()!!   // 升序排序，最后一个报价最高
            val buyRivals = roomInfo.rivalList[buyOrder.userId]?.rivals ?: arrayOf()    // 获取用户的交易对手
            val sellOrder = roomInfo.sellOrderList
                    .stream().filter { buyRivals.contains(it.userId) }  // 过滤用户的交易对手
                    .filter {
                        roomInfo.rivalList[it.userId]?.rivals?.contains(buyOrder.userId) ?: false
                    }   // 获取对手中也选了自己的订单
                    .min(MatchUtil.sortPriceAndTime).get()      // 获取对手中卖价最低的订单
            roomInfo.sellOrderList.remove(sellOrder)
            if (MatchUtil.verify(buyOrder, sellOrder) && buyOrder.price!! > sellOrder.price) {
                matchService.onMatchSuccess(roomInfo.roomId, roomInfo.flag, buyOrder, sellOrder)
                        .subscribeOn(Schedulers.elastic()).subscribe()
            } else {
                matchService.onMatchError(buyOrder, sellOrder, "失败:" + MatchUtil.getVerifyInfo(buyOrder, sellOrder))
                        .subscribeOn(Schedulers.elastic()).subscribe()
            }
        }
        roomInfo.rivalList.clear()
        roomInfo.buyOrderList.forEach {
            matchService.onMatchError(it, null, "失败: 没有可以匹配的报价")
                    .subscribeOn(Schedulers.elastic()).subscribe()
        }
        roomInfo.sellOrderList.forEach {
            matchService.onMatchError(it, null, "失败: 没有可以匹配的报价")
                    .subscribeOn(Schedulers.elastic()).subscribe()
        }
        roomInfo.buyOrderList.clear()
        roomInfo.sellOrderList.clear()
    }

    class ClickRoomInfo(record: RoomRecord) :
            MatchStrategy.RoomInfo(record.roomId!!, record.model!!, record.endTime!!.time, record.endTime ?: LocalTime.MAX.toDate()) {
        private var count = 0
        val buyOrderList = TreeSet(MatchUtil.sortPriceAndTime)
        val sellOrderList = LinkedList<OrderParam>()
        val rivalList = HashMap<Int, RivalInfo>()

        override fun canStart(): Boolean {
            return System.currentTimeMillis() >= cycle && count == 0
        }

        override fun isEnd() = count > 0

        /**
         * 点选撮合只撮合一次
         */
        override fun setNextCycle() {
            count++
        }

        override fun add(data: Any): Boolean {
            return if (data is OrderParam && !buyOrderList.contains(data) && !sellOrderList.contains(data)) {
                if (data.isBuy!!) {
                    buyOrderList.add(data)
                } else {
                    sellOrderList.add(data)
                }
            } else if (data is RivalInfo && !rivalList.containsKey(data.userId!!)) {
                rivalList[data.userId!!] = data
                true
            } else if (data is CancelOrder) {
                buyOrderList.removeIf { it.userId == data.userId } || sellOrderList.removeIf { it.userId == data.userId }
            } else false
        }
    }

    override fun createRoomInfo(record: RoomRecord): ClickRoomInfo {
        return ClickRoomInfo(record)
    }

}