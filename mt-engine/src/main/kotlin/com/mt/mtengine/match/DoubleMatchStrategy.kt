package com.mt.mtengine.match

import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.RoomEnum
import com.mt.mtcommon.TradeState
import com.mt.mtengine.entity.MtTradeInfo
import com.mt.mtengine.service.PositionsService
import com.mt.mtengine.service.RoomService
import com.mt.mtengine.service.TradeInfoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.math.BigDecimal
import java.util.*

/**
 * Created by gyh on 2020/4/30.
 * 两两成交
 */
@Component
class DoubleMatchStrategy : MatchStrategy() {
    override val roomType = RoomEnum.DOUBLE

    @Autowired
    private lateinit var transactionManager: R2dbcTransactionManager

    @Autowired
    private lateinit var roomService: RoomService

    @Autowired
    private lateinit var positionsService: PositionsService

    @Autowired
    private lateinit var tradeInfoService: TradeInfoService

    /**
     * 两两成交
     * 用户不选择买卖方向，直接报价
     * 将用户报价按时间先后排序
     * 一秒钟一次，报价高者为买方，报价低者为卖方，相邻两笔成交，报价相同则该两笔报价作废
     */
    override fun match(roomInfo: RoomInfo) {
        // 两两撮合报价没有身份，全部存在buy队列里面
        roomInfo.buyOrderList.sortWith(Comparator { o1, o2 -> o1.time.compareTo(o2.time) })
        while (roomInfo.buyOrderList.size >= 2) {
            val order1 = roomInfo.buyOrderList.pollLast()
            val order2 = roomInfo.buyOrderList.pollLast()
            if (MatchUtil.verify(order1, order2)) {
                val result = if (order1.price!! > order2.price) {
                    match(order1, order2)
                } else {
                    match(order2, order1)
                }
                val operator = TransactionalOperator.create(transactionManager)     // TODO 添加回滚事务后的操作
                operator.transactional(result).subscribeOn(Schedulers.elastic()).subscribe()    // 弹性线程池可能会创建大量线程
            } else {
                MatchUtil.orderFailed(tradeInfoService, roomService, order1, order2,
                        "失败:" + MatchUtil.getVerifyInfo(order1, order2))
                        .subscribeOn(Schedulers.elastic()).subscribe()
            }
        }
    }

    fun match(buy: OrderParam, sell: OrderParam): Mono<MtTradeInfo> {
        // TODO 交易限制检查
        return if (buy.price != sell.price) {
            MatchUtil.orderSuccess(positionsService, tradeInfoService, roomService, buy, sell)
        } else {
            MatchUtil.orderFailed(tradeInfoService, roomService, buy, sell, "相邻两笔，报价相同作废")
        }
    }

}