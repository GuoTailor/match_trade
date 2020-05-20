package com.mt.mtsocket

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mt.mtcommon.OrderParam
import com.mt.mtcommon.RivalInfo
import com.mt.mtsocket.distribute.ServiceRequestInfo
import com.mt.mtsocket.service.RedisUtil
import com.mt.mtsocket.socket.SocketSessionStore
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


@SpringBootTest
class MtSocketApplicationTests {
    @Autowired
    lateinit var redisUtil: RedisUtil

    @Autowired
    lateinit var redisConnectionFactory: LettuceConnectionFactory

    @Autowired
    lateinit var redisTemplate: ReactiveRedisTemplate<String, Any>

    @Test
    fun contextLoads() {
        redisUtil.putUserRival(RivalInfo(roomId = "27", userId = 12, rivals = arrayListOf(1, 2, 3)), Date(1589899960000)).block()
        redisUtil.getUserRival(12, "27").map { rivals ->
            rivals.toTypedArray().forEach { println(it) }
            println(rivals)
        }.block()
    }

    @Test
    fun testRegex() {
        val data = "{\"order\":\"/echo\", \"data\": {\"value\": \"123\"}, \"req\":12}"
        val jsonOM = jacksonObjectMapper()
        val blankRegex = "\\s".toRegex()
        val orderRegex = "\"order\":(.*?)[,}]".toRegex()
        val dataRegex = "\"data\":(.*?})[,}]".toRegex()
        val reqRegex = "\"req\":(.*?)[,}]".toRegex()

        val json = data.replace(blankRegex, "")
        val orderString = orderRegex.find(json)!!.groups[1]!!.value.replace("\"", "")
        val dataString = dataRegex.find(json)?.groups?.get(1)?.value
        val reqString = reqRegex.find(json)!!.groups[1]!!.value.toInt()
        println(ServiceRequestInfo(orderString, dataString, reqString))
        val nm = jsonOM.readValue(data, TestJson::class.java)
        println(nm)
        var time = System.currentTimeMillis()
        for (i in 0..1000000) {
            val data2 = "{\"order\":\"/echo\", \"data\": {\"value\": \"${i}m\", \"order\":\"/echo\", \"data\": {\"value\": \"$i\"}, \"req\":$i}, \"req\":12}"
            jsonOM.readValue(data2, TestJson::class.java)
        }

        println(System.currentTimeMillis() - time)
        time = System.currentTimeMillis()
        for (i in 0..1000000) {
            val data2 = "{\"order\":\"/echo\", \"data\": {\"value\": \"$i\", \"order\":\"/echo\", \"data\": {\"value\": \"$i\"}, \"req\":$i}, \"req\":12}"
            val json2 = data2.replace(blankRegex, "")
            orderRegex.find(json2)!!.groups[1]!!.value.replace("\"", "")
            dataRegex.find(json2)?.groups?.get(1)?.value
            reqRegex.find(json2)!!.groups[1]!!.value.toInt()
        }
        println(System.currentTimeMillis() - time)
    }

    data class TestJson(var order: String? = null, var data: Any? = null, var req: Int?= null)

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
        om.registerModule(KotlinModule())
        val json = om.readValue<Decimal>("{\"time\":1589770215786}")
        println(json)
        println(om.writeValueAsString(json))
    }
}

data class Decimal(val time: Date)
