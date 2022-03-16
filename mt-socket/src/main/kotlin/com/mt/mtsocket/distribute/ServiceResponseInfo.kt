package com.mt.mtsocket.distribute

import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/4/16.
 */
class ServiceResponseInfo (var data: Mono<*> = Mono.empty<Any>(), var req: Int, var order: Int) {

    fun getMono(): Mono<DataResponse> {
        return data.map { DataResponse(it, req, order) }
    }

    data class DataResponse(val data: Any, val req: Int, val order: Int)
}
