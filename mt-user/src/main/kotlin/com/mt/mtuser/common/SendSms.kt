package com.mt.mtuser.common

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient
import java.io.UnsupportedEncodingException
import java.util.concurrent.TimeUnit


/**
 * Created by gyh on 2020/4/26.
 */
object SendSms {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    private const val DEF_CONN_TIMEOUT = 5000
    private const val DEF_READ_TIMEOUT = 5000L
    private const val appKey = "b077b8e7ee064924895a182a225fb89b"
    private const val url = "http://api.smsbao.com/sms"
    private val resultMap = mapOf(
        0 to "成功",
        40 to "账号不存在",
        41 to "余额不足",
        43 to "IP地址限制",
        50 to "内容含有敏感词",
        51 to "手机号码不正确",
    )

    data class VerificationCode(val code: String, val msg: String?)

    suspend fun send(phone: String, code: String, time: Int): VerificationCode {
        val testContent = "【企拍拍】您的验证码是${code}。有效期为${time}分钟，请尽快验证。"
        val params = mapOf("u" to "zelfly", "p" to appKey, "m" to phone, "c" to testContent)
        try {
            val result = net(url, params)
            logger.info("$phone = $code : ${resultMap[result]}")
            return VerificationCode(result.toString(), resultMap[result])
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return VerificationCode("205402", "错误")
    }


    private suspend fun net(strUrl: String, params: Map<String, String>): Int {
        val tcpClient = HttpClient
            .create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEF_CONN_TIMEOUT)
            .doOnConnected { connection: Connection ->
                connection.addHandlerLast(ReadTimeoutHandler(DEF_READ_TIMEOUT, TimeUnit.MILLISECONDS))
                connection.addHandlerLast(WriteTimeoutHandler(DEF_READ_TIMEOUT, TimeUnit.MILLISECONDS))
            }

        val client = WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(tcpClient))
            .baseUrl("$strUrl?${urlEncode(params)}")
            .build()
        return client.get()
            .accept(MediaType.ALL)
            .exchangeToMono { response ->
                if (response.statusCode().equals(HttpStatus.OK)) {
                    response.bodyToMono(String::class.java)
                } else {
                    response.createException().flatMap { Mono.error(it) }
                }
            }
            .map { it.toInt() }
            .awaitSingle()
    }

    //将map型转为请求参数型
    private fun urlEncode(data: Map<String, String>): String {
        val sb = StringBuilder()
        try {
            for ((key, value) in data) {
                sb.append(key).append("=").append(value).append("&")
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return sb.toString()
    }
}
