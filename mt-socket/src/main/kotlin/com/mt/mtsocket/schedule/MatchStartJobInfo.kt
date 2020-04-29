package com.mt.mtsocket.schedule

import com.mt.mtcommon.RoomRecord
import org.quartz.Job
import org.quartz.JobDataMap

/**
 * Created by gyh on 2020/4/4.
 */
class MatchStartJobInfo : ScheduleJobInfo {
    override var cron: String
    override var className: Class<out Job>  // 定时任务执行类
    override var data: JobDataMap // 要传入的数据
    override var jobName: String    // 任务job的名称
    override var groupName: String = MatchTask.jobGroup  // 任务group的名称

    constructor(cron: String,
                jobName: String,
                data: JobDataMap = JobDataMap(),
                className: Class<out Job> = MatchTask::class.java,
                groupName: String = MatchTask.jobGroup) {
        this.cron = cron
        this.className = className
        this.data = data
        this.jobName = jobName
        this.groupName = groupName
    }

    /*constructor(roomRecord: RoomRecord, vararg data: Pair<String, *>) {
        val date = roomRecord.cycle!!
        this.cron = "0 %d %d ? * *".format(date.minutes, date.hours)
        this.jobName = room.roomId!!
        this.className = MatchTask::class.java
        this.data = JobDataMap(mapOf(*data, MatchTask.roomIdKey to room.roomId,MatchTask.enableKey to true))
    }

    constructor(room: BaseMatch) {
        val date = room.startTime!!
        this.cron = "0 %d %d ? * *".format(date.minute, date.hour)
        this.jobName = room.roomId!!
        this.className = MatchTask::class.java
        this.data = JobDataMap(mapOf(MatchTask.roomIdKey to room.roomId, MatchTask.enableKey to true))
    }*/

}