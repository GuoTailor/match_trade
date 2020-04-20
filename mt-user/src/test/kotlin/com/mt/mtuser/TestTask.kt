package com.mt.mtuser

import com.mt.mtuser.service.room.RoomService
import kotlinx.coroutines.reactor.mono
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager
import org.springframework.transaction.reactive.TransactionalOperator

/**
 * Created by gyh on 2020/4/2.
 */
class TestTask : Job {
    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)

    override fun execute(context: JobExecutionContext) {
        val index = context.mergedJobDataMap["index"].toString()
        log.info("开始定时任务 {}", index)
        Thread.sleep(1000)
    }

    companion object {
        const val jobGroup = "roomGroup"
        const val roomIdKey = "roomId"
        const val enableKey = "enable"
    }
}