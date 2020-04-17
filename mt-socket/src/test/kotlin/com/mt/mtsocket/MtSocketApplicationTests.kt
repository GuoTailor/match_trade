package com.mt.mtsocket

import com.mt.mtsocket.config.socket.SocketSessionStore
import com.mt.mtsocket.entity.OrderParam
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

//@SpringBootTest
class MtSocketApplicationTests {

    @Test
    fun contextLoads() {
        val set = ArrayList<OrderParam>()
        set.add(OrderParam(2, time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2018-01-01 10:12:56")))
        set.removeIf { it.time == SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2018-01-01 10:12:56") }
        set.add(OrderParam(2))
        set.forEach { println(it) }
        val nmka = Mono.just(OrderParam(2))
        nmka.map { SocketSessionStore.getRoom(it.id!!) }
                .filter { it != null }
                .switchIfEmpty(Mono.just("nmka"))
                .map { println(it) }
                .subscribe()

    }

}
