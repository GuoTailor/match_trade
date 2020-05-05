package com.mt.mtsocket.schedule

import com.mt.mtsocket.service.WorkService
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created by gyh on 2020/4/2.
 */
class MatchTask : Job {
    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)
    @Autowired
    private lateinit var workService: WorkService

    override fun execute(context: JobExecutionContext) {
        val roomId = context.mergedJobDataMap[roomIdKey].toString()
        log.info("开始定时任务 {}", roomId)
        //workService.match(roomId)
    }

    companion object {
        const val jobGroup = "matchGroup"
        const val roomIdKey = "roomId"
        const val timeKey = "time"
    }
}