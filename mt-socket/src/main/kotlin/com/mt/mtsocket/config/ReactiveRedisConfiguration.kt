package com.mt.mtsocket.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mt.mtcommon.RedisConsts
import com.mt.mtcommon.toLocalDateTime
import com.mt.mtsocket.service.RoomSocketService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.LocalDateTime
import java.time.ZoneId


/**
 * Created by gyh on 2020/3/24.
 */
@Configuration
class ReactiveRedisConfiguration {
    private val json = jacksonObjectMapper()

    @Autowired
    private lateinit var roomSocketService: RoomSocketService

    @Bean
    fun topic(): ChannelTopic {
        return ChannelTopic(RedisConsts.roomEvent)
    }

    @Bean
    fun redisOperations(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Any> {
        val om = jacksonObjectMapper()
        //om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        //om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        om.activateDefaultTyping(
            om.polymorphicTypeValidator,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        )
        om.registerModule(KotlinModule())
        val jackson2JsonRedisSerializer = GenericJackson2JsonRedisSerializer(om)
        val stringRedisSerializer = StringRedisSerializer()
        val javaTimeModule = JavaTimeModule()
        javaTimeModule.addSerializer(LocalDateTime::class.java, object : JsonSerializer<LocalDateTime>() {
            override fun serialize(value: LocalDateTime, gen: JsonGenerator, serializers: SerializerProvider) {
                val timestamp = value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                gen.writeNumber(timestamp)
            }
        })
        javaTimeModule.addDeserializer(LocalDateTime::class.java, object : JsonDeserializer<LocalDateTime>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDateTime {
                val temp = p.valueAsLong
                return temp.toLocalDateTime()
            }
        })
        om.registerModule(javaTimeModule)
        val context = RedisSerializationContext.newSerializationContext<String, Any>()
            .key(stringRedisSerializer)             // key采用String的序列化方式
            .value(jackson2JsonRedisSerializer)     // value序列化方式采用jackson
            .hashKey(stringRedisSerializer)         // hash的key也采用String的序列化方式
            .hashValue(jackson2JsonRedisSerializer) // hash的value序列化方式采用jackson
            .build()
        return ReactiveRedisTemplate(factory, context)
    }

    @Bean
    fun redisMessageListenerContainer(factory: ReactiveRedisConnectionFactory): ReactiveRedisMessageListenerContainer {
        val container = ReactiveRedisMessageListenerContainer(factory)
        container.receive(topic()).subscribe { roomSocketService.onRoomEvent(json.readValue(it.message)) }
        return container
    }

}