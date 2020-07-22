package com.mt.mtgateway.config

import com.mt.mtgateway.bean.RespBody
import com.mt.mtgateway.bean.User
import com.mt.mtgateway.server.MyReactiveUserDetailsService
import com.mt.mtgateway.server.RedisService
import com.mt.mtgateway.token.Constant
import com.mt.mtgateway.token.TokenManger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono


/**
 * Created by gyh on 2020/3/15.
 */
@Configuration
class AuthHandler {
    val log = LoggerFactory.getLogger(this.javaClass.simpleName)!!

    @Autowired
    private lateinit var userRepository: MyReactiveUserDetailsService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var redisService: RedisService

    @Autowired
    private lateinit var tokenManger: TokenManger

    fun login(request: ServerRequest): Mono<ServerResponse> {
        val body: Mono<Map<*, *>> = request.bodyToMono(MutableMap::class.java)
        return body.flatMap {
            log.info(it.toString())
            val username = it["phone"].toString()
            val password = it["password"].toString()
            return@flatMap userRepository.findByUsername(username).flatMap { user ->
                val resp: RespBody<User> = if (passwordEncoder.matches(password, user.password)) {
                    user.password = null
                    user.toke = tokenManger.createToken(user.id!!)
                    RespBody(0, "成功", user)
                } else RespBody(1, "无效凭据")
                ServerResponse.ok().contentType(APPLICATION_JSON)
                        .body(BodyInserters.fromValue(resp))
                        .doOnSubscribe { resp.data?.let { u -> redisService.setUserToken(u, Constant.JWT_TTL) }?.subscribe() }
            }.switchIfEmpty(ServerResponse.ok().contentType(APPLICATION_JSON)
                    .body(BodyInserters.fromValue(RespBody<Void>(1, "用户不存在"))))
        }
    }

}