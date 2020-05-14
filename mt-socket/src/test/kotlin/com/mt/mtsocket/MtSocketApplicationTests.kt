package com.mt.mtsocket

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mt.mtcommon.OrderParam
import com.mt.mtsocket.service.RedisUtil
import com.mt.mtsocket.socket.SocketSessionStore
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


//@SpringBootTest
class MtSocketApplicationTests {
    @Autowired
    lateinit var redisUtil: RedisUtil

    @Autowired
    lateinit var redisConnectionFactory: LettuceConnectionFactory

    @Test
    fun contextLoads() {
        Mono.just("nmka")
                .flatMap { println("nmka2"); Mono.just("") }
                .then()
                .doOnEach { println("cnm$it") }
                .log().block()
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

    @Test
    fun testRedisTemp() {
        val om = ObjectMapper()
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        //om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        val ptv = BasicPolymorphicTypeValidator.builder().build()
        om.activateDefaultTyping(ptv)
        om.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)
        val jsonString = om.writeValueAsString(OrderParam(userId = 1, roomId = "d11"))
        println(jsonString)
    }

    @Test
    fun testSerialization() {
        val jackson2JsonRedisSerializer = GenericJackson2JsonRedisSerializer()
        val stringRedisSerializer = StringRedisSerializer()

        val context = RedisSerializationContext.newSerializationContext<String, Any>()
                .key(stringRedisSerializer)             // key采用String的序列化方式
                .value(jackson2JsonRedisSerializer)     // value序列化方式采用jackson
                .hashKey(stringRedisSerializer)         // hash的key也采用String的序列化方式
                .hashValue(jackson2JsonRedisSerializer) // hash的value序列化方式采用jackson
                .build()
        val value = OrderParam(userId = 1, roomId = "d11")
        val serialization = context.valueSerializationPair
        val data = serialization.write(value)
        val dataString = String(data.array())
        println(dataString)
        val order = context.valueSerializationPair.read(data)
        println(order.toString())
        var om = ObjectMapper()
        om = om.deactivateDefaultTyping()
        val testOrder = om.readValue("{\"id\":12,\"price\":null,\"roomId\":\"d11\",\"number\":100,\"buy\":null}", OrderParam::class.java)
        println(testOrder.toString())
    }

    @Test
    fun testJson() {
        val om = ObjectMapper()
        val map = Decimal(BigDecimal("0.2"))
        val json = om.writeValueAsString(map)
        println(json)
        Thread.sleep(1000)
        val jsonMap = om.readValue(json, Decimal::class.java)
        println(jsonMap.time.time)
    }
}

data class Decimal(val decimal: BigDecimal? = null, val time: Date = Date())
