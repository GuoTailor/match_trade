package com.mt.mtuser.schedule

import org.quartz.*
import org.quartz.JobKey
import org.quartz.TriggerKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.stereotype.Component


/**
 * Created by gyh on 2020/4/2.
 */
@Component
class QuartzManager {
    val log: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    @Autowired
    private lateinit var schedulerFactoryBean: SchedulerFactoryBean

    /**
     * 添加任务，使用任务组名（不存在就用默认的），触发器名，触发器组名
     */
    fun addJob(info: ScheduleJobInfo) {
        try {
            val scheduler = schedulerFactoryBean.scheduler
            val jobKey = JobKey.jobKey(info.jobName, info.groupName)
            if (!scheduler.checkExists(jobKey)) {
                // JobDetail 是具体Job实例
                val jobDetail = JobBuilder.newJob(info.className)
                        .withIdentity(info.jobName, info.groupName)
                        .usingJobData(info.data)
                        .build()
                // 基于表达式构建触发器
                val cronScheduleBuilder = CronScheduleBuilder.cronSchedule(info.cron)
                // CronTrigger表达式触发器 继承于Trigger
                // TriggerBuilder 用于构建触发器实例
                val cronTrigger = TriggerBuilder
                        .newTrigger()
                        .withIdentity(info.jobName, info.groupName)
                        .withSchedule(cronScheduleBuilder)
                        .build()

                scheduler.scheduleJob(jobDetail, cronTrigger)
            } else {
                log.info("{}， {} 定时任务已经存在", info.jobName, info.groupName)
            }
        } catch (e: SchedulerException) {
            log.error("添加失败", e)
        }
    }


    /**
     * 暂停任务
     * @param info
     */
    fun pauseJob(info: ScheduleJobInfo) {
        try {
            val scheduler = schedulerFactoryBean.scheduler
            val jobKey = JobKey.jobKey(info.jobName, info.groupName)
            scheduler.pauseJob(jobKey)
            log.info("==pause job: {} success=", info.jobName)
        } catch (e: SchedulerException) {
            log.error("暂停任务失败", e)
        }
    }

    /**
     * 恢复任务
     * @param info
     */
    fun resumeJob(info: ScheduleJobInfo) {
        try {
            val scheduler = schedulerFactoryBean.scheduler
            val jobKey = JobKey.jobKey(info.jobName, info.groupName)
            scheduler.resumeJob(jobKey)
            log.info("==resume job: {} success=", info.jobName)
        } catch (e: SchedulerException) {
            log.error("恢复任务失败", e)
        }
    }

    /**
     * 删除任务，在业务逻辑中需要更新库表的信息
     * @param info
     * @return
     */
    fun removeJob(info: ScheduleJobInfo): Boolean {
        var result = true
        try {
            val scheduler = schedulerFactoryBean.scheduler
            val jobKey = JobKey.jobKey(info.jobName, info.groupName)
            if (scheduler.checkExists(jobKey)) {
                result = scheduler.deleteJob(jobKey)
            }
            log.info("==remove job: {} {}=", info.jobName, result)
        } catch (e: SchedulerException) {
            log.error("删除任务失败", e)
            result = false
        }
        return result
    }

    /**
     * 修改定时任务的时间
     * @param info
     * @return
     */
    fun modifyJobTime(info: ScheduleJobInfo): Boolean {
        var result = true
        try {
            val scheduler = schedulerFactoryBean.scheduler
            val triggerKey = TriggerKey.triggerKey(info.jobName , info.groupName)
            val trigger = scheduler.getTrigger(triggerKey) as CronTrigger
            val oldTime = trigger.cronExpression
            if (!oldTime.equals(info.cron, true)) {
                val cronScheduleBuilder = CronScheduleBuilder.cronSchedule(info.cron)
                val ct = TriggerBuilder
                        .newTrigger()
                        .withIdentity(info.jobName , info.groupName)
                        .withSchedule(cronScheduleBuilder)
                        .build()
                scheduler.rescheduleJob(triggerKey, ct)
                scheduler.resumeTrigger(triggerKey)
            }
        } catch (e: SchedulerException) {
            log.error("修改定时任务时间失败", e)
            result = false
        }
        return result
    }

    /**
     * 启动所有定时任务
     */
    fun startJobs() {
        try {
            val scheduler = schedulerFactoryBean.scheduler
            scheduler.start()
        } catch (e: SchedulerException) {
            log.error("", e)
        }
    }
}