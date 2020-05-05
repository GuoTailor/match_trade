package com.mt.mtengine.match

import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.RivalInfo

/**
 * Created by gyh on 2020/5/1.
 */
object MatchManager {
    val strategyList = mutableListOf<MatchStrategy>()

    fun register(list: Collection<MatchStrategy>) {
        strategyList.addAll(list)
        list.forEach { it.start() }
    }

    fun register(strategy: MatchStrategy) {
        strategyList.add(strategy)
        strategy.start()
    }

    fun add(order: OrderParam) {
        strategyList.forEach {
            if (it.isCanAdd(order.roomId)) {
                it.tryAddOrder(order)
            }
        }
    }

    fun add(rival: RivalInfo) {
        strategyList.forEach {
            if (it.isCanAdd(rival.roomId)) {
                it.tryAddRival(rival)
            }
        }
    }
}