package com.mt.mtengine

import com.mt.mtcommon.RivalInfo
import com.mt.mtengine.mq.MatchSink
import org.apache.rocketmq.common.message.MessageConst
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.support.MessageBuilder

val logger = LoggerFactory.getLogger(MtEngineApplication::class.java)

@SpringBootApplication
@EnableBinding(MatchSink::class)
class MtEngineApplication {

    @StreamListener(MatchSink.IN_ORDER)
    fun receive(@Payload messageBody: RivalInfo, @Headers headers: Map<String, Any>) {
        logger.info("通过stream收到了消息，messageBody = $messageBody, headers = $headers")
        logger.info("messageBodyType = ${messageBody.javaClass}, headersType = ${headers.javaClass}")
    }

}

fun main(args: Array<String>) {
    val context = runApplication<MtEngineApplication>(*args)
    Thread {
        Thread.sleep(3000)
        val bean = context.getBean(MatchSink::class.java)
        val msg = RivalInfo(4, "", arrayOf(1, 2, 3))
        val message = MessageBuilder.withPayload(msg)
                .setHeader(MessageConst.PROPERTY_TAGS, "testTag")
                .build()
        var rest = bean.outTrade().send(message)
        logger.info(rest.toString())
        Thread.sleep(2000)
        rest = bean.outTrade().send(message)
        logger.info(rest.toString())
    }.start()

}
