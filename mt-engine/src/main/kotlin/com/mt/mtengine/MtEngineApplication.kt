package com.mt.mtengine

import com.mt.mtengine.mq.MatchSink
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.stream.annotation.EnableBinding

@SpringBootApplication
@EnableBinding(MatchSink::class)
@EnableCaching
class MtEngineApplication

fun main(args: Array<String>) {
    runApplication<MtEngineApplication>(*args)
}
