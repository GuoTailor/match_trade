package com.mt.mtengine.match

import com.mt.mtcommon.RoomEnum
import com.mt.mtengine.mq.MatchSink
import com.mt.mtengine.service.PositionsService
import com.mt.mtengine.service.RoomService
import com.mt.mtengine.service.TradeInfoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.scheduler.Schedulers

/**
 * Created by gyh on 2020/5/4.
 * 点选撮合
 */
@Component
class ClickMatchStrategy : MatchStrategy() {
    override val roomType = RoomEnum.CLICK
    @Autowired
    private lateinit var sink: MatchSink

    @Autowired
    private lateinit var transactionManager: R2dbcTransactionManager

    @Autowired
    private lateinit var roomService: RoomService

    @Autowired
    private lateinit var positionsService: PositionsService

    @Autowired
    private lateinit var tradeInfoService: TradeInfoService

    /**
     * 点选撮合
     * 价格优先、时间优先
     * 从买单最高价开始撮合，在其所有双向选中单子中，卖价最低的优先成交，卖价相同则先提交的成交，只成交一次
     * 撮合完成后所有未成交订单作废
     */
    override fun match(roomInfo: RoomInfo) {
        roomInfo.buyOrderList.sortWith(MatchUtil.sortPriceAndTime)
        while (roomInfo.buyOrderList.size >= 1) {
            val buyOrder = roomInfo.buyOrderList.pollLast() // 升序排序，最后一个报价最高
            val buyRivals = roomInfo.rivalList[buyOrder.userId]?.rivals ?: arrayOf()    // 获取用户的交易对手
            val sellOrder = roomInfo.sellOrderList
                    .stream().filter { buyRivals.contains(it.userId) }  // 获取用户交易对手的报价信息
                    .min(MatchUtil.sortPriceAndTime).get()  // 获取对手中卖价最低的订单
            roomInfo.sellOrderList.remove(sellOrder)
            if (MatchUtil.verify(buyOrder, sellOrder)) {
                if (buyOrder.price != sellOrder.price) {
                    val operator = TransactionalOperator.create(transactionManager)
                    val result = MatchUtil.orderSuccess(positionsService, tradeInfoService, roomService, sink, buyOrder, sellOrder)
                    operator.transactional(result).subscribeOn(Schedulers.elastic()).subscribe()    // TODO 添加回滚事务后的操作
                }
            } else {
                MatchUtil.orderFailed(tradeInfoService, roomService, sink, buyOrder, sellOrder,
                        "失败:" + MatchUtil.getVerifyInfo(buyOrder, sellOrder)
                ).subscribeOn(Schedulers.elastic()).subscribe()
            }
        }
        roomInfo.rivalList.clear()
    }
}