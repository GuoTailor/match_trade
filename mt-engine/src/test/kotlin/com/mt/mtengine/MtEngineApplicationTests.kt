package com.mt.mtengine

import org.apache.rocketmq.common.message.MessageConst
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.support.MessageBuilder

@SpringBootTest
class MtEngineApplicationTests {
    @Autowired
    lateinit var matchSink: MatchSink

    @Test
    fun contextLoads() {
        val msg = mapOf("12" to 12, "nmka" to "abc")
        val message = MessageBuilder.withPayload(msg)
                .setHeader(MessageConst.PROPERTY_TAGS, "testTag")
                .build()
        val rest = matchSink.outTrade().send(message)
        println(rest)
    }

}
