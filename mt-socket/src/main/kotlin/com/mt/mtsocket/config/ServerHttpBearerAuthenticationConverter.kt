package com.mt.mtsocket.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mt.mtsocket.entity.Role
import org.slf4j.LoggerFactory
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/3/16.
 */

class ServerHttpBearerAuthenticationConverter : ServerAuthenticationConverter {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val json = jacksonObjectMapper()

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return Mono.justOrEmpty(exchange).map<Authentication> {
            val request: ServerHttpRequest = exchange.request
            val id: String? = request.headers.getFirst("id")
            val role: String = request.headers.getFirst("roles") ?: "[]"
            val data: List<String> = json.readValue(role)
            val roles = data.map { list -> Role(name = "ROLE_$list") }
            //logger.info("验证 id:{} {} {}", id, roles, request.headers.toString())
            UsernamePasswordAuthenticationToken(id, id, roles)
        }
    }
}
