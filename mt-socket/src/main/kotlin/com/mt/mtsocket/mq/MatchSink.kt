package com.mt.mtsocket.mq

import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel

/**
 * Created by gyh on 2020/4/18.
 */
interface MatchSink {

    @Output(OUT_ORDER)
    fun outOrder():  MessageChannel

    @Output(OUT_RIVAL)
    fun outRival():  MessageChannel

    @Output(OUT_CANCEL)
    fun outCancel():  MessageChannel

    @Input(IN_TRADE)
    fun inTrade(): SubscribableChannel

    companion object {
        const val IN_TRADE = "in-trade"
        const val OUT_ORDER = "out-order"
        const val OUT_RIVAL = "out-rival"
        const val OUT_CANCEL = "out-cancel"
    }
}
