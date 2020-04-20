package com.mt.mtsocket

import com.mt.mtsocket.mq.MatchSink
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.stream.annotation.EnableBinding

@SpringBootApplication
@EnableBinding(MatchSink::class)
class MtSocketApplication

fun main(args: Array<String>) {
    runApplication<MtSocketApplication>(*args)
}
