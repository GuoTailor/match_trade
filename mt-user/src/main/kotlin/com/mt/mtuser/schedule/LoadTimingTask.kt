package com.mt.mtuser.schedule

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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
    @Autowired
    private lateinit var quartzManager: QuartzManager

    override fun run(args: ApplicationArguments?) {
        logger.info("启动完成")
    }
}