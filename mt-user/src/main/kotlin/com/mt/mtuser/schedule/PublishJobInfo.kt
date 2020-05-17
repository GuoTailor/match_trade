package com.mt.mtuser.schedule

import org.quartz.Job
import org.quartz.JobDataMap
import java.time.LocalTime

/**
 * Created by gyh on 2020/5/17.
 */
class PublishJobInfo: ScheduleJobInfo {
    override var cron: String
    override var className: Class<out Job>  // 定时任务执行类
    override var data: JobDataMap // 要传入的数据
    override var jobName: String    // 任务job的名称
    override var groupName: String = PublishTask.jobPublishGroup  // 任务group的名称

    constructor(roomId: String, date: LocalTime, enabled: Boolean) {
        cron = "%d %d %d ? * *".format(date.second, date.minute, date.hour)
        this.className = PublishTask::class.java
        this.jobName = roomId
        this.data = JobDataMap(mapOf(PublishTask.roomIdKey to roomId, PublishTask.enableKey to enabled))
    }
}