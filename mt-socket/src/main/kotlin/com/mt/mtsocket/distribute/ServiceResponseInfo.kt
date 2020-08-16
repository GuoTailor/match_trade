package com.mt.mtsocket.distribute

import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/4/16.
 */
class ServiceResponseInfo (var data: Mono<*>? = null, var req: Int, var order: Int) {

    fun getMono(): Mono<DataResponse> {
        val resp = data ?: Mono.empty<Any>()
        return resp.map { DataResponse(it, req, order) }
    }

    data class DataResponse(val data: Any, val req: Int, val order: Int)
}