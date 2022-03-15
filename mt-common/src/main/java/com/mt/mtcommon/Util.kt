package com.mt.mtcommon

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.Future

/**
 * Created by gyh on 2021/2/4
 */

fun async(task: () -> Unit) = ThreadManager.getInstance().execute(task)

fun <T> asyncResult(task: () -> T): Future<T> = ThreadManager.getInstance().submit(task)

fun getJavaTimeModule(): JavaTimeModule {
    val javaTimeModule = JavaTimeModule()
    javaTimeModule.addSerializer(LocalDateTime::class.java, object : JsonSerializer<LocalDateTime>() {
        override fun serialize(value: LocalDateTime, gen: JsonGenerator, serializers: SerializerProvider) {
            val timestamp = value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            gen.writeNumber(timestamp)
        }
    })
    javaTimeModule.addDeserializer(LocalDateTime::class.java, object : JsonDeserializer<LocalDateTime>() {
        override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): LocalDateTime {
            val timestamp = jsonParser.valueAsLong
            return timestamp.toLocalDateTime()
        }
    })
    return javaTimeModule
}
