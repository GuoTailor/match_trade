package com.mt.mtuser.schedule

import com.mt.mtuser.service.kline.KlineService
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created by gyh on 2020/6/8
 */
class ComputeKlineTask: Job {
    private val logger = LoggerFactory.getLogger(this.javaClass)
    @Autowired
    private lateinit var klineService: KlineService

    override fun execute(p0: JobExecutionContext) {
        klineService.handler()
    }
}