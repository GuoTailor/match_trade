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
    override fun match(roomInfo: BickerRoomInfo) {
        // 抬杠撮合报价没有身份，全部存在buy队列里面
        while (roomInfo.orderList.size >= 2) {
            val buyOrder = roomInfo.orderList.pollLast()!!    // 报价最高的为买家
            val sellOrder = roomInfo.orderList.pollFirst()!!
            if (MatchUtil.verify(buyOrder, sellOrder) && buyOrder.price != sellOrder.price) {
                matchService.onMatchSuccess(roomInfo.roomId, buyOrder, sellOrder)
                        .subscribeOn(Schedulers.elastic()).subscribe()    // 弹性线程池可能会创建大量线程
            } else {
                matchService.onMatchError(buyOrder, sellOrder, "失败:" + MatchUtil.getVerifyInfo(buyOrder, sellOrder))
                        .subscribeOn(Schedulers.elastic()).subscribe()
            }
        }
        roomInfo.orderList.forEach {
            matchService.onMatchError(it, null,"失败: 没有可以匹配的报价")
                    .subscribeOn(Schedulers.elastic()).subscribe()
        }
        roomInfo.orderList.clear()
    }

    class BickerRoomInfo(record: RoomRecord) :
            MatchStrategy.RoomInfo(record.roomId!!, record.endTime!!.time, record.endTime ?: LocalTime.MAX.toDate()) {
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

        override fun add(data: Any): Boolean {
            return if (data is OrderParam && !orderList.contains(data)) {
                orderList.add(data)
            } else if (data is CancelOrder) {
                orderList.removeIf { it.userId == data.userId }
            } else false
        }

    }

    override fun createRoomInfo(record: RoomRecord): BickerRoomInfo {
        return BickerRoomInfo(record)
    }

}