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

    @Test
    fun testRegex() {
        val data = "{\"order\":\"/echo\", \"data\": {\"value\": \"123\"}, \"req\":12}"
        val blankRegex = "\\s".toRegex()
        val orderRegex = "\"order\":(.*?)[,}]".toRegex()
        val dataRegex = "\"data\":(.*?})[,}]".toRegex()
        val reqRegex = "\"req\":(.*?)[,}]".toRegex()

        val json = data.replace(blankRegex, "")
        val orderString = orderRegex.find(json)!!.groups[1]!!.value.replace("\"", "")
        val dataString = dataRegex.find(json)?.groups?.get(1)?.value
        val reqString = reqRegex.find(json)!!.groups[1]!!.value.toInt()
        println("$orderString $dataString, $reqString")
    }

}
