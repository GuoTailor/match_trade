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
import com.mt.mtcommon.RoomRecord
import com.mt.mtcommon.toDuration
import com.mt.mtcommon.toLocalDateTime
import com.mt.mtsocket.distribute.ServiceRequestInfo
import com.mt.mtsocket.service.RedisUtil
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


//@SpringBootTest
class MtSocketApplicationTests {
    @Autowired
    lateinit var redisUtil: RedisUtil

    @Autowired
    lateinit var redisConnectionFactory: LettuceConnectionFactory

    @Autowired
    lateinit var redisTemplate: ReactiveRedisTemplate<String, Any>

    @Test
    fun contextLoads() {
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
        val json = om.readValue<RoomRecord>("{\"id\":null,\"roomId\":null,\"mode\":null,\"stockId\":null,\"companyId\":null,\"startTime\":[2020,6,25,15,35,15,355000000],\"endTime\":null,\"maxPrice\":null,\"minPrice\":null,\"openPrice\":null,\"closePrice\":null,\"quoteTime\":[0,0],\"secondStage\":null,\"rival\":null,\"tradeAmount\":null,\"cycle\":null,\"duration\":null}\n")
        println(json)
        println(om.writeValueAsString(json))
        println(LocalTime.MIN.toLocalDateTime().minusDays(1))
        val formatter = DateTimeFormatter.ofPattern("yyyy-M-d")
        val formattedDateTime = LocalDate.parse("2020-12-2", formatter)
        println(formattedDateTime)
        println(formattedDateTime.format(formatter))
        val nmka: LocalTime? = null
        println(nmka?.toDuration())
    }

    fun nmka(): Mono<String> {
        return Mono.fromCallable { println("nmka"); "nmka2" }
    }

    @Test
    fun testMono() {
        val mono = Mono.just("12")
                .map { testTime() }
                .flatMap {
                    println(it)
                    Mono.just("2")
                }
        println(mono.block())
        println(false != null)
    }

    @Test
    fun testTime() {
        val time = LocalDateTime.now()
        val newTime = time.withSecond(time.second)
        println(time)
        println(newTime)
        println(time.isBefore(newTime))

        println(LocalTime.MAX.plusNanos(1))
    }
}

data class Decimal(val time: LocalDateTime)
