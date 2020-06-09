package com.mt.mtuser.schedule

import org.quartz.Job
import org.quartz.JobDataMap

/**
 * Created by gyh on 2020/6/8
 */
class ComputeKlineJobInfo : ScheduleJobInfo {
    override var cron: String = "0 * * * * ?"
    override var className: Class<out Job> = ComputeKlineTask::class.java   // 定时任务执行类
    override var data: JobDataMap = JobDataMap()        // 要传入的数据
    override var jobName: String = "computeKline"       // 任务job的名称
    override var groupName: String = "kline"            // 任务group的名称
}