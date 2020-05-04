package com.mt.mtengine.match

import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.RivalInfo
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.LockSupport
import kotlin.collections.HashMap


/**
 * Created by gyh on 2020/5/3.
 */
// TODO 抽象接口让每个戳和策略自己实现
class RoomInfo(
        val roomId: String,     // 房间号
        private var cycle: Long // 周期，单位毫秒
) {
    private val tempAdd = AtomicReference<Any>()
    val buyOrderList = LinkedList<OrderParam>()     // TODO 考虑使用treeMap
    val sellOrderList = LinkedList<OrderParam>()
    val rivalList = HashMap<Int, RivalInfo>()

    var nextCycleTime = System.currentTimeMillis() + cycle

    fun isStart(): Boolean = System.currentTimeMillis() >= nextCycleTime    // TODO 添加对房间关闭的支持

    fun setNextCycle() {
        nextCycleTime += cycle
    }

    /**
     * @param cycle 单位毫秒
     */
    fun updateCycle(cycle: Long) {
        nextCycleTime = nextCycleTime - this.cycle + cycle
        this.cycle = cycle
    }

    /**
     * [tryAddOrder]是生产者线程，会有多线程竞争，[add]是消费者线程调用，永远只有一个消费者，不存在线程竞争
     */
    fun tryAddOrder(order: OrderParam, packTime: Long) {
        while (!tempAdd.compareAndSet(null, order)) {
            LockSupport.parkNanos(packTime)
        }
    }

    fun tryAddRival(rival: RivalInfo, packTime: Long) {
        while (!tempAdd.compareAndSet(null, rival)) {
            LockSupport.parkNanos(packTime)
        }
    }

    /**
     * [tryAddOrder]是生产者线程，会有多线程竞争，[add]是消费者线程，永远只有一个消费者，不存在线程竞争
     */
    fun add(): Boolean {
        val temp = tempAdd.getAndSet(null)
        return if (temp is OrderParam) {
            if (temp.isBuy == null || temp.isBuy!!) {
                buyOrderList.add(temp)
            } else {
                sellOrderList.add(temp)
            }
        } else if (temp is RivalInfo) {
            rivalList[temp.userId!!] = temp
            true
        } else false
    }
}