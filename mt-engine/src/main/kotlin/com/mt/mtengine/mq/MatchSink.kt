package com.mt.mtengine.mq

import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel

/**
 * Created by gyh on 2020/4/18.
 */
interface MatchSink {

    /**
     * 添加订单
     */
    @Input(IN_ORDER)
    fun inOrder(): SubscribableChannel

    /**
     * 添加选择的对手
     */
    @Input(IN_RIVAL)
    fun inRival(): SubscribableChannel

    /**
     * 交易状态通知
     */
    @Output(OUT_TRADE)
    fun outTrade(): MessageChannel

    companion object {
        const val OUT_TRADE = "out-trade"
        const val IN_ORDER = "in-order"
        const val IN_RIVAL = "in-rival"
    }
}