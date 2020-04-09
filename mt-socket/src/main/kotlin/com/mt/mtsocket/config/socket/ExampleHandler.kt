package com.mt.mtsocket.config.socket

import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration

/**
 * Created by gyh on 2020/4/5.
 * 这个架构一点也不好用
 */
@WebSocketMapping("/echo")
class ExampleHandler : WebSocketHandler {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun handle(session: WebSocketSession): Mono<Void> {
        val sessionHandler = WebSocketSessionHandler(session)
        val watchDog = WebSocketWatchDog().start(sessionHandler, 3000).ignoreElements()
        val send = Flux
                .interval(Duration.ofMillis(1000))
                .subscribeOn(Schedulers.elastic())
                .takeUntil { !sessionHandler.isConnected() }
                .map { value: Long -> value.toString() }
                .doOnNext { message -> logger.info("Server Sent: [{}]", message) }
                .flatMap(sessionHandler::send)
        var disposable: Disposable? = null
        val connect = sessionHandler.connected()
                .doOnNext { disposable = send.subscribe() }

        val disconnect = sessionHandler.disconnected()
                .doOnNext {
                    logger.info("Server Disconnected [{}]", it.id)
                    disposable?.dispose()
                }
        val output = sessionHandler.receive()
                .flatMap(sessionHandler::send)
                .ignoreElements()

        logger.info("lianjie")
        return sessionHandler.handle()
                .zipWith(connect) { o1, _ -> o1 }
                .zipWith(disconnect) { o1, _ -> o1 }
                .zipWith(output) { o1, _ -> o1 }
                .zipWith(watchDog) { o1, _ -> o1 }
    }
}
