package com.mt.mtsocket.config.socket

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mt.mtsocket.distribute.DispatcherServlet
import com.mt.mtsocket.distribute.ServiceRequestInfo
import com.mt.mtsocket.distribute.ServiceResponseInfo
import com.mt.mtsocket.entity.BaseUser
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.StringUtils
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.HashMap

/**
 * Created by gyh on 2020/4/5.
 * 这个架构一点也不好用
 */
@WebSocketMapping("/room")
class ExampleHandler : WebSocketHandler {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val json = jacksonObjectMapper()

    @Autowired
    private lateinit var dispatcherServlet: DispatcherServlet

    override fun handle(session: WebSocketSession): Mono<Void> {
        val sessionHandler = WebSocketSessionHandler(session)
        val watchDog = WebSocketWatchDog().start(sessionHandler, 3000)
        val queryMap = getQueryMap(sessionHandler.getSession().handshakeInfo.uri.query)
        if (queryMap["roomId"] == null) {
            return sessionHandler.send("错误，不支持的参数列表$queryMap")
                    .then(sessionHandler.connectionClosed())
        }
        val connect = sessionHandler.connected()
                .flatMap { SocketSessionStore.addUser(it, queryMap["roomId"].toString()) }
                .flatMap { sessionHandler.disconnected() }
                .flatMap { BaseUser.getcurrentUser() }
                .doOnNext { SocketSessionStore.removeUser(it.id!!) }
                .flatMapMany { sessionHandler.receive() }
                .flatMap {
                    val request = json.readValue<ServiceRequestInfo>(it)
                    val resp = ServiceResponseInfo(req = request.req)
                    dispatcherServlet.doDispatch(request, resp)
                    logger.info("接收到数据$it")
                    resp.getMono()
                }.map { json.writeValueAsString(it) }
                .flatMap(sessionHandler::send)
                .ignoreElements()

        return sessionHandler.handle()
                .zipWith(connect)
                .zipWith(watchDog).then()
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

}
