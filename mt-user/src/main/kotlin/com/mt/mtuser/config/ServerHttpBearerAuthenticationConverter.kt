package com.mt.mtuser.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mt.mtuser.entity.Role
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.function.Function
import java.util.stream.Collectors

/**
 * Created by gyh on 2020/3/16.
 */

class ServerHttpBearerAuthenticationConverter : ServerAuthenticationConverter {
    private val json = jacksonObjectMapper()

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return Mono.justOrEmpty(exchange).map<Authentication> {
            val request: ServerHttpRequest = exchange.request
            val id: String? = request.headers.getFirst("id")
            //val username: String? = request.headers.getFirst("username")
            val role: String = request.headers.getFirst("roles") ?: "[]"
            val data: List<List<Any>> = json.readValue(role)
            val roles = data.map { list ->
                val r = Role()
                r.name = "ROLE_" + list[0]
                r.companyid = list[1] as? Int
                r
            }
            UsernamePasswordAuthenticationToken(id, id, roles)
        }
    }
}
