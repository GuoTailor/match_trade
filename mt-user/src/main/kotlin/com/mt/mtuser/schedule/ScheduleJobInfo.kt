package com.mt.mtuser.schedule

import org.quartz.CronScheduleBuilder
import org.quartz.Job
import org.quartz.JobDataMap

/**
 * Created by gyh on 2020/4/2.
 */
data class ScheduleJobInfo(
        var cron: String,       // cron 表达式
        var className: Class<out Job>,  // 定时任务执行类
        var data: JobDataMap = JobDataMap(), // 要传入的数据
        var groupName: String = "roomGroup",  // 任务group的名称
        var jobName: String = "roomTimedEnable"    // 任务job的名称
) {

    constructor(): this("", RoomTask::class.java)

    /*fun nmka(): CronScheduleBuilder {

    }*/
}