package com.mt.mtsocket.socket

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter


/**
 * Created by gyh on 2020/4/5.
 */
@Configuration
class WebSocketConfig {

    @Bean
    fun handlerAdapter(): WebSocketHandlerAdapter? {
        return WebSocketHandlerAdapter()
    }
}
