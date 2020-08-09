package com.mt.mtuser.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator


/**
 * Created by gyh on 2020/8/3
 */
class PartnerServerHttpRequestDecorator(delegate: ServerHttpRequest) : ServerHttpRequestDecorator(delegate) {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    init {
        val path = delegate.uri.path
        val query = delegate.uri.query
        val method = delegate.method?.name ?: HttpMethod.GET.name
        logger.info("""
            
            HttpMethod : {}
            Uri        : {}
            """.trimIndent(), method, path + if (query == null) "" else "?$query")
    }

}