package com.mt.mtuser.common

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.lang.IllegalStateException
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Created by gyh on 2019/8/18.
 */
object SmsSample {
    data class VerificationCode(val code: Int, val msg: String)

    suspend fun send(phone: String, code: String): VerificationCode {
        val testContent = "【泰斯特科技】你的短信验证码为" + code + "验证码5分钟内有效！"
        val result = sendMessage(phone, testContent)
        val msg =  when (result) {
            "0" -> "成功"
            "30" -> "错误密码"
            "40" -> "账号不存在"
            "41" -> "余额不足"
            "43" -> "IP地址限制"
            "50" -> "内容含有敏感词"
            "51" -> "手机号码不正确"
            else -> "失败"
        }
        return VerificationCode(result.toInt(), msg)
    }

    private suspend fun sendNote(httpUrl: String, httpArg: String) :String {
        val client = WebClient.create("$httpUrl?$httpArg")
        return client.get()
                .accept(MediaType.ALL)
                .exchange()
                .flatMap { it.bodyToMono(String::class.java) }
                .awaitSingle()
    }

    private suspend fun sendMessage(phone: String, content: String): String {
        val username = "zelfly" //在短信宝注册的用户名
        val password = "zhengfei737218" //在短信宝注册的密码
        val httpUrl = "https://api.smsbao.com/sms"
        val httpArg = "u=" + username + "&" +
                "p=" + md5(password) + "&" +
                "m=" + phone + "&" +
                "c=" + encodeUrlString(content, "UTF-8")
        val result = sendNote(httpUrl, httpArg)
        println(result)
        return result
    }

    private fun md5(plainText: String): String {
        val buf = StringBuilder("")
        try {
            val md = MessageDigest.getInstance("MD5")
            md.update(plainText.toByteArray())
            val b = md.digest()
            var i: Int
            for (value in b) {
                i = value.toInt()
                if (i < 0) i += 256
                if (i < 16) buf.append("0")
                buf.append(Integer.toHexString(i))
            }
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return buf.toString()
    }

    private fun encodeUrlString(str: String?, charset: String): String? {
        var strret: String? = null
        if (str != null) {
            try {
                strret = URLEncoder.encode(str, charset)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return strret
    }
}