package com.mt.mtuser.config

import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebExchangeDecorator

/**
 * Created by gyh on 2020/8/3
 */
class PayloadServerWebExchangeDecorator(delegate: ServerWebExchange) : ServerWebExchangeDecorator(delegate) {
    private var requestDecorator = PartnerServerHttpRequestDecorator(delegate.request)

    override fun getRequest(): ServerHttpRequest {
        return requestDecorator;
    }

}