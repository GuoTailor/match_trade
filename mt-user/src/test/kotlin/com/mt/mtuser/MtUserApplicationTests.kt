package com.mt.mtuser

import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux
import java.time.Duration


//@SpringBootTest
class MtUserApplicationTests {

    @Test
    fun contextLoads() {
        Flux.interval(Duration.ofMillis(250))
                .map { input: Long ->
                    if (input < 3) return@map "tick $input"
                    throw RuntimeException("boom")
                }
                .elapsed()
                .retry(1)
                .subscribe({ x -> println(x) }) { x -> System.err.println(x) }

        Thread.sleep(2100)
    }

}
