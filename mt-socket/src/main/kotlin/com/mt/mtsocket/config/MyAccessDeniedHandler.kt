package com.mt.mtsocket.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mt.mtsocket.entity.ResponseInfo
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

/**
 * Created by gyh on 2020/3/18.
 */
class MyAccessDeniedHandler : ServerAccessDeniedHandler {
    val logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    override fun handle(exchange: ServerWebExchange, denied: AccessDeniedException): Mono<Void> {
        logger.info("权限拒绝")
        val resp = exchange.response
        val mono  = Mono.fromCallable{
            resp.statusCode = HttpStatus.UNAUTHORIZED
            resp.headers.add("Content-Type", "application/json;charset=UTF-8")
            val returnStr = jacksonObjectMapper().writeValueAsString(ResponseInfo<Unit>(1, "没有权限访问"))
            resp.bufferFactory().wrap(returnStr.toByteArray(StandardCharsets.UTF_8))
        }
        return resp.writeWith(mono)
    }
}