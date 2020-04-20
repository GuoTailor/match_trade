package com.mt.mtengine

import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel

/**
 * Created by gyh on 2020/4/18.
 */
interface MatchSink {

    @Input(IN_ORDER)
    fun inOrder(): SubscribableChannel

    @Output(OUT_TRADE)
    fun outTrade(): MessageChannel

    companion object {
        const val OUT_TRADE = "out-trade"
        const val IN_ORDER = "in-order"
    }
}