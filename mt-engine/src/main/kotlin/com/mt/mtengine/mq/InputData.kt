package com.mt.mtengine.mq

import com.mt.mtcommon.RivalInfo
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

/**
 * Created by gyh on 2020/4/30.
 */
@Component
class InputData {

    @StreamListener(MatchSink.IN_ORDER)
    fun inputOrder() {

    }

    fun inputRival(@Payload rivalInfo: RivalInfo) {

    }
}