package com.mt.mtengine.match

import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.RoomEnum
import com.mt.mtengine.service.PositionsService
import com.mt.mtengine.service.RoomService
import com.mt.mtengine.service.TradeInfoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.scheduler.Schedulers

/**
 * Created by gyh on 2020/5/3.
 * 及时-连续撮合
 */
@Component
class TimelyMatchStrategy : MatchStrategy() {
    override val roomType = RoomEnum.TIMELY

    @Autowired
    private lateinit var transactionManager: R2dbcTransactionManager

    @Autowired
    private lateinit var roomService: RoomService

    @Autowired
    private lateinit var positionsService: PositionsService

    @Autowired
    private lateinit var tradeInfoService: TradeInfoService

    /**
     * 及时-连续撮合
     * 价格优先，时间优先
     * 由买价第一档开始向卖价第一档撮合
     * 一秒钟撮合一次
     * 未撮合订单保留至撮合持续时间结束
     */
    override fun match(roomInfo: RoomInfo) {
        val buyFailedList = mutableListOf<OrderParam>()
        val sellFailedList = mutableListOf<OrderParam>()
        while (roomInfo.buyOrderList.size >= 1 && roomInfo.sellOrderList.size >= 1) {
            val buyOrder = roomInfo.buyOrderList.stream().max(MatchUtil.sortPriceAndTime).get()
            // TODO 使用排序，快排平均时间复杂度为O(nlogn)，而循环求max时间复杂度为O(n2)
            val sellOrder = roomInfo.sellOrderList.stream().min(MatchUtil.sortPriceAndTime).get()
            roomInfo.buyOrderList.remove(buyOrder)
            roomInfo.sellOrderList.remove(sellOrder)
            if (MatchUtil.verify(buyOrder, sellOrder)) {    // TODO 检查报价不满足的的情况
                if (buyOrder.price != sellOrder.price) {
                    val operator = TransactionalOperator.create(transactionManager)
                    val result = MatchUtil.orderSuccess(positionsService, tradeInfoService, roomService, buyOrder, sellOrder)
                    operator.transactional(result).subscribeOn(Schedulers.elastic()).subscribe()    // TODO 添加回滚事务后的操作
                } else {
                    buyFailedList.add(buyOrder)
                    sellFailedList.add(sellOrder)
                }
            } else {
                MatchUtil.orderFailed(tradeInfoService, roomService, buyOrder, sellOrder,
                        "失败:" + MatchUtil.getVerifyInfo(buyOrder, sellOrder)
                ).subscribeOn(Schedulers.elastic()).subscribe()
            }
        }
        // 戳和失败的放到下一次撮合
        roomInfo.buyOrderList.addAll(buyFailedList)
        roomInfo.sellOrderList.addAll(sellFailedList)
    }
}