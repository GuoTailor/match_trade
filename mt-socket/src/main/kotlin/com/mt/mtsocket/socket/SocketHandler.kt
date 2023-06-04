package com.mt.mtsocket.socket

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mt.mtcommon.getJavaTimeModule
import com.mt.mtsocket.common.NotifyOrder
import com.mt.mtsocket.common.Util
import com.mt.mtsocket.distribute.DispatcherServlet
import com.mt.mtsocket.distribute.ServiceRequestInfo
import com.mt.mtsocket.distribute.ServiceResponseInfo
import com.mt.mtsocket.entity.ResponseInfo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.TimeoutException

/**
 * Created by gyh on 2020/5/19.
 */
abstract class SocketHandler : WebSocketHandler {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val json = jacksonObjectMapper()

    @Autowired
    private lateinit var dispatcherServlet: DispatcherServlet

    init {
        json.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        json.registerModule(getJavaTimeModule())
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        val sessionHandler = SessionHandler()
        sessionHandler.setSessionId(session.id)
        val queryMap = Util.getQueryMap(session.handshakeInfo.uri.query)
        val input = session.receive()
            .timeout(Duration.ofMillis(8000))
            .doOnError(TimeoutException::class.java) { logger.info("超时 " + session.id) }
            .map { it.payloadAsText }
            .map(::toServiceRequestInfo)
            .filter { it.order != "/ping" }
            .filter { filterConfirm(it, sessionHandler) }
            .doOnNext(::printLog)
            .flatMap {
                val resp = ServiceResponseInfo(req = it.req, order = NotifyOrder.requestReq)
                dispatcherServlet.doDispatch(it, resp)
                logger.info("{}", resp.data)
                resp.getMono()
                    .map { d -> logger.info("响应 {}", d.data.toString()); d }
                    .onErrorResume { e ->
                    logger.info("错误 {}", e.message)
                    ServiceResponseInfo(
                        ResponseInfo.failed("错误 ${e.message}"),
                        NotifyOrder.errorNotify,
                        NotifyOrder.requestReq
                    ).getMono()
                }
            }
            .map { sessionHandler.send(it, true) }
            .doOnTerminate { onDisconnected(queryMap, sessionHandler); sessionHandler.tryEmitComplete() }
            .then()//.log()
        val output = session.send(sessionHandler.asFlux()
            .doOnError { logger.info("错误 {}", it.message) }
            .map { session.textMessage(it) })
        val onCon = onConnect(queryMap, sessionHandler)
            .flatMap { sessionHandler.send(ResponseInfo.ok<Unit>("连接成功"), NotifyOrder.connectSucceed, true) }
        return Mono.zip(onCon, input, output).then()
    }

    /**
     * 当socket连接时
     */
    abstract fun onConnect(queryMap: Map<String, String>, sessionHandler: SessionHandler): Mono<*>

    /**
     * 当socket断开连接时
     */
    abstract fun onDisconnected(queryMap: Map<String, String>, sessionHandler: SessionHandler)

    private fun toServiceRequestInfo(data: String): ServiceRequestInfo {
        return this.json.readValue(data)
    }

    private fun printLog(info: ServiceRequestInfo): ServiceRequestInfo {
        if (info.order != "/echo")
            logger.info("接收到数据order:{} req:{} data:{}", info.order, info.req, info.data)
        return info
    }

    private fun filterConfirm(info: ServiceRequestInfo, sessionHandler: SessionHandler): Boolean {
        if (info.order == "/ok") {
            sessionHandler.reqIncrement(info.req)
            return false
        }
        return true
    }
}
