package com.mt.mtsocket.config.socket

import org.springframework.web.reactive.socket.WebSocketMessage
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
        webSocketConnected = false
        this.session = session
    }

    internal fun handle(): Mono<Void> {
        val receive = session.receive()
                .map { obj -> obj.payloadAsText }
                .doOnNext { t -> receiveProcessor.onNext(t) }
                .doOnComplete { receiveProcessor.onComplete() }
        val connected = Mono.fromRunnable<Any> {
            webSocketConnected = true
            connectedProcessor.onNext(session)
        }
        val disconnected = Mono.fromRunnable<Any> {
            webSocketConnected = false
            disconnectedProcessor.onNext(session)
        }
        return connected.thenMany(receive).then(disconnected).then()
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

    fun getId(): String? {
        return session.id
    }

    fun send(message: String): Mono<Void> {
        return if (webSocketConnected) {
            session.send(Mono.just(session.textMessage(message)))
                    .doOnError(ClosedChannelException::class.java) { connectionClosed() }
                    .doOnError(AbortedException::class.java) { connectionClosed() }
                    .onErrorResume(ClosedChannelException::class.java) { Mono.empty() }
                    .onErrorResume(AbortedException::class.java) { Mono.empty() }
        } else Mono.empty()
    }

    fun connectionClosed() {
        if (webSocketConnected) {
            webSocketConnected = false
            session.close()
        }
    }
}