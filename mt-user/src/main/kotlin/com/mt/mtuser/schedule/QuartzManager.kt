package com.mt.mtuser.schedule

import org.quartz.*
import org.quartz.JobKey
import org.quartz.TriggerKey
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.stereotype.Component
import java.time.LocalTime


/**
 * Created by gyh on 2020/4/2.
 * 注意触发器和任务应使用相同group和name，也就是说每个任务分配唯一触发器，每个触发器只触发一个任务
 */
@Component
class QuartzManager {
    val log: Logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    @Autowired
    private lateinit var schedulerFactoryBean: SchedulerFactoryBean

    /**
     * 添加任务，使用任务组名，触发器名，触发器组名
     * 如果任务已存在则更新任务的触发器
     */
    fun addJob(info: ScheduleJobInfo) {
        try {
            val scheduler = schedulerFactoryBean.scheduler
            val jobKey = JobKey.jobKey(info.jobName, info.groupName)
            val cronScheduleBuilder = CronScheduleBuilder.cronSchedule(info.cron)
            val cronTrigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity(info.jobName, info.groupName)
                    .withSchedule(cronScheduleBuilder)
                    .build()

            if (!scheduler.checkExists(jobKey)) {
                val jobDetail = JobBuilder.newJob(info.className)
                        .withIdentity(info.jobName, info.groupName)
                        .usingJobData(info.data)
                        .build()
                log.info("添加定时任务 {} - {} - {}",info.cron, info.jobName, info.groupName)
                scheduler.scheduleJob(jobDetail, cronTrigger)
            } else {
                log.info("{}， {} 定时任务已经存在，只修改时间", info.jobName, info.groupName)
                val triggerKey = TriggerKey.triggerKey(info.jobName, info.groupName)
                scheduler.rescheduleJob(triggerKey, cronTrigger)
                //scheduler.resumeTrigger(triggerKey)
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
     * 删除任务
     * @param info
     * @return
     */
    fun removeJob(info: ScheduleJobInfo) = removeJob(info.groupName, info.jobName)

    /**
     * 删除任务
     * @return
     */
    fun removeJob(groupName: String, jobName: String):Boolean {
        var result = true
        try {
            val scheduler = schedulerFactoryBean.scheduler
            val jobKey = JobKey.jobKey(jobName, groupName)
            if (scheduler.checkExists(jobKey)) {
                result = scheduler.deleteJob(jobKey)
            }
            log.info("==remove job: {} {}=", jobName, result)
        } catch (e: SchedulerException) {
            log.error("删除任务失败 $jobName", e)
            result = false
        }
        return result
    }

    /**
     * 修改任务，删除老任务，添加新任务
     * 如果老任务不存在则直接添加新任务
     * 如果新任务已存在就跟新任务执行周期
     */
    fun modifyJob(info: ScheduleJobInfo, oldGroup: String, oldName: String) {
        removeJob(oldGroup, oldName)
        addJob(info)
    }

    /**
     * 修改定时任务的时间
     * @param info
     * @return
     */
    fun modifyJobTime(info: ScheduleJobInfo): Boolean {
        var result = false
        try {
            val scheduler = schedulerFactoryBean.scheduler
            val triggerKey = TriggerKey.triggerKey(info.jobName, info.groupName)
            val trigger = scheduler.getTrigger(triggerKey) as? CronTrigger
            val oldTime = trigger?.cronExpression
            if (!oldTime.equals(info.cron, true)) {
                val cronScheduleBuilder = CronScheduleBuilder.cronSchedule(info.cron)
                val ct = TriggerBuilder
                        .newTrigger()
                        .withIdentity(info.jobName, info.groupName)
                        .withSchedule(cronScheduleBuilder)
                        .build()

                scheduler.rescheduleJob(triggerKey, ct)
                scheduler.resumeTrigger(triggerKey)
                result = true
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