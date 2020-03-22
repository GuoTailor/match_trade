package com.mt.mtgateway.config

import com.mt.mtgateway.RespBody
import com.mt.mtgateway.User
import com.mt.mtgateway.server.MyReactiveUserDetailsService
import com.mt.mtgateway.token.TokenMgr
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

    fun login(request: ServerRequest): Mono<ServerResponse> {
        val body: Mono<Map<*, *>> = request.bodyToMono(MutableMap::class.java)
        return body.flatMap {
            log.info(it.toString())
            val username = it["phone"].toString()
            val password = it["password"].toString()
            return@flatMap userRepository.findByUsername(username).flatMap { user ->
                if (passwordEncoder.matches(password, user.password)) {
                    ServerResponse.ok()
                            .contentType(APPLICATION_JSON)
                            .body(BodyInserters.fromValue(RespBody<String>(0, "成功", TokenMgr.createJWT(user as User))))
                            // TODO 包装阻塞代码
                } else {
                    ServerResponse.badRequest()
                            .body(BodyInserters.fromValue(RespBody<Void>(1, "Invalid credentials")))
                }
            }.switchIfEmpty(ServerResponse.badRequest()
                    .body(BodyInserters.fromValue(RespBody<Void>(1, "User does not exist"))))
        }
    }
}