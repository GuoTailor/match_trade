package com.mt.mtsocket.schedule

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

/**
 * Created by gyh on 2020/4/23.
 * 冷加载定时任务
 */
@Component
class LoadTimingTask : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun run(args: ApplicationArguments?) = runBlocking {
        logger.info("启动完成》》")
    }
}