package com.mt.mtuser.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mt.mtuser.entity.Stockholder
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
    private val json = jacksonObjectMapper()

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return Mono.justOrEmpty(exchange).map {
            val request: ServerHttpRequest = exchange.request
            val id: String? = request.headers.getFirst("id")
            val role: String = request.headers.getFirst("roles") ?: "[]"
            val data: List<String> = json.readValue(role)
            val roles = data.map { list -> Stockholder().setName(list) }
            println(role)
            UsernamePasswordAuthenticationToken(id, id, roles)
        }
    }
}
