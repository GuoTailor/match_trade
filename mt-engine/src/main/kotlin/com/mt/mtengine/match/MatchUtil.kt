package com.mt.mtengine.match

import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.TradeState
import com.mt.mtengine.entity.MtTradeInfo
import com.mt.mtengine.service.PositionsService
import com.mt.mtengine.service.RoomService
import com.mt.mtengine.service.TradeInfoService
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Created by gyh on 2020/5/3.
 */
object MatchUtil {

    val sortPriceAndTime = Comparator<OrderParam> { o1, o2 ->
        val priceResult = o1.price!!.compareTo(o2.price!!)  // 价格升序
        if (priceResult == 0) {
            return@Comparator o2.time.compareTo(o1.time)    // 时间降序，越先报价时间越小
        } else return@Comparator priceResult
    }

    /**
     * 基础验证，包括用户id，房间号，报价及数量，
     * 不验证买卖身份和报价时间
     */
    fun verify(buy: OrderParam?, sell: OrderParam?): Boolean {
        if (buy != null && sell != null) {
            if (buy.userId != null && buy.userId != null) {
                if (buy.roomId != null && sell.roomId != null) {
                    if (buy.roomId == sell.roomId) {
                        return buy.verify() && sell.verify()
                    }
                }
            }
        }
        return false
    }

    fun getVerifyInfo(buy: OrderParam?, sell: OrderParam?): String {
        return if (buy != null && sell != null) {
            if (buy.userId != null && buy.userId != null) {
                if (buy.roomId != null && sell.roomId != null) {
                    if (buy.roomId == sell.roomId) {
                        if (buy.verify()) {
                            if (sell.verify()) {
                                "成功"
                            } else "卖方价格（${sell.price}）或数量（${sell.number}）不合法"
                        } else "买方价格（${buy.price}）或数量（${buy.number}）不合法"
                    } else "卖方房间号（${buy.roomId}）和买方房间号（${sell.roomId}）不相同"
                } else "卖方房间号（${buy.roomId}）或买方房间号（${sell.roomId}）不合法"
            } else "卖方用户id（${buy.userId}）或买方用户id（${sell.userId}）不合法"
        } else if (buy == null) "没有匹配的买家" else "有匹配的卖家"
    }

    fun orderSuccess(positionsService: PositionsService,
                     tradeInfoService: TradeInfoService,
                     roomService: RoomService,
                     buy: OrderParam,
                     sell: OrderParam): Mono<MtTradeInfo> {

        return roomService.findCompanyIdByRoomId(buy.roomId!!)
                .flatMap {
                    val m1 = positionsService.addAmount(it.companyId, it.stockId, buy.userId!!, buy.number)
                    val m2 = positionsService.minusAmount(it.companyId, it.stockId, sell.userId!!, sell.number)
                    m1.zipWith(m2) { _, _ ->
                        val threadInfo = MtTradeInfo(buy, sell, it.companyId, it.stockId)
                        threadInfo.tradePrice = buy.price?.add(sell.price)?.divide(BigDecimal(2))
                        threadInfo.tradeState = TradeState.SUCCESS
                        threadInfo
                    }.flatMap { threadInfo -> tradeInfoService.save(threadInfo) }
                }
    }

    fun orderFailed(tradeInfoService: TradeInfoService,
                    roomService: RoomService,
                    buy: OrderParam,
                    sell: OrderParam?,
                    stateDetails: String): Mono<MtTradeInfo> {

        return roomService.findCompanyIdByRoomId(buy.roomId!!)
                .flatMap {
                    val threadInfo = MtTradeInfo(buy, sell, it.companyId, it.stockId)

                    if (buy.price != null && sell?.price != null)
                        threadInfo.tradePrice = buy.price?.add(sell.price)?.divide(BigDecimal(2))

                    threadInfo.tradeState = TradeState.FAILED
                    threadInfo.stateDetails = stateDetails
                    tradeInfoService.save(threadInfo)
                }
    }
}