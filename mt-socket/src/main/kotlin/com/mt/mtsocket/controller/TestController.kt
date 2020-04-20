package com.mt.mtsocket.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Mono

/**
 * Created by gyh on 2020/4/17.
 */
@Controller
class TestController {

    @RequestMapping("/echo")
    fun echo(@RequestParam value: String, @RequestParam(required = false) nmka: Any?): Mono<String> {
        println("nmkasdasdasd")
        return Mono.just(value).doOnNext { println("?>>>>>>>>>$it") }
    }
}