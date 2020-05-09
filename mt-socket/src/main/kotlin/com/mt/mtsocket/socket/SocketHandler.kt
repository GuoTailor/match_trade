package com.mt.mtsocket.socket

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mt.mtcommon.RoomRecord
import com.mt.mtsocket.distribute.DispatcherServlet
import com.mt.mtsocket.distribute.ServiceRequestInfo
import com.mt.mtsocket.distribute.ServiceResponseInfo
import com.mt.mtsocket.entity.BaseUser
import com.mt.mtsocket.entity.ResponseInfo
import com.mt.mtsocket.service.RedisUtil
import com.mt.mtsocket.service.WorkService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.StringUtils
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Mono
import java.util.*

/**
 * Created by gyh on 2020/4/5.
 */
@WebSocketMapping("/room")
class SocketHandler : WebSocketHandler {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val json = jacksonObjectMapper()
    private val blankRegex = "\\s".toRegex()
    private val orderRegex = "\"order\":(.*?)[,}]".toRegex()
    private val dataRegex = "\"data\":(.*?})[,}]".toRegex()
    private val reqRegex = "\"req\":(.*?)[,}]".toRegex()

    @Autowired
    private lateinit var dispatcherServlet: DispatcherServlet

    @Autowired
    private lateinit var workService: WorkService

    @Autowired
    private lateinit var redisUtil: RedisUtil

    init {
        json.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    override fun handle(session: WebSocketSession): Mono<Void> {
        val sessionHandler = WebSocketSessionHandler(session)
        val watchDog = WebSocketWatchDog().start(sessionHandler, 3000)
        val queryMap = getQueryMap(sessionHandler.getSession().handshakeInfo.uri.query)
        if (queryMap["roomId"] == null) {
            return sessionHandler.send("错误，不支持的参数列表$queryMap")
                    .then(sessionHandler.connectionClosed())
        }
        val roomId = queryMap["roomId"].toString()
        val connect = sessionHandler.connected()
                .flatMap { workService.enterRoom(roomId) }
                .onErrorResume {
                    ServiceResponseInfo(ResponseInfo.failed("错误: ${it.message}"), -1).getMono()
                            .map { data -> json.writeValueAsString(data) }
                            .flatMap(sessionHandler::send)
                            .flatMap { Mono.empty<RoomRecord>() }
                }
                .flatMap { SocketSessionStore.addUser(sessionHandler, it) }
                .flatMap { sessionHandler.disconnected() }
                .flatMap { BaseUser.getcurrentUser() }
                .doOnNext { SocketSessionStore.removeUser(it.id!!) }

        val output = sessionHandler.receive()
                .flatMap {
                    logger.info("接收到数据${it}")
                    val request = toServiceRequestInfo(it)
                    val resp = ServiceResponseInfo(req = request.req)
                    dispatcherServlet.doDispatch(request, resp)
                    resp.getMono()
                }.onErrorResume { ServiceResponseInfo(ResponseInfo.failed("错误 ${it.message}"), -1).getMono() }
                .map { json.writeValueAsString(it) }
                .doOnError { logger.info("错误") }
                .flatMap(sessionHandler::send)
                .then()

        return sessionHandler.handle()
                .zipWith(connect)
                .zipWith(watchDog)
                .zipWith(output).then()
    }

    private fun getQueryMap(queryStr: String): Map<String, String> {
        val queryMap: MutableMap<String, String> = HashMap()
        if (!StringUtils.isEmpty(queryStr)) {
            val queryParam = queryStr.split("&")
            queryParam.forEach { s: String ->
                val kv = s.split("=".toRegex(), 2)
                val value = if (kv.size == 2) kv[1] else ""
                queryMap[kv[0]] = value
            }
        }
        return queryMap
    }

    private fun toServiceRequestInfo(data: String): ServiceRequestInfo {
        val json = data.replace(blankRegex, "")
        val orderString = orderRegex.find(json)!!.groups[1]!!.value.replace("\"", "")
        val dataString = dataRegex.find(json)?.groups?.get(1)?.value
        val reqString = reqRegex.find(json)!!.groups[1]!!.value.toInt()
        return ServiceRequestInfo(orderString, dataString, reqString)
    }

}
