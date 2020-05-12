package com.mt.mtuser.schedule

import com.mt.mtcommon.plus
import com.mt.mtuser.entity.room.BaseRoom
import org.quartz.Job
import org.quartz.JobDataMap

/**
 * Created by gyh on 2020/4/4.
 */
class RoomEndJobInfo : ScheduleJobInfo {
    override var cron: String
    override var className: Class<out Job>  // 定时任务执行类
    override var data: JobDataMap // 要传入的数据
    override var jobName: String    // 任务job的名称
    override var groupName: String = RoomTask.jobEndGroup  // 任务group的名称

    constructor(cron: String,
                jobName: String,
                data: JobDataMap = JobDataMap(),
                className: Class<out Job> = RoomTask::class.java,
                groupName: String = RoomTask.jobStartGroup) {
        this.cron = cron
        this.className = className
        this.data = data
        this.jobName = jobName
        this.groupName = groupName
    }

    constructor(room: BaseRoom, vararg data: Pair<String, *>) {
        val date = room.startTime!! + room.time!!
        cron = "%d %d %d ? * *".format(date.second, date.minute, date.hour)
        this.className = RoomTask::class.java
        this.jobName = room.roomId!!
        this.data = JobDataMap(mapOf(*data, RoomTask.roomIdKey to room.roomId, RoomTask.enableKey to false, RoomTask.roomFlagKey to room.flag))
    }

    constructor(room: BaseRoom) {
        val date = room.startTime!! + room.time!!
        // 延时2秒关闭，以让engine能消费完mq里的订单
        cron = "%d %d %d ? * *".format(date.second + 2, date.minute, date.hour)
        this.className = RoomTask::class.java
        this.jobName = room.roomId!!
        this.data = JobDataMap(mapOf(RoomTask.roomIdKey to room.roomId, RoomTask.enableKey to false, RoomTask.roomFlagKey to room.flag))
    }

}