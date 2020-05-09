package com.mt.mtuser

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
class MtUserApplication

fun main(args: Array<String>) {
    runApplication<MtUserApplication>(*args)
}
