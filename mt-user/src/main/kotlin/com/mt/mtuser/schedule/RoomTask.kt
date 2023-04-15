package com.mt.mtuser.schedule

import com.mt.mtuser.service.room.RoomService
import kotlinx.coroutines.reactor.mono
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created by gyh on 2020/4/2.
 */
class RoomTask : Job {
    private val log = LoggerFactory.getLogger(this.javaClass.simpleName)

    @Autowired
    private lateinit var roomService: RoomService

    override fun execute(context: JobExecutionContext) {
        val enable = context.mergedJobDataMap[enableKey] as Boolean
        val roomId = context.mergedJobDataMap[roomIdKey].toString()
        val roomFlag = context.mergedJobDataMap[roomFlagKey].toString()
        log.info("开始定时任务 {} {}", enable, roomId)
        val result = mono { roomService.enableRoom(roomId, enable, roomFlag) }
        result.block()
    }

    companion object {
        const val jobStartGroup = "roomStartGroup"
        const val jobEndGroup = "roomEndGroup"
        const val roomIdKey = "roomId"
        const val roomFlagKey = "roomFlag"
        const val enableKey = "enable"
    }
}