package com.mt.mtsocket.socket

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mt.mtcommon.toLocalDateTime
import com.mt.mtsocket.common.NotifyReq
import com.mt.mtsocket.common.Util
import com.mt.mtsocket.distribute.DispatcherServlet
import com.mt.mtsocket.distribute.ServiceRequestInfo
import com.mt.mtsocket.distribute.ServiceResponseInfo
import com.mt.mtsocket.entity.ResponseInfo
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Created by gyh on 2020/5/19.
 */
abstract class SocketHandler : WebSocketHandler {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val json = jacksonObjectMapper()
    private val blankRegex = "\\s".toRegex()
    private val orderRegex = "\"order\":(.*?)[,}]".toRegex()
    private val dataRegex = "\"data\":(.*?})[,}]".toRegex()
    private val reqRegex = "\"req\":(.*?)[,}]".toRegex()

    init {
        json.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val javaTimeModule = JavaTimeModule()
        javaTimeModule.addSerializer(LocalDateTime::class.java, object : JsonSerializer<LocalDateTime>() {
            override fun serialize(value: LocalDateTime, gen: JsonGenerator, serializers: SerializerProvider) {
                val timestamp = value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                gen.writeNumber(timestamp)
            }
        })
        javaTimeModule.addDeserializer(LocalDateTime::class.java, object : JsonDeserializer<LocalDateTime>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDateTime {
                val temp = p.valueAsLong
                return temp.toLocalDateTime()
            }
        })
        json.registerModule(javaTimeModule)
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        val sessionHandler = WebSocketSessionHandler(session)
        val watchDog = WebSocketWatchDog().start(sessionHandler, 5000)
        val queryMap = Util.getQueryMap(sessionHandler.getSession().handshakeInfo.uri.query)
        val connect = sessionHandler.connected().flatMap { onConnect(queryMap, sessionHandler) }
                .flatMap { sessionHandler.send(ResponseInfo.ok<Unit>("连接成功"), NotifyReq.connectSucceed) }
        val disconnected = sessionHandler.disconnected().flatMap { onDisconnected(queryMap, sessionHandler) }
        val output = sessionHandler.receive()
                .map {
                    val info = toServiceRequestInfo(it)
                    if (info.order != "/echo" && info.order != "/ping") logger.info("接收到数据${it}")
                    info
                }.filter { it.order != "/ping" }    // 心跳就不回应
                .flatMap {
                    val resp = ServiceResponseInfo(req = it.req)
                    getServlet().doDispatch(it, resp)
                    resp.getMono()
                }.onErrorResume { ServiceResponseInfo(ResponseInfo.failed("错误 ${it.message}"), NotifyReq.errorNotify).getMono() }
                .map { json.writeValueAsString(it) }
                .flatMap(sessionHandler::send)
                .doOnNext {
                    if (it.length > 1024) {
                        logger.info("send {}...{}...{}", it.substring(0, 512), it.length - 1024, it.substring(it.length - 512))
                    } else {
                        logger.info("send $it")
                    }
                }.then()

        return sessionHandler.handle()
                .zipWith(connect)
                .zipWith(watchDog)
                .zipWith(output)
                .zipWith(disconnected)
                .then()
    }

    abstract fun getServlet(): DispatcherServlet

    /**
     * 当socket连接时
     */
    abstract fun onConnect(queryMap: Map<String, String>, sessionHandler: WebSocketSessionHandler): Mono<*>

    /**
     * 当socket断开连接时
     */
    abstract fun onDisconnected(queryMap: Map<String, String>, sessionHandler: WebSocketSessionHandler): Mono<*>

    private fun toServiceRequestInfo(data: String): ServiceRequestInfo {
        // TODO 经测试正则表达式比jackson反序列化慢
        val json = data.replace(blankRegex, "")
        val orderString = orderRegex.find(json)!!.groups[1]!!.value.replace("\"", "")
        val dataString = dataRegex.find(json)?.groups?.get(1)?.value
        val reqString = reqRegex.find(json)!!.groups[1]!!.value.toInt()
        return ServiceRequestInfo(orderString, dataString, reqString)
    }
}