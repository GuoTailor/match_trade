package com.mt.mtsocket.config.socket

import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoProcessor
import reactor.core.publisher.ReplayProcessor
import reactor.netty.channel.AbortedException
import java.nio.channels.ClosedChannelException

/**
 * Created by gyh on 2020/4/7.
 */
class WebSocketSessionHandler {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val receiveProcessor: ReplayProcessor<String>
    private val connectedProcessor: MonoProcessor<WebSocketSession>
    private val disconnectedProcessor: MonoProcessor<WebSocketSession>

    private var webSocketConnected = false
    private val session: WebSocketSession

    constructor(session: WebSocketSession) : this(50, session)

    constructor(historySize: Int, session: WebSocketSession) {
        receiveProcessor = ReplayProcessor.create(historySize)
        connectedProcessor = MonoProcessor.create()
        disconnectedProcessor = MonoProcessor.create()
        webSocketConnected = true
        this.session = session
    }

    fun handle(): Mono<Void> {
        return session.receive()
                .map { obj -> obj.payloadAsText }
                .doOnNext { t -> receiveProcessor.onNext(t) }
                .doOnComplete { logger.info("nmka2 Complete");connectionClosed().subscribe() }
                .doOnCancel { logger.info("nmka2 Cancel");connectionClosed().subscribe() }
                .doOnRequest {
                    webSocketConnected = true
                    connectedProcessor.onNext(session)
                }.then()
    }

    fun connected(): Mono<WebSocketSession> {
        return connectedProcessor
    }

    fun disconnected(): Mono<WebSocketSession> {
        return disconnectedProcessor
    }

    fun isConnected(): Boolean {
        return webSocketConnected
    }

    fun receive(): Flux<String> {
        return receiveProcessor
    }

    fun getId(): String {
        return session.id
    }

    fun getSession() = session

    fun send(message: String): Mono<Void> {
        return if (webSocketConnected) {
            logger.info("send $message")
            session.send(Mono.just(session.textMessage(message)))
                    .onErrorResume(ClosedChannelException::class.java) { connectionClosed() }
                    .onErrorResume(AbortedException::class.java) { connectionClosed() }
                    .doOnError { logger.info("send error ${it.message}") }
        } else Mono.empty()
    }

    fun connectionClosed(): Mono<Void> {
        webSocketConnected = false
        val result = session.close()
        receiveProcessor.onComplete()
        disconnectedProcessor.onNext(session)
        return result
    }
}