package com.mt.mtuser.entity

import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toMono
import java.io.Serializable


/**
 * Created by gyh on 2020/3/18.
 */
val logger = LoggerFactory.getLogger(ResponseInfo::class.java)

class ResponseInfo<T>(var code: Int, var msg: String) : Serializable {

    var data: T? = null

    constructor(code: Int, msg: String, data: T) : this(code, msg) {
        this.data = data
    }

    companion object {
        @JvmStatic
        fun <T> ok(monoBody: Mono<T>): Mono<ResponseInfo<T>> {
            return responseBodyCreate(monoBody, 0, "成功")
        }

        @JvmStatic
        fun <T> ok(monoBody: Mono<T>, msg: String): Mono<ResponseInfo<T>> {
            return responseBodyCreate(monoBody, 0, msg)
        }

        @JvmStatic
        fun <T> ok(monoBody: Mono<T>, code: Int, msg: String): Mono<ResponseInfo<T>> {
            return responseBodyCreate(monoBody, code, msg)
        }

        @JvmStatic
        fun <T> failed(monoBody: Mono<T>): Mono<ResponseInfo<T>> {
            return responseBodyCreate(monoBody, 1, "失败")
        }

        @JvmStatic
        fun <T> failed(monoBody: Mono<T>, msg: String): Mono<ResponseInfo<T>> {
            return responseBodyCreate(monoBody, 1, msg)
        }

        @JvmStatic
        fun <T> failed(monoBody: Mono<T>, code: Int, msg: String): Mono<ResponseInfo<T>> {
            return responseBodyCreate(monoBody, code, msg)
        }

        @JvmStatic
        fun <T> responseBodyCreate(monoData: Mono<T>, code: Int, msg: String): Mono<ResponseInfo<T>> {
            return monoData.map { data: T ->
                val responseInfo = ResponseInfo<T>(code, msg)
                responseInfo.data = data
                responseInfo
            }.switchIfEmpty {
                Mono.just(ResponseInfo(code, msg))
            }.onErrorResume {
                it.printStackTrace()
                val responseInfo = ResponseInfo<T>(1, it.message ?: "失败")
                logger.error(it.message)
                responseInfo.toMono()
            }
        }

    }
}