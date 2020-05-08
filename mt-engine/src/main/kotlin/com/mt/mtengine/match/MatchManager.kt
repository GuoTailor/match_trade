package com.mt.mtengine.match

import com.mt.mtcommon.CancelOrder
import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.RivalInfo
import com.mt.mtengine.match.strategy.MatchStrategy

/**
 * Created by gyh on 2020/5/1.
 */
object MatchManager {
    private val strategyList = mutableListOf<MatchStrategy<out MatchStrategy.RoomInfo>>()

    fun register(list: Collection<MatchStrategy<out MatchStrategy.RoomInfo>>) {
        strategyList.addAll(list)
        list.forEach { it.start() }
    }

    fun register(strategy: MatchStrategy<out MatchStrategy.RoomInfo>) {
        strategyList.add(strategy)
        strategy.start()
    }

    fun add(order: OrderParam) {
        strategyList.forEach {
            if (it.isCan(order.flag ?: "")) {
                it.tryAddOrder(order)
            }
        }
    }

    fun add(rival: RivalInfo) {
        strategyList.forEach {
            if (it.isCan(rival.flag ?: "")) {
                it.tryAddRival(rival)
            }
        }
    }

    fun cancel(cancelOrder: CancelOrder) {
        strategyList.forEach {
            if (it.isCan(cancelOrder.flag ?: "")) {
                it.tryCancelOrder(cancelOrder)
            }
        }
    }
}