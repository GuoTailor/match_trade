package com.mt.mtgateway.config

import com.mt.mtgateway.token.TokenMgr
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

/**
 * Created by gyh on 2020/3/16.
 */
@Component
class CustomGlobalFilter : WebFilter, Ordered {
    val log = LoggerFactory.getLogger(this.javaClass.simpleName)
    val TOKEN_PREFIX = "Bearer "
    val skipAuthUrls = arrayOf("/login", "/user/register", "/user/login")


    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val url = exchange.request.path.value()
        log.info(url + "    " + exchange.request.uri.path)
        if (!skipAuthUrls.contains(url)) {
            val request = exchange.request
            val authHeader: String? = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
            if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
                val authToken = authHeader.replaceFirst(TOKEN_PREFIX, "")
                val checkPOJO = TokenMgr.validateJWT(authToken)
                if (checkPOJO.isSuccess) {
                    val claims = checkPOJO.claims
                    val id = claims["id"]
                    val username = claims["username"].toString()
                    val authorities = claims["role"].toString()
                    log.info("验证 $username : $authorities")
                    val host = exchange.request.mutate()
                            .header("id", id.toString())
                            .header("username", username)
                            .header("role", authorities)
                            .header("Authorization")
                            .build()
                    val build = exchange.mutate().request(host).build()
                    return chain.filter(build)
                }
                return authErro(exchange, "token 已失效")
            }
            return authErro(exchange, "无权访问")
        }
        return chain.filter(exchange)
    }

    /**
     * 认证错误输出
     * @param resp 响应对象
     * @param mess 错误信息
     * @return
     */
    private fun authErro(swe: ServerWebExchange, mess: String): Mono<Void> {
        log.info(mess)
        val resp = swe.response
        resp.statusCode = HttpStatus.UNAUTHORIZED
        resp.headers.add("Content-Type", "application/json;charset=UTF-8")
        val returnStr = "{" +
                "\"code\":1," +
                "\"msg\":\"$mess\"" +
                "}"
        val buffer = resp.bufferFactory().wrap(returnStr.toByteArray(StandardCharsets.UTF_8))
        return resp.writeWith(Mono.just(buffer))
    }

    override fun getOrder(): Int {
        return Ordered.LOWEST_PRECEDENCE
    }
}
