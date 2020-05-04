package com.mt.mtengine.match

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
 * Created by gyh on 2020/5/4.
 * 抬杠交易
 */
@Component
class BickerMatchStrategy : MatchStrategy() {
    override val roomType = RoomEnum.BICKER

    @Autowired
    private lateinit var transactionManager: R2dbcTransactionManager

    @Autowired
    private lateinit var roomService: RoomService

    @Autowired
    private lateinit var positionsService: PositionsService

    @Autowired
    private lateinit var tradeInfoService: TradeInfoService

    /**
     * 抬杠交易
     * 不选择交易身份，时间一到就开始撮合
     * 报价从高到低依次排列
     * 最高和最低撮合，成交价取平均
     * 报价人数是奇数舍去中间的报价
     */
    override fun match(roomInfo: RoomInfo) {
        // 抬杠撮合报价没有身份，全部存在buy队列里面
        roomInfo.buyOrderList.sortWith(MatchUtil.sortPriceAndTime)
        while (roomInfo.buyOrderList.size >= 2) {
            val buyOrder = roomInfo.buyOrderList.pollLast()    // 报价最高的为买家
            val sellOrder = roomInfo.buyOrderList.pollFirst()
            if (MatchUtil.verify(buyOrder, sellOrder)) {
                val result = MatchUtil.orderSuccess(positionsService, tradeInfoService, roomService, buyOrder, sellOrder)
                val operator = TransactionalOperator.create(transactionManager)     // TODO 添加回滚事务后的操作
                operator.transactional(result).subscribeOn(Schedulers.elastic()).subscribe()    // 弹性线程池可能会创建大量线程
            } else {
                MatchUtil.orderFailed(tradeInfoService, roomService, buyOrder, sellOrder,
                        "失败:" + MatchUtil.getVerifyInfo(buyOrder, sellOrder))
                        .subscribeOn(Schedulers.elastic()).subscribe()
            }
        }
        roomInfo.buyOrderList.forEach {
            MatchUtil.orderFailed(tradeInfoService, roomService, it, null, "失败: 没有可以匹配的报价")
                    .subscribeOn(Schedulers.elastic()).subscribe()
        }
    }
}