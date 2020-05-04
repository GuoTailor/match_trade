package com.mt.mtsocket.mq

import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Component

/**
 * Created by gyh on 2020/4/20.
 */
@Component
class MatchOutProcessor {

    @StreamListener(MatchSink.IN_TRADE)
    fun inTrade() {
        TODO()
    }
}