package com.mt.mtgateway.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RequestPredicates.POST
import org.springframework.web.reactive.function.server.RequestPredicates.accept
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions


/**
 * Created by gyh on 2020/3/15.
 */
@Configuration
class BeanConfig {

    @Autowired
    private lateinit var authHandler: AuthHandler

    @Bean
    fun authRoute(): RouterFunction<*> {
        return RouterFunctions
                .route(POST("/api/login").and(accept(APPLICATION_JSON))) { authHandler.login(it) }

    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }


}
