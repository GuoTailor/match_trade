package com.mt.mtgateway.config

import com.mt.mtgateway.server.RedisService
import com.mt.mtgateway.token.TokenManger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Pattern

/**
 * Created by gyh on 2020/3/16.
 */
@Component
class CustomGlobalFilter(@Value("\${skipAuthUrls}") val skipAuthUrls: List<String>) : WebFilter, Ordered {
    private val ALLOWED_HEADERS =
        "x-requested-with, authorization, Content-Type, Authorization, credential, X-XSRF-TOKEN,token,username,client"
    private val ALLOWED_METHODS = "*"
    private val ALLOWED_ORIGIN = "*"
    private val ALLOWED_Expose = "*"
    private val MAX_AGE = "18000L"
    val log = LoggerFactory.getLogger(this.javaClass.simpleName)!!
    val urlPatten: MutableList<Pattern> = mutableListOf()
    val TOKEN_PREFIX = "Bearer "

    @Autowired
    private lateinit var redisService: RedisService

    @Autowired
    private lateinit var tokenManger: TokenManger

    init {
        skipAuthUrls.forEach {
            urlPatten.add(Pattern.compile(it))
            log.info(it)
        }
    }

    fun match(input: CharSequence): Boolean {
        urlPatten.forEach {
            if (it.matcher(input).matches())
                return true
        }
        return false
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val url = request.path.value()
        val response: ServerHttpResponse = exchange.response
        val headers: HttpHeaders = response.headers
        headers.add("Access-Control-Allow-Origin", ALLOWED_ORIGIN)
        headers.add("Access-Control-Allow-Methods", ALLOWED_METHODS)
        headers.add("Access-Control-Max-Age", MAX_AGE)
        headers.add("Access-Control-Allow-Headers", ALLOWED_HEADERS)
        headers.add("Access-Control-Expose-Headers", ALLOWED_Expose)
        headers.add("Access-Control-Allow-Credentials", "true")

        log.info("{} >> {}?{}", request.headers.toString(), url, request.uri.query)
        if (!match(url) && request.method != HttpMethod.OPTIONS) {
            val authHeader = getAuthToken(request)
            //log.info(authHeader)
            if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
                val authToken = authHeader.replaceFirst(TOKEN_PREFIX, "")
                val tokenInfo = tokenManger.parseToken(authToken)
                return redisService.getUserByToken(tokenInfo.id)
                    .filter { it.toke == authToken }
                    .flatMap {
                        val host = request.mutate()
                            .header("id", it.id?.toString())
                            .header(
                                "roles",
                                it.roles.joinToString(prefix = "[", postfix = "]") { s -> "\"${s.name}\"" })
                            .header(HttpHeaders.AUTHORIZATION)
                            .build()
                        val build = exchange.mutate().request(host).build()
                        chain.filter(build)
                    }.switchIfEmpty {
                        authError(
                            exchange,
                            "登录已过期",
                            if (tokenInfo.time.before(Date())) HttpStatus.UNAUTHORIZED else HttpStatus.FORBIDDEN
                        )
                    }
            }
            return authError(exchange, "无权访问 $url")
        }
        return chain.filter(exchange)
    }

    private fun getAuthToken(request: ServerHttpRequest): String? {
        val headers = request.headers
        if (headers.getFirst(HttpHeaders.CONNECTION) == "Upgrade") {
            if (headers.getFirst(HttpHeaders.UPGRADE) == "websocket") {
                val queryMap = getQueryMap(request.uri.query)
                return TOKEN_PREFIX + queryMap[TOKEN_PREFIX.trim().lowercase(Locale.getDefault())]
            }
        }
        return headers.getFirst(HttpHeaders.AUTHORIZATION)
    }

    private fun getQueryMap(queryStr: String): Map<String, String> {
        val queryMap: MutableMap<String, String> = HashMap()
        if (StringUtils.hasLength(queryStr)) {
            val queryParam = queryStr.split("&")
            queryParam.forEach { s: String ->
                val kv = s.split("=".toRegex(), 2)
                val value = if (kv.size == 2) kv[1] else ""
                queryMap[kv[0]] = value
            }
        }
        return queryMap
    }

    /**
     * 认证错误输出
     * @param mess 错误信息
     * @return
     */
    private fun authError(
        swe: ServerWebExchange,
        mess: String,
        statusCode: HttpStatus = HttpStatus.UNAUTHORIZED
    ): Mono<Void> {
        val resp = swe.response
        resp.statusCode = statusCode
        //resp.headers.add("Content-Type", "application/json;charset=UTF-8")
        val returnStr = "{" +
                "\"code\":1," +
                "\"msg\":\"$mess\"" +
                "}"
        return resp.writeWith(Mono.defer {
            val buffer = resp.bufferFactory().wrap(returnStr.toByteArray(StandardCharsets.UTF_8))
            Mono.just(buffer)
        })
    }

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE
    }
}
