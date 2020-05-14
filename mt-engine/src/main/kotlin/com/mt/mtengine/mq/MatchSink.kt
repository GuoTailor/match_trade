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
     * 撤单
     */
    @Input(IN_CANCEL)
    fun inCancel(): SubscribableChannel

    /**
     * 交易状态通知
     */
    @Output(OUT_TRADE)
    fun outTrade(): MessageChannel

    /**
     * 添加订单结果通知
     */
    @Output(OUT_RESULT)
    fun outResult(): MessageChannel

    companion object {
        const val OUT_TRADE = "out-trade"
        const val OUT_RESULT = "out-result"
        const val IN_ORDER = "in-order"
        const val IN_RIVAL = "in-rival"
        const val IN_CANCEL = "in-cancel"
    }
}