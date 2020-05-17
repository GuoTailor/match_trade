package com.mt.mtuser.schedule

import com.mt.mtcommon.RoomEvent
import com.mt.mtuser.service.RedisUtil
import kotlinx.coroutines.runBlocking
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.beans.factory.annotation.Autowired

/**
 * Created by gyh on 2020/5/17.
 */
class PublishTask : Job  {
    @Autowired
    private lateinit var redisUtil: RedisUtil

    override fun execute(context: JobExecutionContext) {
        val enable = context.mergedJobDataMap[enableKey] as Boolean
        val roomId = context.mergedJobDataMap[roomIdKey].toString()
        runBlocking { redisUtil.publishRoomEvent(RoomEvent(roomId, enable)) }
    }

    companion object {
        const val jobPublishGroup = "publishGroup"
        const val enableKey = "enable"
        const val roomIdKey = "roomId"
    }
}