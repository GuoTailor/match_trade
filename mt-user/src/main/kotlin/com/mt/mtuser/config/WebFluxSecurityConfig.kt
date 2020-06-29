package com.mt.mtuser.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mt.mtcommon.toMillisOfDay
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


/**
 * Created by gyh on 2020/3/15.
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class WebFluxSecurityConfig {

    /**
     * 序列化LocalDateTime
     * @return
     */
    @Bean
    @Primary
    fun serializingObjectMapper(): ObjectMapper {
        val objectMapper = jacksonObjectMapper()
        val javaTimeModule = JavaTimeModule()
        javaTimeModule.addSerializer(LocalDateTime::class.java, object : JsonSerializer<LocalDateTime>() {
            override fun serialize(value: LocalDateTime, gen: JsonGenerator, serializers: SerializerProvider) {
                val timestamp = value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                gen.writeNumber(timestamp)
            }
        })
        javaTimeModule.addDeserializer(LocalDateTime::class.java, object : JsonDeserializer<LocalDateTime>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDateTime {
                val temp = p.valueAsString
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                return LocalDateTime.parse(temp, formatter)
            }

        })
        javaTimeModule.addSerializer(LocalDate::class.java, object : JsonSerializer<LocalDate>() {
            override fun serialize(value: LocalDate, gen: JsonGenerator, serializers: SerializerProvider) {
                val timestamp = value.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                gen.writeNumber(timestamp)
            }
        })
        javaTimeModule.addSerializer(LocalTime::class.java, object : JsonSerializer<LocalTime>() {
            override fun serialize(value: LocalTime, gen: JsonGenerator, serializers: SerializerProvider) {
                val timestamp = value.toMillisOfDay()
                gen.writeNumber(timestamp)
            }
        })
        objectMapper.registerModule(javaTimeModule)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return objectMapper
    }


    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    /**
     * 配置了默认表单登陆以及禁用了 csrf 功能，并开启了httpBasic 认证
     *
     * @param http
     * @throws Exception
     */
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
                .httpBasic().disable()
                .csrf().disable()
                .formLogin().disable()
                .logout().disable()
                .addFilterAt(bearerAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                //.authenticationManager { Mono.just(it) }
                //.securityContextRepository(ServerHttpBearerSecurityContextRepository())
                .exceptionHandling()
                //.authenticationEntryPoint { swe, _ -> Mono.fromRunnable { swe.response.statusCode = HttpStatus.UNAUTHORIZED } }
                .accessDeniedHandler(MyAccessDeniedHandler()).and()
                .authorizeExchange()
                .pathMatchers("/login", "/register", "/common/**").permitAll()
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .anyExchange().authenticated()
                .and()
                .build()
    }

    private fun bearerAuthenticationFilter(): AuthenticationWebFilter {
        val bearerAuthenticationFilter = AuthenticationWebFilter(ReactiveAuthenticationManager { Mono.just(it) })
        bearerAuthenticationFilter.setRequiresAuthenticationMatcher(NegatedServerWebExchangeMatcher(ServerWebExchangeMatchers.pathMatchers("/login", "/register")))
        bearerAuthenticationFilter.setServerAuthenticationConverter(ServerHttpBearerAuthenticationConverter())
        return bearerAuthenticationFilter
    }

}