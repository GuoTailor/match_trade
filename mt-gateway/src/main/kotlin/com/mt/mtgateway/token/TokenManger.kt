package com.mt.mtgateway.token

import org.springframework.stereotype.Service
import java.lang.StringBuilder
import java.security.MessageDigest
import java.util.*

/**
 * Created by gyh on 2020/7/9
 */
@Service
class TokenManger {

    fun createToken(id: Int): String {
        val time = System.currentTimeMillis()
        val strToken = "${time + Constant.JWT_TTL}:$id:${Constant.JWT_SECRET}"
        val m = MessageDigest.getInstance("MD5")
        m.update(strToken.toByteArray())
        val sb = StringBuilder()
        m.digest().forEach { sb.append(it) }
        sb.append(":")
        sb.append(id)
        sb.append(":")
        sb.append(time)
        return sb.toString()
    }

    fun parseToken(token: String): TokenInfo {
        val ts = token.trim().split(":")
        if (ts.size == 3) {
            return TokenInfo(ts[1].toInt(), Date(ts[2].toLong() + Constant.JWT_TTL))
        } else error("不支持的token")
    }
}