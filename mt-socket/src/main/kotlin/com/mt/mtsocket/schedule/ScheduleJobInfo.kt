package com.mt.mtsocket.schedule

import org.quartz.Job
import org.quartz.JobDataMap

/**
 * Created by gyh on 2020/4/2.
 */
interface ScheduleJobInfo {
    var cron: String
    var className: Class<out Job>  // 定时任务执行类
    var data: JobDataMap // 要传入的数据
    var jobName: String     // 任务job的名称
    var groupName: String  // 任务group的名称
}