package com.mt.mtsocket.socket

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mt.mtsocket.distribute.ServiceResponseInfo
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.Disposable
import reactor.core.publisher.*
import reactor.core.scheduler.Schedulers
import reactor.netty.channel.AbortedException
import java.nio.channels.ClosedChannelException
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by gyh on 2020/4/7.
 */
class WebSocketSessionHandler {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val receiveProcessor: ReplayProcessor<String>
    private val connectedProcessor: MonoProcessor<WebSocketSession>
    private val disconnectedProcessor: MonoProcessor<WebSocketSession>
    private val json = jacksonObjectMapper()
    private var webSocketConnected = false
    private val session: WebSocketSession
    private val responseCount = AtomicInteger(1)
    private val responseMap = ConcurrentHashMap<Int, SendInfo>()
    private var retryCount = 3
    private var retryTimeout = 2L

    constructor(session: WebSocketSession) : this(50, session)

    constructor(historySize: Int, session: WebSocketSession) {
        receiveProcessor = ReplayProcessor.create(historySize)
        connectedProcessor = MonoProcessor.create()
        disconnectedProcessor = MonoProcessor.create()
        webSocketConnected = true
        val javaTimeModule = JavaTimeModule()
        javaTimeModule.addSerializer(LocalDateTime::class.java, object : JsonSerializer<LocalDateTime>() {
            override fun serialize(value: LocalDateTime, gen: JsonGenerator, serializers: SerializerProvider) {
                val timestamp = value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                gen.writeNumber(timestamp)
            }
        })
        json.registerModule(javaTimeModule)
        this.session = session
    }

    fun handle(): Mono<Void> {
        return session.receive()
                .map { obj -> obj.payloadAsText }
                .doOnNext { t -> receiveProcessor.onNext(t) }
                .doOnComplete { connectionClosed().subscribe() }
                .doOnCancel { connectionClosed().subscribe() }
                .doOnRequest {
                    webSocketConnected = true
                    connectedProcessor.onNext(session)
                }.then()
    }

    fun connected(): Mono<WebSocketSession> {
        return connectedProcessor
                .doOnError { logger.info("错误 {}", it.message) }
    }

    fun disconnected(): Mono<WebSocketSession> = disconnectedProcessor

    fun isConnected(): Boolean = webSocketConnected

    fun receive(): Flux<String> = receiveProcessor

    fun getId(): String = session.id

    fun getSession() = session

    fun send(message: String): Mono<String> {
        return if (webSocketConnected) {
            session.send(Mono.just(session.textMessage(message)))
                    .onErrorResume(ClosedChannelException::class.java) { connectionClosed() }
                    .onErrorResume(AbortedException::class.java) { connectionClosed() }
                    .doOnError { logger.info("send error ${it.message}") }
                    .then(Mono.just(message))
        } else Mono.empty()
    }

    fun send(message: String, req: Int, confirm: Boolean): Mono<String> {
        if (confirm) {
            val processor = EmitterProcessor.create<Int>(8)
            val cycle = Flux.interval(Duration.ofSeconds(retryTimeout), Schedulers.elastic())
                    .map {
                        if (it > retryCount) reqIncrement(req)
                        processor.onNext(it.toInt() + 1)
                        it
                    }.subscribe()
            val info = SendInfo(req, processor, cycle)
            responseMap[req] = info
            processor.onNext(0)
            return processor.flatMap {
                if (false != responseMap[req]?.ack || it > retryCount) {
                    responseMap.remove(req)
                    cycle.dispose()
                    processor.onComplete()
                    Mono.just(message)
                } else {
                    send(message)
                }
            }.last()
        }
        return send(message)
    }

    fun <T> send(data: Mono<T>, order: Int, confirm: Boolean = false): Mono<String> {
        val req = responseCount.getAndIncrement()
        return ServiceResponseInfo(data, req, order).getMono()
                .map { json.writeValueAsString(it) }
                .flatMap { send(it, req, confirm) }
    }

    fun connectionClosed(): Mono<Void> {
        webSocketConnected = false
        receiveProcessor.onComplete()
        disconnectedProcessor.onNext(session)
        return session.close()
    }

    fun reqIncrement(req: Int) {
        val value = responseMap[req]
        if (value != null) {
            value.ack = true
            value.cycle.dispose()
            value.processor.onComplete()
            logger.info("取消 {} ", req)
            responseMap.remove(req)
        }
    }

    data class SendInfo(val req: Int,
                        val processor: FluxProcessor<Int, Int>,
                        val cycle: Disposable,
                        var ack: Boolean = false
    )
}