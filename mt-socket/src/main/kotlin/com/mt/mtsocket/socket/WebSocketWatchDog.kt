package com.mt.mtsocket.socket

import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.TimeoutException

/**
 * Created by gyh on 2020/4/8.
 */
class WebSocketWatchDog {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun start(session: WebSocketSessionHandler, interval: Long): Mono<Void> {
        return session.receive()
                .doOnNext(logger::info)
                .timeout(Duration.ofMillis(interval))
                .doOnError(TimeoutException::class.java) { session.send(it.message ?: "错误").subscribe() }
                .doOnError(TimeoutException::class.java) { session.connectionClosed() }
                .doOnError(TimeoutException::class.java) { logger.info("超时 " + session.getId()) }
                .then()
    }
}