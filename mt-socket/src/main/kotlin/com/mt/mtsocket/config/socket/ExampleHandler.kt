package com.mt.mtsocket.config.socket

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mt.mtsocket.distribute.DispatcherServlet
import com.mt.mtsocket.distribute.ServiceRequestInfo
import com.mt.mtsocket.distribute.ServiceResponseInfo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import kotlin.math.log

/**
 * Created by gyh on 2020/4/5.
 * 这个架构一点也不好用
 */
@WebSocketMapping("/echo")
class ExampleHandler : WebSocketHandler {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val json = jacksonObjectMapper()

    @Autowired
    private lateinit var dispatcherServlet: DispatcherServlet

    override fun handle(session: WebSocketSession): Mono<Void> {
        val sessionHandler = WebSocketSessionHandler(session)
        val watchDog = WebSocketWatchDog().start(sessionHandler, 3000)
        val send = Flux.interval(Duration.ofMillis(1000), Schedulers.elastic())
                .takeUntil { !sessionHandler.isConnected() }
                .map { value: Long -> value.toString() }
                .doOnNext { message -> logger.info("Server Sent: [{}]", message) }
                .flatMap(sessionHandler::send)
                .doOnComplete { logger.info("完成") }

        val disposable: Disposable = send.subscribe()

        val disconnect = sessionHandler.disconnected()
                .doOnNext {
                    logger.info("Server Disconnected [{}]", it.id)
                    disposable.dispose()
                }
        val output = sessionHandler.receive()
                .flatMap {
                    val req = ServiceRequestInfo("/echo", it, it, 0)
                    val resp = ServiceResponseInfo(req = 0)
                    dispatcherServlet.doDispatch(req, resp)
                    logger.info(">>>>>>>>>>>><<<")
                    resp.getMono()
                }
                .map { json.writeValueAsString(it) }
                .flatMap(sessionHandler::send)
                .ignoreElements()

        return sessionHandler.handle()
                .zipWith(disconnect) { o1, _ -> o1 }
                .zipWith(output) { o1, _ -> o1 }
                .zipWith(watchDog) { o1, _ -> o1 }
    }

}
