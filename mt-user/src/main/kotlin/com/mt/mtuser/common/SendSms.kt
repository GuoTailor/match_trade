package com.mt.mtuser.common

import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.Connection
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.TcpClient
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by gyh on 2020/4/26.
 */
object SendSms {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)
    private val json = ObjectMapper()
    private const val DEF_CONN_TIMEOUT = 5000
    private const val DEF_READ_TIMEOUT = 5000L
    private const val userAgent = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36"
    private const val appKey = "9bb3098df4c6adc174d89cee32e6e729"

    data class VerificationCode(val code: String, val msg: String?)

    suspend fun send(phone: String, code: String, m: Int): VerificationCode {
        val url = "http://v.juhe.cn/sms/send" //请求接口地址
        val params = HashMap<String, String>() //请求参数
        params["mobile"] = phone //接受短信的用户手机号码
        params["tpl_id"] = "214513" //您申请的短信模板ID，根据实际情况修改
        params["tpl_value"] = "#code#=$code&#m#=$m" //您设置的模板变量，根据实际情况修改
        params["key"] = appKey //应用APPKEY(应用详细页查询)
        params["dtype"] = "json" //应用APPKEY(应用详细页查询)
        try {
            val result = net(url, params)
            val data = json.readValue(result, Map::class.java)
            logger.info("$phone = $code : $result")
            return VerificationCode(data["error_code"].toString(), data["reason"].toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return VerificationCode("205402", "错误")
    }

    private suspend fun net(strUrl: String, params: Map<String, String>): String {
        val tcpClient = TcpClient
                .create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, DEF_CONN_TIMEOUT)
                .doOnConnected { connection: Connection ->
                    connection.addHandlerLast(ReadTimeoutHandler(DEF_READ_TIMEOUT, TimeUnit.MILLISECONDS))
                    connection.addHandlerLast(WriteTimeoutHandler(DEF_READ_TIMEOUT, TimeUnit.MILLISECONDS))
                }
        val client = WebClient.builder()
                .clientConnector(ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .baseUrl("$strUrl?${urlEncode(params)}")
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .build()
        return client.get()
                .accept(MediaType.ALL)
                .exchange()
                .flatMap { it.bodyToMono(String::class.java) }
                .awaitSingle()
    }

    //将map型转为请求参数型
    private fun urlEncode(data: Map<String, String>): String {
        val sb = StringBuilder()
        try {
            for ((key, value) in data) {
                sb.append(key).append("=").append(URLEncoder.encode(value + "", "UTF-8")).append("&")
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return sb.toString()
    }
}
