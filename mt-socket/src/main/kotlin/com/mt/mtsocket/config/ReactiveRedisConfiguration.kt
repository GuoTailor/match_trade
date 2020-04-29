package com.mt.mtsocket.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mt.mtcommon.Consts
import com.mt.mtsocket.socket.SocketHandler
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


/**
 * Created by gyh on 2020/3/24.
 */
@Configuration
class ReactiveRedisConfiguration {
    private val json = jacksonObjectMapper()
    @Autowired
    private lateinit var socketHandler : SocketHandler

    @Bean
    fun topic(): ChannelTopic {
        return ChannelTopic(Consts.roomEvent)
    }

    @Bean
    fun redisOperations(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Any> {
        val om = ObjectMapper()
        //om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        //om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        om.activateDefaultTyping(om.polymorphicTypeValidator, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
        val jackson2JsonRedisSerializer = GenericJackson2JsonRedisSerializer(om)
        val stringRedisSerializer = StringRedisSerializer()

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
        container.receive(topic()).subscribe { socketHandler.onRoomEvent(json.readValue(it.message)) }
        return container
    }

}