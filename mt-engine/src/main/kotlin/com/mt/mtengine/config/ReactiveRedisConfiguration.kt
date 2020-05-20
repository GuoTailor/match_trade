package com.mt.mtengine.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.common.cache.CacheBuilder
import org.springframework.cache.Cache
import org.springframework.cache.concurrent.ConcurrentMapCache
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.util.concurrent.TimeUnit


/**
 * Created by gyh on 2020/3/24.
 */
@Configuration
class ReactiveRedisConfiguration {

    @Bean
    fun redisOperations(factory: ReactiveRedisConnectionFactory): ReactiveRedisTemplate<String, Any> {
        val om = ObjectMapper()
        //om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        //om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        om.activateDefaultTyping(om.polymorphicTypeValidator, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
        om.registerModule(KotlinModule())
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
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): ConcurrentMapCacheManager {
        return object : ConcurrentMapCacheManager("companyAndStockId") {
            override fun createConcurrentMapCache(name: String): Cache {
                return ConcurrentMapCache(name, CacheBuilder
                        .newBuilder()
                        .concurrencyLevel(4)
                        .expireAfterWrite(12, TimeUnit.HOURS)
                        .maximumSize(256)
                        .build<Any, Any>()
                        .asMap(), false)
            }
        }
    }

}