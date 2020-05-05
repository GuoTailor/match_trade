package com.mt.mtengine.match

import com.mt.mtcommon.OrderParam
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
 * 定时撮合
 */
@Component
class TimingMatchStrategy : MatchStrategy() {
    override val roomType = RoomEnum.TIMING
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
     * 定时撮合
     * 价格优先、时间优先.
     * 由买价第一档开始向卖价第一档撮合.
     * 报价相同作废
     */
    override fun match(roomInfo: RoomInfo) {
        while (roomInfo.buyOrderList.size >= 1 && roomInfo.sellOrderList.size >= 1) {
            val buyOrder = roomInfo.buyOrderList.stream().max(MatchUtil.sortPriceAndTime).get()
            val sellOrder = roomInfo.sellOrderList.stream().max(MatchUtil.sortPriceAndTime).get()
            roomInfo.buyOrderList.remove(buyOrder)
            roomInfo.sellOrderList.remove(sellOrder)
            if (MatchUtil.verify(buyOrder, sellOrder)) {
                if (buyOrder.price != sellOrder.price) {
                    val operator = TransactionalOperator.create(transactionManager)
                    val result = MatchUtil.orderSuccess(positionsService, tradeInfoService, roomService, sink, buyOrder, sellOrder)
                    operator.transactional(result).subscribeOn(Schedulers.elastic()).subscribe()    // TODO 添加回滚事务后的操作
                } else {
                    MatchUtil.orderFailed(tradeInfoService, roomService, sink, buyOrder, sellOrder, "报价相同作废")
                }
            } else {
                MatchUtil.orderFailed(tradeInfoService, roomService, sink, buyOrder, sellOrder,
                        "失败:" + MatchUtil.getVerifyInfo(buyOrder, sellOrder)
                ).subscribeOn(Schedulers.elastic()).subscribe()
            }
        }
    }
}