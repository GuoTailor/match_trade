package com.mt.mtengine.match

import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.TradeInfo
import com.mt.mtcommon.TradeState
import com.mt.mtengine.mq.MatchSink
import com.mt.mtengine.service.PositionsService
import com.mt.mtengine.service.RoomService
import com.mt.mtengine.service.TradeInfoService
import org.springframework.messaging.support.MessageBuilder
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.util.*
import kotlin.Comparator

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

    val sortTime = Comparator<OrderParam> { o1, o2 -> o2.time.compareTo(o1.time) }  // 时间降序，越先报价时间越小

    /**
     * 不能用TreeSet.contains比较，它会用比较器去比较，不会调用OrderParam.equals方法
     */
    fun TreeSet<OrderParam>.contain(orderParam: OrderParam): Boolean {
        this.forEach {
            if (it == orderParam) {
                return true
            }
        }
        return false
    }

    /**
     * 基础验证，包括用户id，房间号，报价是否为空及数量，
     * 不验证买卖身份、报价时间和报价是否满足需求
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
                                if (buy.price!! > sell.price) {
                                    "成功"
                                } else "卖方价格（${sell.price}）和买方价格（${buy.price}）不匹配"
                            } else "卖方价格（${sell.price}）或数量（${sell.number}）不合法"
                        } else "买方价格（${buy.price}）或数量（${buy.number}）不合法"
                    } else "卖方房间号（${buy.roomId}）和买方房间号（${sell.roomId}）不相同"
                } else "卖方房间号（${buy.roomId}）或买方房间号（${sell.roomId}）不合法"
            } else "卖方用户id（${buy.userId}）或买方用户id（${sell.userId}）不合法"
        } else if (buy == null) "没有匹配的买家" else "有匹配的卖家"
    }
}